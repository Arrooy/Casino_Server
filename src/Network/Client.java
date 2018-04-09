package Network;

import Controlador.Controller;
import Model.Card;
import Model.Database;
import Model.User;
import Vista.Tray;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

/** ServidorDedicat a un client*/

public class Client extends Thread {

    public static final String CONTEXT_LOGIN = "login";
    public static final String CONTEXT_LOGOUT = "logout";
    public static final String CONTEXT_SIGNUP = "signup";
    public static final String CONTEXT_BLACK_JACK = "blackjack";
    public static final String CONTEXT_BLACK_JACK_INIT = "blackjackinit";

    /** Controlador del sistema*/
    private Controller controller;

    /** Llistat d'usuaris connectats al servidor*/
    private ArrayList<Client> usuarisConnectats;

    /** Socket connectat al client*/
    private Socket socket;

    /** La persona amb la que tracta el client*/
    private User user;

    /** Canals de entrada / sortida d'objectes servidorDedicat*/
    private ObjectOutputStream oos;
    private ObjectInputStream ois;


    private Stack<String> baralla;

    /** Inicialitza un nou client.*/
    public Client(ArrayList<Client> usuarisConnectats, Socket socket, Controller controller) {

        baralla = new Stack<>();

        this.controller = controller;
        this.usuarisConnectats = usuarisConnectats;
        this.socket = socket;
        this.user = null;

        //S'intentan guardar les referencies dels streams d'entrada i sortida del socket
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (user == null || user.isOnline()) {
            try {
                System.out.println("[DEBUG]: Reading next message now");
                Message msg = (Message) ois.readObject();
                System.out.println("[DEBUG]: New Message: " + msg.getContext());
                switch (msg.getContext()) {
                    case CONTEXT_LOGIN:
                        logIn(msg);
                        break;
                    case CONTEXT_SIGNUP:
                        signUp(msg);
                        break;
                    case CONTEXT_BLACK_JACK_INIT:
                        blackJackInit(msg);
                        break;
                    case CONTEXT_BLACK_JACK:
                        blackJack(msg);
                        break;
                    case CONTEXT_LOGOUT:
                        logOut();
                        break;
                }
                //TODO: FICAR EXCEPCIONS CONCRETES AMB SOLUCIONS UTILS
            } catch (Exception e) {
                Tray.showNotification("Usuari ha marxat inesperadament","una tragedia...");
                usuarisConnectats.remove(this);
                break;
            }
        }
    }

    private void signUp(Message msg) {

        User request = (User) msg;
        boolean b = false;

        if (Database.usernamePicked(request.getUsername())) {
            b = true;
        } else {
            try {
                Database.insertNewUser(request);
                request.setCredentialsOk(true);
                oos.writeObject(request);
            } catch (Exception e) {
                b = true;
            }
        }

        if (b) {
            try {
                request.setCredentialsOk(false);
                oos.writeObject(request);
            } catch (Exception e) {
                System.out.println("No s'ha pogut retornar la peticio de signup");
            }
        }
    }

    private void logIn(Message reading) {
        try {
            //El user vol entrar les creedencials
            User auxUser = (User) reading;

            System.out.println(auxUser.getUsername() + " is online.");
            auxUser.setCredentialsOk(true);
            user = auxUser;
            oos.writeObject(user);
            /*
            if (Database.checkUserLogIn(auxUser).areCredentialsOk()) {
                System.out.println("Creedencials ok");
                user = auxUser;
                oos.writeObject(user);
            } else {
                System.out.println("Creedencials WRONG");
                System.out.println(auxUser.getUsername());
                System.out.println(auxUser.getPassword());
                oos.writeObject(auxUser);
            }
            */
        }catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private void logOut() {
        try {
            Tray.showNotification("Usuari desconectat","Total de clients actius: " + (usuarisConnectats.size() - 1));
            if(user == null) user = new User("","",CONTEXT_LOGOUT);
            user.setOnline(false);
            user.setContext(CONTEXT_LOGOUT);
            usuarisConnectats.remove(this);
            oos.writeObject(user);
            socket.close();
        } catch (Exception e) {
            System.out.println("Impossible desconectarse per les bones.");
        }
    }

    private void blackJackInit(Message reading) {
        Card carta = (Card)reading;

        baralla.clear();
        baralla = carta.getNomCartes();
        System.out.println(Arrays.toString(baralla.toArray()));
        Collections.shuffle(baralla);
        blackJack(carta);
    }

    private void blackJack(Message reading){

        Card carta = (Card)reading;

        try{
            if(!baralla.isEmpty()) {
                carta.setReverseName("back-red.png"); //TODO: DB GET REVERSE FROM USER
                carta.setCardName(baralla.pop());
                carta.setValue(calculaValorBlackJackCard(carta.getCardName()));
                if (carta.isForIA())
                    carta.setGirada(true);
                System.out.println("Sending carta: " + carta.getCardName());
                oos.writeObject(carta);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int calculaValorBlackJackCard(String cardName) {

        if(cardName.contains("king") || cardName.contains("queen") || cardName.contains("jack") || cardName.charAt(0) == '1'){
            return 10;
        }else if(cardName.charAt(0) == 'a') {
            return 1;
        }else{
            return Integer.parseInt(cardName.substring(0,1));
        }
    }
}
