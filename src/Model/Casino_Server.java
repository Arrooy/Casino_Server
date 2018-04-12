package Model;

import Controlador.Controller;
import Network.NetworkManager;
import Vista.CoinHistoryView;
import Vista.Finestra;
import Vista.MainView;
import Vista.Tray;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Classe principal del programa pel servidor. S'encarrega d'establir la connexió amb
 * la base de dades, iniciar la vista amb el seu respectiu controlador i iniciar
 * el gestor dels clients.
 *
 * @since 13/3/2018
 * @version 0.2
 */

public class Casino_Server {

    public static final boolean OFF_LINE = true;

    public static final int WELCOME_GIFT = 500;

    public static void main(String[] args) {


        /*JFrame frame = new JFrame();

        CoinHistoryView coinHistoryView = new CoinHistoryView();

        frame.add(coinHistoryView);
        frame.setSize(1280, 720);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        coinHistoryView.createCoinHistory("gg");*/

        //Iniciem connexió amb la base de dades
        try {
            //S'estableix la connexió amb la base de dades
            if(!OFF_LINE){
                Database.initBaseDades();
                Database.test();
            }


            /*User u = new User("miquelsaula", "1234", "miquelsaula@gmail.com");
            Database.deleteUser(u.getUsername());
            Database.insertNewUser(u);*/

            // Es crea la vista del Servidor
            Finestra vista = new Finestra();

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

        } catch (Exception e) {

            if (e instanceof SQLException || e instanceof ClassNotFoundException)
                JOptionPane.showMessageDialog(new JFrame(), "No s'ha pogut iniciar el servidor degut a un problema de connexió amb la base de dades",
                    "Error connexió Database", JOptionPane.ERROR_MESSAGE);
            else e.printStackTrace();

        }
    }
}