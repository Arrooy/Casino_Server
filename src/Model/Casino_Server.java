package Model;

import Controlador.Controller;
import Network.NetworkManager;
import Vista.Finestra;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Classe principal del programa pel servidor. S'encarrega d'establir la connexió amb
 * la base de dades, iniciar la vista amb el seu respectiu controlador i iniciar
 * el gestor dels clients.
 *
 * @since 13/3/2018
 * @version 0.2
 */

public class Casino_Server {

    public static final boolean OFF_LINE = false;

    public static final int WELCOME_GIFT = 500;

    public static void main(String[] args) {

        //Iniciem connexió amb la base de dades
        try {
            //S'estableix la connexió amb la base de dades
            if(!OFF_LINE) Database.initBaseDades();

            // Es crea la vista del Servidor
            Finestra vista = new Finestra();

            //Es defineix el gestor de clients
            NetworkManager networkManager = new NetworkManager();

            //Es crea el controlador del sistema i es relacionen controlador amb vista i controlador amb el gestor
            Controller controlador = new Controller(vista,networkManager);


            //S'inicia el servidor i es crea l'enllaç gestor amb el controlador del sistema i del joc dels cavalls
            networkManager.initServer(controlador);

            //Es crea l'enllaç vista amb controlador
            vista.addController(controlador);

            //Es fa visible la finestra grafica
            vista.setVisible(true);

        } catch (Exception e) {

            if (e instanceof SQLException || e instanceof ClassNotFoundException) {
                JOptionPane.showMessageDialog(new JFrame(), "No s'ha pogut iniciar el servidor degut a un problema de connexió amb la base de dades",
                        "Error connexió Database", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }else e.printStackTrace();

        }
    }
}