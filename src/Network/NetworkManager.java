package Network;

import Controlador.Controller;

public class NetworkManager {

    private Controller controlador;

    public NetworkManager(){
        controlador = null;
    }

    public void initServer(Controller controlador) {
        this.controlador = controlador;
    }
}
