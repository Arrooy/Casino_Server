package Model;

import Controlador.Controller;
import Network.NetworkManager;
import Vista.MainView;

import java.util.LinkedList;

public class Casino_Server {
    public static void main(String[] args) {

        //Iniciem connexió amb la base de dades
        BaseDades.initBaseDades();

        // Es crea la vista del Servidor
        MainView vista = new MainView(640,480);

        //Es defineix el gestor de clients
        NetworkManager networkManager = new NetworkManager();

        //Es crea el controlador del sistema i es relacionen controlador amb vista i controlador amb el gestor
        Controller controlador = new Controller(vista,networkManager);

        //S'inicia el servidor i es crea l'enllaç gestor amb controlador
        networkManager.initServer(controlador);

        //Es crea l'enllaç vista amb controlador
        vista.addController(controlador);

        //Es fa visible la finestra grafica
        vista.setVisible(true);
    }
}