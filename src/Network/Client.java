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

                Message reading = (Message) ois.readObject();

                if (user == null) {
                    //El user vol entrar les creedencials
                    User auxUser = (User) reading;
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
                } else {
                    //El user ja esta registrat
                    if(reading instanceof User) {
                        if (((User) reading).isOnline()) {
                            //Alguna comanda relacionada amb l'user

                        } else {
                            user.setOnline(false);
                            oos.writeObject(user);
                            disconnectMe();
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** Desconnecta al usuari*/
    public void disconnectMe(){
        usuarisConnectats.remove(this);
    }
}
