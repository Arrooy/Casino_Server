package Model;

import Controlador.Controller;
import Network.NetworkManager;
import Vista.MainView;

public class Casino_Server {
    public static void main(String[] args) {

        MainView vista = new MainView(640,480);

        NetworkManager networkManager = new NetworkManager();

        Controller controlador = new Controller(vista,networkManager);

        networkManager.initServer(controlador);

        vista.addController(controlador);

        vista.setVisible(true);
    }
}
