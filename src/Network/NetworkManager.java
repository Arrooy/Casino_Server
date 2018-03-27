package Network;

import Controlador.Controller;
import Controlador.JsonManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class NetworkManager extends Thread{


    private Controller controller;
    private ServerSocket serverSocket;

    private ArrayList<Client> usuarisConnectats;

    public NetworkManager(){
        controller = null;
        usuarisConnectats = new ArrayList<>();

        try {
            serverSocket = new ServerSocket((int)JsonManager.llegirJson("PortClients")[0]);
        } catch (IOException e) {
            controller.displayError("Error " + e.getLocalizedMessage(),e.getMessage());
        }
    }

    public void initServer(Controller controller) {
        this.controller = controller;
        start();
    }

    @Override
    public void run() {
        while(true){
            try {

                //Esperem i creem un client per a cada nova conexio entrant
                Client nouClient = new Client(usuarisConnectats,serverSocket.accept(),controller);

                //Afegeim el client a la llista de clients
                usuarisConnectats.add(nouClient);

                //Iniciem el client
                nouClient.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
