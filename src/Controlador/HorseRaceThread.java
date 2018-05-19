package Controlador;

import Model.Database;
import Model.HorseRace_Model.HorseMessage;
import Model.HorseRace_Model.HorseBet;
import Model.HorseRace_Model.HorseRaceModel;
import Model.HorseRace_Model.HorseResult;
import Model.HorseRace_Model.HorseSchedule;
import Model.Transaction;
import Network.Client;
import Network.NetworkManager;


import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Controlador per el joc dels cavalls
 * */
public class HorseRaceThread extends Thread  {
    private  static HorseRaceModel horseRaceModel;
    private  static boolean racing;
    private  static int finished;
    private  static long countdown;
    private  long   startTime;

    private NetworkManager networkManager;
    private  static ArrayList<Client> clients;
    private static ArrayList<Client> playRequests;

    private static final long WAITTIME = 60 * 1000;
    private static final int PRIZE_MULTIPLIER = 11;

    public HorseRaceThread(HorseRaceModel horseRaceModel, ArrayList<Client> clients, NetworkManager networkManager){
        this.horseRaceModel = horseRaceModel;
        this.networkManager = networkManager;
        this.racing = false;
        this.finished = 0;
        this.clients = clients;
        this.countdown = WAITTIME;
        this.playRequests = new ArrayList<>();
        this.start();
        startTime = 0;
    }

    /**Afegim una aposta per gestionar mes tard*/
    public static synchronized void addHorseBet(HorseBet horseBet) {
        HorseRaceThread.horseRaceModel.addBet(horseBet);
        sendBetList();
    }

    public static synchronized void sendBetList(){
        String[][] betList = new String[3][horseRaceModel.getPendingBets().size()];
        for (int i = horseRaceModel.getPendingBets().size() - 1; i >= 0; i--) {
            betList[0][i] = horseRaceModel.getPendingBets().get(i).getName();
            betList[1][i] = "Horse " + (12 - horseRaceModel.getPendingBets().get(i).getHorse());
            betList[2][i] = horseRaceModel.getPendingBets().get(i).getBet() + "";
        }

        for (Client c: clients) {
            if(c.isPlayingHorses()){
                c.sendHorseBetList(betList);
            }
        }
    }



    /**Esborrem totes les apostes fetes des d'un usuari a partir del seu username*/
    public static synchronized void removeBets(String name) {
        HorseBet horseBet;
        for(int i = horseRaceModel.getPendingBets().size() - 1; i >= 0 ; i--){
            horseBet = horseRaceModel.getPendingBets().get(i);
            if(horseBet.getName() == name){
                horseRaceModel.getPendingBets().remove(i);
            }
        }
    }

    public static synchronized void addPlayRequest(Client client){
        playRequests.add(client);
    }

    public static synchronized void removeRequests(Client client) {
        if (playRequests.contains(client)){
            playRequests.remove(client);
        }
    }

    @Override
    public void run() {

        try {
            while(true){
                manageRequests(playRequests);
                this.racing = false;
                this.countdown = WAITTIME;
                this.startTime = System.currentTimeMillis();
                for(int i = clients.size() - 1; i >= 0; i--){
                    if(clients.get(i).isPlayingHorses()){
                        clients.get(i).send(new HorseMessage(countdown, "Countdown"));
                    }
                }
                while(countdown  > 0){
                    countdown -= (System.currentTimeMillis() - startTime);
                    startTime = System.currentTimeMillis();
                    sleep(100);
                }
                this.horseRaceModel.setHorseSchedule(new HorseSchedule());
                this.finished = 0;
                if(!clients.isEmpty() & checkPlayers(clients) > 0){
                    this.racing = true;
                    sendRace();
                    while(!allFinished()){
                        sleep(100);
                    }
                    updateWallets();
                    sendBetList();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Error in HorseRaceThread Thread");
        }



    }

    private synchronized void manageRequests(ArrayList<Client> playRequests) {
        if(!playRequests.isEmpty()) {
            for (int i = playRequests.size() - 1; i >= 0; i--) {
                playRequests.get(i).setPlayingHorses(true);
                playRequests.remove(i);
            }
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
            if(bet.getName() == client.getName()){
                return true;
            }
        }
        return false;
    }

    /**Retorna el resultat de la carrera i el premi en cas d'aposta*/
    private static HorseMessage calculateResult(int winner, Client client) {
        HorseResult horseResult =  new HorseResult(winner, 0);
        HorseBet bet;
        for(int i = horseRaceModel.getPendingBets().size() - 1; i >= 0; i--){
            bet = horseRaceModel.getPendingBets().get(i);
                if(bet.getHorse() == winner && bet.getName().equals(client.getUser().getUsername())){
                    horseResult = new HorseResult(winner, bet.getBet() * PRIZE_MULTIPLIER);
                    break;
                }
        }
        return new HorseMessage(horseResult, "Result");
    }


    /**Retorna el temps restant per començar la carrera*/
    public static long getCountdown(){
        if(HorseRaceThread.racing){
            return 0;
        }else{
            return countdown;
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
        HorseRaceThread.finished++;
    }

    /**Indica si tots els jugadors an acabat de reproduir la carrera*/
    public boolean allFinished(){
        int i = 0;
        for(Client client: clients){
            if(client.isPlayingHorses()){
                i++;
            }
        }

        return i <= HorseRaceThread.finished;

    }

    /**Retorna si s'esta reproduint una cursa*/
    public static boolean isRacing(){
        return HorseRaceThread.racing;
    }

    /**Actualitzem les monedes de tot usuari que hagi apostat i guanyat*/
    public void updateWallets(){
        Transaction transaction;
        HorseBet h;
        for(int i = horseRaceModel.getPendingBets().size() - 1; i >= 0; i--){
            h = horseRaceModel.getPendingBets().get(i);
            if(h.getHorse() == this.horseRaceModel.getHorseSchedule().getWinner()) {
                    transaction = new Transaction(null, h.getName(), h.getBet() * PRIZE_MULTIPLIER, Transaction.TRANSACTION_HORSES);
                    transaction.setTime(new Timestamp(System.currentTimeMillis()));
                    Database.registerTransaction(transaction);
            }
        }
        horseRaceModel.getPendingBets().clear();
    }

    public void sendResult(Client client) {
        client.send(HorseRaceThread.calculateResult(horseRaceModel.getHorseSchedule().getWinner(), client));
    }
}


