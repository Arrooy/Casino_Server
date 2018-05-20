package Controlador;

import Model.Database;
import Network.HorseMessage;
import Model.HorseRace_Model.HorseBet;
import Model.HorseRace_Model.HorseRaceModel;
import Model.HorseRace_Model.HorseResult;
import Model.HorseRace_Model.HorseSchedule;
import Model.Transaction;
import Network.Client;


import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Thread per el joc dels cavalls encarregat de gestionar els jugadors i la logica del joc
 * */
public class HorseRaceThread extends Thread  {
    /**Model de la cursa*/
    private  static HorseRaceModel horseRaceModel;
    /**Estat de la cursa*/
    private  static boolean racing;
    /**Jugadors que han acabat de veure la cursa*/
    private  static int finished;
    /**Compta enrere per començar la cursa*/
    private  static long countdown;
    /**Temps per començar la cursa*/
    private  long   startTime;

    /**Clients*/
    private  static ArrayList<Client> clients;
    /**Clients que han solicitat jugar*/
    private static ArrayList<Client> playRequests;

    /**Temps d'espera*/
    private static final long WAITTIME = 60 * 1000;
    /**Premi per una apota guanyada*/
    private static final int PRIZE_MULTIPLIER = 11;

    public HorseRaceThread(HorseRaceModel horseRaceModel, ArrayList<Client> clients){
        this.horseRaceModel = horseRaceModel;
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
    }

    /**
     * Envia la llista d'apostes a tots els usuaris que estan jugant amb les seves apostes en cas de que hagin apostat
     */
    public static synchronized void sendList(){
        String[][] list = new String[3][playRequests.size() + checkPlayers() ];
        Client client;
        int betPos;
        int j = 0;
        for (int i = playRequests.size()- 1; i >= 0; i--) {
            list[0][i] = playRequests.get(i).getUser().getUsername();
            list[1][i] = "";
            list[2][i] = "";
            j = i + 1;
        }
        for(int k = clients.size() - 1; k >= 0; k--) {
            client = clients.get(k);
            if(client.isPlayingHorses()) {
                list[0][j] = client.getUser().getUsername();
                betPos = isBetting(client);
                if(betPos >= 0) {
                    list[1][j] = "Horse " + (12 - horseRaceModel.getPendingBets().get(betPos).getHorse());
                    list[2][j] = horseRaceModel.getPendingBets().get(betPos).getBet() + "";
                }else {
                    list[1][j] = "";
                    list[2][j] = "";
                }
                j++;
            }
        }
        for (int i = clients.size() - 1; i >= 0; i--) {
            client = clients.get(i);
            if(client.isPlayingHorses()){
                client.sendHorseBetList(list);
            }
        }
    }



    /**Esborrem totes les apostes fetes des d'un usuari a partir del seu username*/
    public static synchronized void removeBets(String name) {
        HorseBet horseBet;
        for(int i = horseRaceModel.getPendingBets().size() - 1; i >= 0 ; i--){
            horseBet = horseRaceModel.getPendingBets().get(i);
            if(horseBet.getName().equals(name)){
                horseRaceModel.getPendingBets().remove(i);
            }
        }
    }

    /**
     * S'afageix un client a la llista de clients que volen jugar
     * @param client Client que vol jugar
     */
    public static synchronized void addPlayRequest(Client client){
        playRequests.add(client);
    }

    /**
     * S'elimina un client de la llista de clients que volen jugar
     * @param client Client que ja no vol jugar
     */
    public static synchronized void removeRequests(Client client) {
        playRequests.remove(client);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {

        try {
            while(true){
                manageRequests(playRequests);
                sendList();
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
                if(!clients.isEmpty() & checkPlayers() > 0){
                    this.racing = true;
                    sendRace();
                    while(!allFinished()){
                        sleep(100);
                    }
                    updateWallets();
                    sendList();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Error in HorseRaceThread Thread");
        }



    }

    /**
     * Busquem tots els clients de la llista de solicituds de joc i els afegim al joc
     * @param playRequests llista de solicituds de joc
     */
    private synchronized void manageRequests(ArrayList<Client> playRequests) {
        if(!playRequests.isEmpty()) {
            for (int i = playRequests.size() - 1; i >= 0; i--) {
                playRequests.get(i).setPlayingHorses(true);
                playRequests.remove(i);
            }
        }

    }

    /**Retorna la quantitat de jugadors que estan jugant als cavalls*/
    private static int checkPlayers() {
        int players = 0;
        for(int i = clients.size() - 1; i >= 0; i--){
            if(clients.get(i).isPlayingHorses()){
                players++;
            }
        }
        return players;
    }

    /**
     * Metode que retorna -1 en cas de que el client no hag fet cap aposta, retorna la posico en l'array
     * d'apostes pendents en cas de que hagi apostat
     * @param client client a comprovar
     * @return int indicant la posicio del client en l'array d'apostes pendents
     */
    public static int isBetting(Client client){
        for(int i = horseRaceModel.getPendingBets().size() - 1; i >= 0 ; i--){
            if(horseRaceModel.getPendingBets().get(i).getName().equals(client.getUser().getUsername())){
                 return i;
            }
        }
        return -1;
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

    /**
     * Envia el resultat de la carrera a un client
     * @param client Client al que s'el vol enviar la carrera
     */
    public void sendResult(Client client) {
        client.send(HorseRaceThread.calculateResult(horseRaceModel.getHorseSchedule().getWinner(), client));
    }
}


