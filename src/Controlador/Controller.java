package Controlador;

import Network.NetworkManager;
import Vista.MainView;
import Vista.Tray;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Controller implements WindowListener, ActionListener{

    /** Finestra grafica del servidor*/
    private MainView vista;

    /** Gestor de la network*/
    private NetworkManager networkManager;

    /** Inicialitza el controlador del servidor*/
    public Controller(MainView vista, NetworkManager networkManager){
        this.vista = vista;
        this.networkManager = networkManager;

    }

    public void displayError(String title, String errorText){
        vista.displayError(title,errorText);
    }

    public void exitProgram(int status){
        Tray.exit();
        System.exit(status);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case "trayButtonExit" :
                exitProgram(0);
            break;
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
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
