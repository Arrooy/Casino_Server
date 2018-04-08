package Network;

import Controlador.Controller;
import Controlador.JsonManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class NetworkManager extends Thread{


    /** Controlador del sistema*/
    private Controller controller;

    /** Socket del servidor on tots els clients demanaran un servidorDedicat*/
    private ServerSocket serverSocket;

    /** Llistat d'usuaris connectats al servidor*/
    private ArrayList<Client> usuarisConnectats;

    /** Inicialitza el newtWorkManager i obre el port determinat al json de configuracio*/
    public NetworkManager(){
        controller = null;
        usuarisConnectats = new ArrayList<>();

        try {
            serverSocket = new ServerSocket((int)JsonManager.llegirJson("PortClients")[0]);
        } catch (IOException e) {
            controller.displayError("Error " + e.getLocalizedMessage(),e.getMessage());
        }
    }

    /** Inicia la acceptacio de nous usuaris*/
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
                System.out.println("[NEW CLIENT OBERT]\n[CLIENT COUNT]: " + usuarisConnectats.size());
                //Iniciem el client
                nouClient.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
