package Controlador;

import Model.Database;
import Network.NetworkManager;
import Vista.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/** Controlador del servidor*/

public class Controller implements WindowListener, ActionListener{

    /** Finestra grafica del servidor*/
    /** Finestra grafica del client*/
    private Finestra vista;
    private MainView mainView;
    private Top5OptionsView top5;
    private RankingView ranking;

    /** Gestor de la network*/
    private NetworkManager networkManager;

    /** Inicialitza el controlador del servidor*/
    public Controller(Finestra vista, NetworkManager networkManager){
        this.vista = vista;
        this.networkManager = networkManager;
    }

    /** Mostra un error amb una alerta al centre de la finestra grafica*/
    public void displayError(String title, String errorText){
        vista.displayError(title,errorText);
    }

    /** Metode per a tencar el servidor de forma segura.*/
    public void exitProgram(int status){
        Tray.exit();
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

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case "rankings":
                vista.setRankings();
                ranking.updateTable(Database.getInfoRank());
                break;
            case "top5":
                vista.setTop5();
                break;
            case "blackJackGraph":
                top5.setGraphContent(new JLabel("blackJackGraph"));
                break;
            case "horseGraph":
                top5.setGraphContent(new JLabel("horseGraph"));
                break;
            case "rouletteGraph":
                top5.setGraphContent(new JLabel("rouletteGraph"));
                break;
            case "returnMainView":
                vista.setMainView();
                break;
            //En el cas de apretar l'opcio de sortir desde la Tray
            case "trayButtonExit" :
                //Es surt del programa sense indicar cap error
                exitProgram(0);
            break;
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

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
}
