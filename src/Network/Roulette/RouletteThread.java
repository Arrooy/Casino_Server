package Network.Roulette;

import Model.Database;
import Model.RouletteModel.RouletteMessage;
import Model.Transaction;
import Network.Client;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;

public class RouletteThread extends Thread {

    private final int[] winnerConversionTable = {0, 3, 2, 1, 6, 5, 4, 9, 8, 7, 12, 11, 10, 15, 14, 13, 18, 17, 16, 21, 20, 19, 24, 23, 22, 27, 26, 25, 30, 29, 28, 33, 32, 31, 36, 35, 34, 12, 12, 12, 18, 18, 18, 18, 18, 18, 12, 12, 12};
    private final int[] redCells = {1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36};

    private class Bet {
        long bet;
        String username;
        int cellID;

        Bet(String username, long bet, int cellID) {
            this.bet = bet;
            this.username = username;
            this.cellID = cellID;
        }
    }

    private RouletteManager rouletteManager;
    private ArrayList<Client> clients;
    private static double timeTillNext;

    private static final int[] converTable = {0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 24, 26, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26};

    private LinkedList<Bet> bets;

    public RouletteThread(ArrayList<Client> clients) {
        rouletteManager = new RouletteManager(600, 600, this);
        rouletteManager.init();

        this.clients = clients;
        bets = new LinkedList<>();

        timeTillNext = 0;
        start();
    }

    @Override
    public void run() {
        while (true) {
            rouletteManager.setRandomParams();
            rouletteManager.shootBall();

            double errortimer = System.nanoTime();

            while (!rouletteManager.winnerExists()) {
                rouletteManager.update();
                if ((System.nanoTime() - errortimer)/1000000 > 100) {
                    errortimer = System.nanoTime();
                    rouletteManager.setRandomParams();
                    rouletteManager.shootBall();
                }
            }

            RouletteMessage rm = rouletteManager.genMessage();

            System.out.println("[ROULETTE WINNER]: " + converTable[rm.getWinner()]);

            while (Timestamp.from(Instant.now()).getTime() < timeTillNext);

            timeTillNext = Timestamp.from(Instant.now()).getTime() + 1000 * 60 * .5;
            rm.setTimeTillNext(timeTillNext);
            for (Client c: clients) c.sendRouletteShot(rm);

            LinkedList<String> names = new LinkedList<>();
            for (Bet bet: bets) {
                boolean found = false;
                for (String name: names) if (name.equals(bet.username)) found = true;
                if (!found) names.add(bet.username);
            }

            for (String n: names) {
                Transaction t = new Transaction("", n, 0, Transaction.TRANSACTION_ROULETTE);

                for (Bet bet: bets) if (bet.username.equals(n)) {
                    if (isWinner(bet.cellID, rm.getWinner())) t.setGain(t.getGain() + moneyWon(bet));
                    else t.setGain(t.getGain() - bet.bet);
                }

                Database.registerTransaction(t);
            }

            cleanBetList();

            try {
                sleep((int) (1000 * 60 * .4));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long moneyWon(Bet bet) {
        if (bet.cellID < 37) return bet.bet * 36 - bet.bet;
        else return (36 / winnerConversionTable[bet.cellID] - 1) * bet.bet;
    }

    private boolean isWinner(int bet, int win) {
        if (bet < 37 && winnerConversionTable[bet] == win) return true;

        switch (bet - 37) {
            case 0:
                if (win % 3 == 0) return true;
                break;
            case 1:
                if ((win + 1) % 3 == 0) return true;
                break;
            case 2:
                if ((win + 2) % 3 == 0) return true;
                break;
            case 3:
                if (win > 0 && win < 19) return true;
                break;
            case 4:
                if (win % 2 == 0) return true;
                break;
            case 7:
                if (win % 2 == 1) return true;
                break;
            case 5:
                if (isRedCell(win)) return true;
                break;
            case 6:
                if (!isRedCell(win)) return true;
                break;
            case 8:
                if (win >= 19) return true;
                break;
        }

        return false;
    }

    public static double getTimeTillNext() {
        return timeTillNext;
    }

    public void addBet(String username, long bet, int cellID) {
        bets.add(new Bet(username, bet, cellID));
    }

    public void cleanBetList() {
        for (int i = bets.size()-1; i > 0; i--) bets.remove(i);
        bets = new LinkedList<>();
    }

    private boolean isRedCell(int cell) {
        for (int i: redCells) if (i == cell) return true;
        return false;
    }
}