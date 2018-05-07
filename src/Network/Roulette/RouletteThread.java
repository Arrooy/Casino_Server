package Network.Roulette;

import Model.RouletteMessage;
import Network.Client;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

public class RouletteThread extends Thread {

    private RouletteManager rouletteManager;
    private ArrayList<Client> clients;
    private static double timeTillNext;

    public RouletteThread(ArrayList<Client> clients) {
        rouletteManager = new RouletteManager(600, 600, this);
        rouletteManager.init();

        this.clients = clients;

        timeTillNext = 0;
        start();
    }

    @Override
    public void run() {
        while (true) {
            rouletteManager.setRandomParams();
            rouletteManager.shootBall();

            double errortimer = System.nanoTime();
            System.out.println("searching winner");
            while (!rouletteManager.winnerExists()) {
                rouletteManager.update();
                if ((System.nanoTime() - errortimer)/1000000 > 100000) {
                    errortimer = System.nanoTime();
                    rouletteManager.setRandomParams();
                    rouletteManager.shootBall();
                    System.out.println("[ROULETTE WARNING]: mal rollo");
                }
            }
            System.out.println("found winner");

            RouletteMessage rm = rouletteManager.genMessage();

            System.out.println("[ROULETTE WINNER]: " + rm.getWinner());
            while (Timestamp.from(Instant.now()).getTime() < timeTillNext);

            timeTillNext = Timestamp.from(Instant.now()).getTime() + 1000 * 60 * .3;
            rm.setTimeTillNext(timeTillNext);
            for (Client c: clients) c.sendRouletteShot(rm);

            try {
                sleep((int) (1000 * 60 * .2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static double getTimeTillNext() {
        return timeTillNext;
    }
}
