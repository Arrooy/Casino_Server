package Network;

import Model.Database;
import Model.HorseRace_Model.HorseMessage;
import Model.HorseRace_Model.HorseBet;
import Model.HorseRace_Model.HorseRaceModel;
import Model.HorseRace_Model.HorseResult;
import Model.HorseRace_Model.HorseSchedule;
import Model.Transaction;


import java.sql.Timestamp;
import java.util.ArrayList;

import static Model.Transaction.TRANSACTION_HORSES;

/**
 * Controlador per el joc dels cavalls
 * */
public class HorseRaceController extends Thread  {
    private  static HorseRaceModel horseRaceModel;
    private  static boolean racing;
    private  static int finished;
    private  static long countdown;

    private  NetworkManager networkManager;
    private  static ArrayList<Client> clients;
    private static ArrayList<Client> playRequests;

    private static final long WAITTIME = 10 * 1000;
    private static final int PRIZE_MULTIPLIER = 11;

    public HorseRaceController(HorseRaceModel horseRaceModel, ArrayList<Client> clients, NetworkManager networkManager){
        this.horseRaceModel = horseRaceModel;
        this.networkManager = networkManager;
        this.racing = false;
        this.finished = 0;
        this.clients = clients;
        this.countdown = WAITTIME;
        this.playRequests = new ArrayList<>();
        this.start();
    }

    /**Afegim una aposta per gestionar mes tard*/
    public static void addHorseBet(HorseBet horseBet) {
        HorseRaceController.horseRaceModel.addBet(horseBet);
    }

    /**Esborrem totes les apostes fetes des d'un usuari a partir del seu id*/
    public static void removeBets(double id) {
        HorseBet horseBet;
        for(int i = horseRaceModel.getPendingBets().size() - 1; i >= 0 ; i--){
            horseBet = horseRaceModel.getPendingBets().get(i);
            if(horseBet.getID() == id){
                horseRaceModel.getPendingBets().remove(i);
            }
        }
    }

    public static void addPlayRequest(Client client){
        playRequests.add(client);
    }

    @Override
    public void run() {

        try {
            while(true){
                //System.out.println("HORSES - New race");
                manageRequests(playRequests);
                this.racing = false;
                this.countdown = WAITTIME;
                for(int i = clients.size() - 1; i >= 0; i--){
                    if(clients.get(i).isPlayingHorses()){
                        clients.get(i).send(new HorseMessage(countdown, "Countdown"));
                    }
                }
                //System.out.println("Horses- Sending countdown");
                for(int i = 0; i < WAITTIME/100 ; i++){
                    sleep(100);
                    countdown-=100;
                    if (countdown <= 0 ){
                        break;
                    }
                }
                this.horseRaceModel.setHorseSchedule(new HorseSchedule());
                //System.out.println("Horses- New schedule");
                //System.out.println("HORSES- PLAYING: " +  checkPlayers(clients));
                this.racing = true;
                this.finished = 0;
                if(!clients.isEmpty() & checkPlayers(clients) > 0){
                    sendRace();
                    //System.out.println("Horses- schedule sent");
                    System.out.println("Waiting for " + checkPlayers(clients) + " to finish.");
                    while(!allFinished()){
                        sleep(100);
                    }
                    sleep(500);
                    //System.out.println("Horses- result sent");
                    updateWallets();
                    //System.out.println("Horses- wallets updated");
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Error in HorseRaceController Thread");
        }



    }

    private void manageRequests(ArrayList<Client> playRequests) {
        for(int i = playRequests.size() - 1; i >= 0; i--){
            playRequests.get(i).setPlayingHorses(true);
            playRequests.remove(i);
        }

    }

    /**Retorna la quantitat de jugadors que estan jugant als cavalls*/
    private int checkPlayers(ArrayList<Client> clients) {
        int players = 0;
        for(int i = clients.size() - 1; i >= 0; i--){
            if(clients.get(i).isPlayingHorses()){
                players++;
            }
        }
        return players;
    }

    /**Indica si el client ha fet una aposta*/
    private boolean isBetting(Client client){
        for(HorseBet bet: horseRaceModel.getPendingBets()){
            if(bet.getID() == client.getId()){
                return true;
            }
        }
        return false;
    }

    /**Retorna el resultat de la carrera i el premi en cas d'aposta*/
    private static HorseMessage calculateResult(int winner, Client client) {
        HorseResult horseResult = null;
        boolean found = false;
        for(HorseBet bet: horseRaceModel.getPendingBets()){
            if(bet.getID() == client.getId()){
                found = true;
                if(bet.getHorse() == winner){
                    horseResult = new HorseResult(winner, bet.getBet() * PRIZE_MULTIPLIER);
                }else{
                    horseResult = new HorseResult(winner, 0);
                }
            }
        }
        if (!found){
            horseResult = new HorseResult(winner, 0);
        }
        return new HorseMessage(horseResult, "Result");
    }


    /**Retorna el temps restant per començar la carrera*/
    public static long getCountdown(){
        if(HorseRaceController.racing){
            return 0;
        }else{
            return HorseRaceController.countdown;
        }
    }


    /**Enviem la cursa a tot client que estigui jugant*/
    public void sendRace(){
        if(clients.size() > 0) {
            for (Client client : clients) {
                if (client.isPlayingHorses()) {
                    client.send(new HorseMessage(this.horseRaceModel.getHorseSchedule(), "Schedule"));
                }
            }
        }
    }

    /**Incrementem el comptador de jugador que han acabat de reproduir la carrera*/
    public static synchronized void addFinished(){
        HorseRaceController.finished++;
    }

    /**Indica si tots els jugadors an acabat de reproduir la carrera*/
    public boolean allFinished(){
        int i = 0;
        for(Client client: clients){
            if(client.isPlayingHorses()){
                i++;
            }
        }
        return i <= HorseRaceController.finished;

    }

    /**Retorna si s'esta reproduint una cursa*/
    public static boolean isRacing(){
        return HorseRaceController.racing;
    }

    /**Actualitzem les monedes de tot usuari que hagi apostat i guanyat*/
    public void updateWallets(){
        Transaction transaction;
        for(HorseBet h: horseRaceModel.getPendingBets()){
            if(h.getHorse() == this.horseRaceModel.getHorseSchedule().getWinner()){
                for(Client client: clients){
                    if(client.getId() == h.getID()){
                        transaction = new Transaction("HORSES", client.getName(), h.getBet() * PRIZE_MULTIPLIER, TRANSACTION_HORSES);
                        transaction.setTime(new Timestamp(System.currentTimeMillis()));
                        Database.registerTransaction(transaction);
                    }
                }

            }
        }
    }

    public void sendResult(Client client) {
        if(isRacing()){
              client.send(HorseRaceController.calculateResult(horseRaceModel.getHorseSchedule().getWinner(), client));
        }
    }
}


