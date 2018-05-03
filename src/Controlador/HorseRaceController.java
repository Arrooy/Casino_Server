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

public class HorseRaceController extends Thread  {
    private  static HorseRaceModel horseRaceModel;
    private  static boolean racing;
    private  static int finished;
    private  static float countdown;

    private  NetworkManager networkManager;
    private  static ArrayList<Client> clients;

    private static final int WAITTIME = 600;
    private static final int PRIZE_MULTIPLIER = 11;

    public HorseRaceController(HorseRaceModel horseRaceModel, ArrayList<Client> clients, NetworkManager networkManager){
        this.horseRaceModel = horseRaceModel;
        this.networkManager = networkManager;
        this.racing = false;
        this.finished = 0;
        this.clients = clients;
        this.countdown = 60.0f;
        this.start();
    }

    public static void addHorseBet(HorseBet horseBet) {
        HorseRaceController.horseRaceModel.addBet(horseBet);
    }

    public static void removeBets(long id) {
        HorseBet horseBet;
        for(int i = horseRaceModel.getPendingBets().size() - 1; i >= 0 ; i--){
            horseBet = horseRaceModel.getPendingBets().get(i);
            if(horseBet.getID() == id){
                horseRaceModel.getPendingBets().remove(i);
            }
        }
    }

    @Override
    public void run() {
        try {
            this.racing = false;
            for(int i = 0; i < WAITTIME ; i++){
                sleep(100);
                countdown-=0.1f;
            }
            countdown = 60.0f;
            this.horseRaceModel.setHorseSchedule(new HorseSchedule());
            this.racing = true;
            this.finished = 0;
            sendRace();
            while(!allFinished()){
                sleep(200);
            }
            for(Client client: clients){
                if(client.isPlayingHorses()){
                    if(isBetting(client)){
                        client.send(HorseRaceController.calculateResult(client.getId(), horseRaceModel.getHorseSchedule().getWinner()));
                    }
                    client.send(new HorseMessage(countdown, "Countdown"));
                }
            }
            updateWallets();

        } catch (InterruptedException e) {
            System.out.println("Error in HorseRaceController Thread");
        }



    }

    private boolean isBetting(Client client){
        for(HorseBet bet: horseRaceModel.getPendingBets()){
            if(bet.getID() == client.getId()){
                return true;
            }
        }
        return false;
    }

    private static HorseMessage calculateResult(long id, int winner) {
        HorseResult horseResult = null;
        boolean found = false;
        for(HorseBet bet: horseRaceModel.getPendingBets()){
            if(bet.getID() == id){
                found = true;
                if(bet.getHorse() == winner){
                    horseResult = new HorseResult(winner, bet.getBet() * PRIZE_MULTIPLIER);
                }else{
                    horseResult = new HorseResult(winner, 0);
                }
            }
        }
        return new HorseMessage(horseResult, "Result");
    }


    public static float getCountdown(){
        if(HorseRaceController.racing){
            return -1;
        }else{
            return HorseRaceController.countdown;
        }
    }


    public void sendRace(){
        if(clients.size() > 0) {
            for (Client client : clients) {
                if (client.isPlayingHorses()) {
                    client.send(new HorseMessage(this.horseRaceModel.getHorseSchedule(), "Schedule"));
                }
            }
        }
    }

    public static synchronized void addFinished(){
        HorseRaceController.finished++;
    }

    public boolean allFinished(){
        int i = 0;
        for(Client client: clients){
            if(client.isPlayingHorses()){
                i++;
            }
        }
        return i == HorseRaceController.finished;

    }

    public static boolean isRacing(){
        return HorseRaceController.racing;
    }

    public void updateWallets(){
        Transaction transaction;
        for(HorseBet h: horseRaceModel.getPendingBets()){
            if(h.getHorse() == this.horseRaceModel.getHorseSchedule().getWinner()){
                for(Client client: clients){
                    if(client.getId() == h.getID()){
                        transaction = new Transaction("HORSES", client.getName(), h.getBet() * PRIZE_MULTIPLIER, 1);
                        transaction.setTime(new Timestamp(System.currentTimeMillis()));
                        Database.registerTransaction(transaction);
                    }
                }

            }
        }
    }
}


