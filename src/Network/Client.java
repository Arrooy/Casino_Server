package Network;

import Controlador.Controller;
import Model.BaseDades;
import Model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends Thread {

    private Controller controller;

    private ArrayList<Client> usuarisConnectats;
    private Socket socket;
    /** La persona amb la que tracta el client*/
    private User user;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;


    public Client(ArrayList<Client> usuarisConnectats, Socket socket, Controller controller) {
        System.out.println("New client in");

        this.controller = controller;
        this.usuarisConnectats = usuarisConnectats;
        this.socket = socket;

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("IniciBucle START");
        while (user == null || user.isOnline()) {
            try {
                Message reading = (Message) ois.readObject();
                if (user == null) {
                    //El user vol entrar les creedencials
                    User auxUser = (User) reading;
                    if (BaseDades.checkUserLogIn(auxUser).areCredentialsOk()) {
                        System.out.println("Usuari correcte: " + auxUser.getID());
                        user = auxUser;
                        oos.writeObject(user);
                    } else {
                        oos.writeObject(auxUser);
                    }
                } else {
                    //El user ja esta registrat
                    if(reading instanceof User) {
                        if (((User) reading).isOnline()) {
                            //Alguna comanda relacionada amb l'user

                        } else {
                            System.out.println("Bye!");
                            user.setOnline(false);
                            oos.writeObject(user);
                            disconnectMe();
                        }
                    }
                }
            }catch (Exception e) {

            }
        }
        System.out.println("FI BUCLE START");
    }

    public void disconnectMe(){
        usuarisConnectats.remove(this);
    }
}
