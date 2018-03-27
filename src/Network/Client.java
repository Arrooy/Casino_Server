package Network;

import Controlador.Controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends Thread {

    private Controller controller;

    private ArrayList<Client> usuarisConnectats;
    private Socket socket;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public Client(ArrayList<Client> usuarisConnectats, Socket socket, Controller controller) {

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
        while(true){
            try{

            }catch (Exception e){

            }
        }
    }

    public void disconnectMe(){
        usuarisConnectats.remove(this);
        controller.exitProgram(0);
    }
}
