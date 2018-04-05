package Network;

import Controlador.Controller;
import Model.Database;
import Model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/** ServidorDedicat a un client*/

public class Client extends Thread {

    public static final String CONTEXT_LOGIN = "login";
    public static final String CONTEXT_LOGOUT = "logout";
    public static final String CONTEXT_SIGNUP = "signup";

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

    /** Inicialitza un nou client.*/
    public Client(ArrayList<Client> usuarisConnectats, Socket socket, Controller controller) {

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
                Message msg = (Message) ois.readObject();

                switch (msg.getContext()) {
                    case CONTEXT_LOGIN:
                        logIn(msg);
                        break;
                    case CONTEXT_SIGNUP:
                        signUp(msg);
                        break;

                }
            } catch (Exception e) {

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

            System.out.println("Is online: " + auxUser.isOnline());

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
        }catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private void logOut(Message reading) {
        try {
            //El user ja esta registrat
            if (((User) reading).isOnline()) {
                //Alguna comanda relacionada amb l'user

            } else {
                System.out.println("apagan2");
                user.setOnline(false);
                user.setContext("logout");
                oos.writeObject(user);
                disconnectMe();
                socket.close();
            }
        } catch (Exception e) {

        }
    }

    /** Desconnecta al usuari*/
    public void disconnectMe(){
        usuarisConnectats.remove(this);
    }
}
