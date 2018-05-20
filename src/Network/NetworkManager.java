package Network;

import Controlador.Controller;
import Controlador.HorseRaceThread;
import Model.HorseRace_Model.HorseRaceModel;
import Utils.JsonManager;
import Network.Roulette.RouletteThread;
import Vista.Tray;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * Classe que gestiona la comunicació amb els clients.
 * Concretament es dedica a esperar nous usuaris i redirigir-los a servidors
 * dedicats que atendràn de manera directa les peticions de cadascun.
 *
 * A més a més s'encarrega d'iniciar els fils d'execució dels dos jocs paral·lels
 * independents a la resta, que consisteixen en els cavalls i la ruleta.
 */
public class NetworkManager extends Thread{

    /** Controlador del sistema*/
    private Controller controller;

    /** Socket del servidor on tots els clients demanaran un servidorDedicat*/
    private ServerSocket serverSocket;

    /** Llistat d'usuaris connectats al servidor*/
    private ArrayList<Client> usuarisConnectats;

    /** Fil d'execució del joc dels cavalls */
    private HorseRaceThread horseRaceThread;

    /** Fil d'execució del joc de la ruleta */
    private RouletteThread rouletteThread;

    /** Inicialitza el newtWorkManager i obre el port determinat al json de configuracio*/
    public NetworkManager(){
        controller = null;
        usuarisConnectats = new ArrayList<>();

        try {
            serverSocket = new ServerSocket((int)JsonManager.llegirJson("PortClients")[0]);
        } catch (IOException e) {
            if (controller != null) controller.displayError("Error " + e.getLocalizedMessage(),e.getMessage());
        }

        rouletteThread = new RouletteThread(usuarisConnectats);
        this.horseRaceThread = new HorseRaceThread(new HorseRaceModel(), usuarisConnectats);
    }

    /** Inicia la acceptacio de nous usuaris*/
    public void initServer(Controller controller) {
        this.controller = controller;
        start();
    }

    /** Getter del Thread de la ruleta */
    public RouletteThread getRouletteThread() {
        return rouletteThread;
    }

    /**
     * Fil d'execució que es manté esperant mentre dura l'execució del programa, la connexió de nous
     * usuaris. Un cop són detectats, s'afegeixen a un llistat de clients i s'els obre un servidor
     * dedicat que escolti les seves peticions.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while(true){
            try {
                //Esperem i creem un client per a cada nova conexio entrant
                Client nouClient = new Client(usuarisConnectats,serverSocket.accept(),controller, horseRaceThread, rouletteThread);

                //Afegeim el client a la llista de clients
                usuarisConnectats.add(nouClient);
                Tray.showNotification("Nou client connectat","Total de clients actius: " + usuarisConnectats.size());
                //Iniciem el client
                nouClient.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
