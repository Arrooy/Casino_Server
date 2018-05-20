package Network.Roulette;

import Model.Database;
import Model.RouletteModel.RouletteMessage;
import Model.Transaction;
import Network.Client;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Classe que gestiona el fil d'execució del joc de la ruleta.
 * El seu funcionament es basa en gestionar una llista de clients que
 * podran estar o no conectats, i periodicament enviar-los la informació
 * necessaria per a simular una tirada de la ruleta i obtenir tots el mateix
 * resultat de manera sincronitzada.
 *
 * El servidor, previament a enviar la informacio de la tirada als jugadors,
 * calcula per si mateix el resultat final de la tirada i posteriorment,
 * mitjançant una llista d'apostes, actualitza la informació del jugador directament
 * a la base de dades.
 *
 * En quant a les apostes cal dir que altres fils d'execució son els que
 * s'encarregaran d'omplir la llista al anar rebent les peticions dels clients,
 * i aquesta es processarà i buidarà cada cop que es generi una nova tirada.
 *
 * Finalment cal dir que el temps d'espera fins la següent tirada s'envia a
 * l'usuari per a que aquest pugui mostrar el compte enrere al jugador.
 */
public class RouletteThread extends Thread {

    /** Taula de conversió per obtenir informació corresponent a cada cel·la de la taula d'apostes */
    private final int[] winnerConversionTable = {0, 3, 2, 1, 6, 5, 4, 9, 8, 7, 12, 11, 10, 15, 14, 13, 18, 17, 16, 21, 20, 19, 24, 23, 22, 27, 26, 25, 30, 29, 28, 33, 32, 31, 36, 35, 34, 12, 12, 12, 18, 18, 18, 18, 18, 18, 12, 12, 12};

    /** Taula que recull totes les caselles de color vermell */
    private final int[] redCells = {1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36};

    private static final double MINSTOWAIT = 1;

    /**
     * Classe privada per a facilitar la gestió de les apostes
     */
    private class Bet {

        /** Quantitat de diners apostats */
        long bet;

        /** Usuari que ha realitzat l'aposta */
        String username;

        /** Cel·la de la taula en la que s'ha realitzat la aposta
         * Nota: correspon a l'identificador d'aquesta i no al seu valor real. */
        int cellID;

        /** Constructor per inicialitzar les variables */
        Bet(String username, long bet, int cellID) {
            this.bet = bet;
            this.username = username;
            this.cellID = cellID;
        }
    }

    /** Objecte que simula cada tir per a calcular-ne el guanyador */
    private RouletteManager rouletteManager;

    /** Llista de clients als que enviar la informació pertinent */
    private ArrayList<Client> clients;

    /** Temps que ha de passar fins la següent tirada */
    private static double timeTillNext;

    /** Taula que converteix el index de cada secció de la roda de la ruleta al seu valor real */
    private static final int[] converTable = {0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 24, 26, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26};

    /** Llista d'apostes realitzades pels usuaris */
    private LinkedList<Bet> bets;

    /**
     * Constructor de la classe que inicialitza els parametres necessaris.
     * @param clients Llista on es manté el registre de clients connectats al servidor
     */
    public RouletteThread(ArrayList<Client> clients) {
        rouletteManager = new RouletteManager(600, 600);
        rouletteManager.init();

        this.clients = clients;
        bets = new LinkedList<>();

        timeTillNext = 0;
        start();
    }

    /**
     * Fil d'execució que controla la lògica de la ruleta i manté a tots els usuaris
     * informats de cada tirada i acció.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            //Es realitza la tirada
            rouletteManager.setRandomParams();
            rouletteManager.shootBall();

            double errortimer = System.nanoTime();

            //Es calcula el guanyador
            while (!rouletteManager.winnerExists()) {
                rouletteManager.update();
                if ((System.nanoTime() - errortimer)/1000000 > 100) {
                    errortimer = System.nanoTime();
                    rouletteManager.setRandomParams();
                    rouletteManager.shootBall();
                }
            }

            //Es genera el missatge a enviar als usuaris
            RouletteMessage rm = rouletteManager.genMessage();

            //S'espera a que el servidor es sincronitzi amb els temps establerts
            while (Timestamp.from(Instant.now()).getTime() < timeTillNext);

            //Es calcula el moment en el que es realitzara la següent tirada
            timeTillNext = Timestamp.from(Instant.now()).getTime() + 1000 * 60 * MINSTOWAIT;
            rm.setTimeTillNext(timeTillNext);

            //S'envia la tirada a tots els clients
            for (Client c: clients) c.sendRouletteShot(rm);

            //Es genera una llista que recull els noms de tots els usuaris que
            //hagin realitzat alguna aposta
            LinkedList<String> names = new LinkedList<>();
            for (Bet bet: bets) {
                boolean found = false;
                for (String name: names) if (name.equals(bet.username)) found = true;
                if (!found) names.add(bet.username);
            }

            //Es comprova quant ha guanyat/perdut cada usuari i s'actualitza la base de dades
            int winner = converTable[rm.getWinner()];
            for (String n: names) {
                Transaction t = new Transaction("", n, 0, Transaction.TRANSACTION_ROULETTE);

                for (Bet bet: bets) if (bet.username.equals(n)) {
                    if (isWinner(bet.cellID, winner)) t.setGain(t.getGain() + moneyWon(bet));
                    else t.setGain(t.getGain() - bet.bet);
                }
                Database.registerTransaction(t);
            }

            //Es nateja la llista d'apostes
            cleanBetList();
            addBet(null, -1, -1, false);

            //S'espera fins a instants abans de realitzar la següent tirada
            try {
                sleep((int) (1000 * 60 * (MINSTOWAIT - .1)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Mètode que nateja tota la llista d'apostes realitzades d'un usuari en concret
     * @param username Nom del usuari a borrar
     */
    public synchronized void cleanUserBets(String username) {
        System.out.println(username);
        for (int i = bets.size() - 1; i >= 0; i--) if (bets.get(i).username.equals(username)) bets.remove(i);
        addBet(null, -1, -1, false);
    }

    /**
     * Mètode que calcula l'aposta total realitzada per un sol usuari
     * @param username Usuari del que es calcula l'aposta
     * @return Total apostat per l'usuari indicat
     */
    public synchronized long getUserBet(String username) {
        long money = 0;
        for (Bet bet: bets) if (bet.username.equals(username)) money += bet.bet;
        return money;
    }

    /**
     * Mètode que calcula els diners guanyats en una aposta guanyadora.
     * @param bet Diners apostats inicialment
     * @return Quantitat total guanyada
     */
    private long moneyWon(Bet bet) {
        if (bet.cellID < 37) return bet.bet * 36 - bet.bet;
        else return (36 / winnerConversionTable[bet.cellID] - 1) * bet.bet;
    }

    /**
     * Mètode que comprova si una aposta ha estat guanyadora.
     * @param bet Identificador de la cel·la apostada
     * @param win Numero guanyador de l'aposta
     * @return Indica si s'ha guanyat o no
     */
    private boolean isWinner(int bet, int win) {
        if (bet < 37 && bet == win) return true;

        if (win == 0) return false;

        switch (bet) {
            case 39:
                if (win % 3 == 0) return true;
                break;
            case 38:
                if ((win + 1) % 3 == 0) return true;
                break;
            case 37:
                if ((win + 2) % 3 == 0) return true;
                break;
            case 40:
                if (win > 0 && win < 19) return true;
                break;
            case 41:
                if (win % 2 == 0) return true;
                break;
            case 44:
                if (win % 2 == 1) return true;
                break;
            case 42:
                if (isRedCell(win)) return true;
                break;
            case 43:
                if (!isRedCell(win)) return true;
                break;
            case 45:
                if (win >= 19) return true;
                break;
            case 46:
                if (win <= 12) return true;
                break;
            case 47:
                if (win > 12 && win < 25) return true;
                break;
            case 48:
                if (win >= 25) return true;
                break;
        }

        return false;
    }

    /**
     * Getter del temps restant fins la següent aposta
     * @return Temps restant fins la següent aposta
     */
    public static double getTimeTillNext() {
        return timeTillNext;
    }

    /**
     * Mètode per a afegir una aposta a la llista.
     * @param username Usuari que realitza la aposta
     * @param bet Quantitat apostada
     * @param cellID Cel·la en la que s'ha apostat
     */
    public synchronized void addBet(String username, long bet, int cellID, boolean toadd) {
        if (toadd) bets.add(new Bet(username, bet, cellID));
        LinkedList<String> nonBet = new LinkedList<>();

        for (Client c: clients) if (c.isConnectedToRoulette()) {
            boolean bool = false;
            for (Bet b: bets) if (b.username.equals(c.getUser().getUsername())) bool = true;
            if (!bool) nonBet.add(c.getUser().getUsername());
        }

        int i;
        String[][] betInfo = new String[3][bets.size() + nonBet.size()];
        for (i = 0; i < bets.size(); i++) {
            betInfo[0][i] = bets.get(i).username;
            betInfo[1][i] = bets.get(i).cellID + "";
            betInfo[2][i] = bets.get(i).bet + "";
        }

        for (; i < bets.size() + nonBet.size(); i++) {
            betInfo[0][i] = nonBet.get(i - bets.size());
            betInfo[1][i] = "-----";
            betInfo[2][i] = "-----";
        }

        for (Client c: clients) c.sendRouletteList(betInfo);
    }

    /**
     * Mètode que nateja completament el llistat d'apostes
     */
    private void cleanBetList() {
        for (int i = bets.size()-1; i > 0; i--) bets.remove(i);
        bets = new LinkedList<>();
    }

    /**
     * Mètode que comprova si un numero és o no vermell
     * @param cell Numero a comprovar
     * @return Boolea que indica si es vermell
     */
    private boolean isRedCell(int cell) {
        for (int i: redCells) if (i == cell) return true;
        return false;
    }
}