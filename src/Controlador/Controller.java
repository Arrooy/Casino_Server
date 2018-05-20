package Controlador;

import Model.Database;
import Model.HorseRace_Model.HorseRaceModel;
import Model.Transaction;
import Model.User;
import Network.NetworkManager;
import Vista.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Controlador del servidor*/

public class Controller implements ActionListener, WindowListener, MouseListener, ComponentListener{

    /** Finestra grafica del servidor*/
    private Finestra vista;

    /**Vista principal del servidor que conte els botons per accedir a les diferents opcions de les quals disposa,
     * veure el ranking i veure el top 5*/
    private MainView mainView;

    /**Vista que conte 3 botons per escollir quina de les 3 grafiques del top 5 es vol visualitzar (BJ, cavalls, ruleta),
     * a mes a mes tambe conte un panell per visualitzar la grafica seleccionada*/
    private Top5OptionsView top5;

    /**Vista que mostra el ranking per mitja d'una taula on es pot veure amb
     *ordre ascendent les monedes de les quals disposa cada usuari*/
    private RankingView ranking;

    /**Vista que mostra per mitja d'una grafica l'evolucio monetaria de l'usuari*/
    private CoinHistoryView coinHistoryView;

    /**Model de la cursa de cavalls*/
    private HorseRaceModel horseRaceModel;

    /** Gestor de la network*/
    private NetworkManager networkManager;

    /** Inicialitza el controlador del servidor*/
    public Controller(Finestra vista, NetworkManager networkManager){
        this.vista = vista;
        this.networkManager = networkManager;
        this.horseRaceModel = new HorseRaceModel();
    }

    /** Mostra un error amb una alerta al centre de la finestra grafica*/
    public void displayError(String title, String errorText){
        vista.displayError(title,errorText);
    }

    /** Metode per a tencar el servidor de forma segura.*/
    public void exitProgram(int status){
        Tray.exit();
        vista.dispose();
        coinHistoryView.closeView();
        System.exit(status);
    }

    public void setMainView(MainView mainView) {
        this.mainView = mainView;
    }

    public void setLogInView(Top5OptionsView top5) {
        this.top5 = top5;
    }

    public void setRankingView(RankingView ranking) {
        this.ranking = ranking;
    }

    public void setCoinHistoryView(CoinHistoryView coinHistoryView) {
        this.coinHistoryView = coinHistoryView;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case "rankings":
                vista.setRankings();
                ranking.updateTable(Database.getInfoRank());
                break;
            case "top5":
                vista.setTop5();
                top5.enableResize(true);
                break;
            case "blackJackGraph":
                generateGraph("b");
                break;
            case "horseGraph":
                generateGraph("h");
                break;
            case "rouletteGraph":
                generateGraph("r");
                break;
            case "returnMainView":
                vista.setMainView();
                top5.enableResize(false);
                break;
            //En el cas de apretar l'opcio de sortir desde la Tray
            case "trayButtonExit" :
                //Es surt del programa sense indicar cap error
                exitProgram(0);
                break;
        }
    }


    private void updateGraph() {
        if(top5.isResize()) generateGraph(top5.getLastGraphSelected());
    }

    private void generateGraph(String obj) {
        Graphics g =  top5.getGraphicsOfView();

        top5.setLastGraphSelected(obj);

        User[] user;
        switch (obj) {
            case "b":
                user = Database.getTop(Transaction.TRANSACTION_BLACKJACK);
                break;
            case "h":
                user = Database.getTop(Transaction.TRANSACTION_HORSES);
                break;
            default:
                    user = Database.getTop(Transaction.TRANSACTION_BLACKJACK);
        }

        String[] noms = new String[user.length];
        long maxWallet = maxWallet(user);
        long[] wallets = new long[user.length];
        int i = 0;
        for(User u : user){
            noms[i] = u.getUsername();
            wallets[i++] = u.getWallet();
        }

        switch (obj){
            case "b":
                top5.createGraph(g,new Color(92, 131, 47),"BlackJack",noms,maxWallet,wallets);
                break;
            case "h":
                top5.createGraph(g,new Color(166, 32, 49),"Horses",noms,maxWallet,wallets);
                break;
            case "r":
                top5.createGraph(g,new Color(56, 37, 19),"Roulette",noms,maxWallet,wallets);
                break;
        }

        g.dispose();
    }

    /**Donat un array d'usuaris, retorna el valor de la cartera més alta.
     * @param users Array dels usuaris existents en la base de dades
     * @return enter amb el valor de la cartera més alta*/
    private int maxWallet(User [] users){
        int max = 0;
        for(User u : users){
            if(u.getWallet() > max) max = (int)u.getWallet();
        }
        return max;
    }


    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void viewRankingView(){
        vista.setResizable(true);
        vista.setRankings();
        coinHistoryView.closeView();
        ranking.updateTable(Database.getInfoRank());
        System.gc();
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e) {
        //Es pregunta si es vol sortir de veritat del programa
        if(vista.displayQuestion("Want to exit?"))
            exitProgram(0);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    @Override
    public void componentResized(ComponentEvent e) {
        if(coinHistoryView != null)
            coinHistoryView.updateSize(false);
        if(top5 != null)
            updateGraph();

    }

    @Override
    public void componentMoved(ComponentEvent e) {
        if(coinHistoryView != null)
            coinHistoryView.updateSize(true);
        if(top5 != null)
            updateGraph();

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getSource() instanceof JTable && e.getClickCount() >= 2){
            vista.setCoinHistoryView(ranking.getUsername(), Database.getTransactions(ranking.getUsername()));
            coinHistoryView.updateSize(false);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
