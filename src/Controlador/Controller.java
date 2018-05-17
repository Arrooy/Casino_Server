package Controlador;

import Model.Database;
import Model.HorseRace_Model.HorseRaceModel;
import Model.Transaction;
import Network.NetworkManager;
import Vista.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Controlador del servidor*/

public class Controller implements ActionListener, WindowListener, MouseListener, ComponentListener{

    /** Finestra grafica del servidor*/
    /** Finestra grafica del client*/
    private Finestra vista;
    private MainView mainView;
    private Top5OptionsView top5;
    private RankingView ranking;
    private CoinHistoryView coinHistoryView;
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

        switch (obj){
            case "b":
                top5.createGraph(g,Database.getTop(Transaction.TRANSACTION_BLACKJACK),new Color(92, 131, 47),"BlackJack");
                break;
            case "h":
                top5.createGraph(g,Database.getTop(Transaction.TRANSACTION_HORSES),Color.blue,"Horses");
                break;
            case "r":
                top5.createGraph(g,Database.getTop(Transaction.TRANSACTION_ROULETTE),Color.red,"Roulette");
                break;
        }

        g.dispose();
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void viewRankingView(){
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
