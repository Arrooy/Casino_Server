package Network;

import Controlador.Controller;
import Model.Card;
import Model.Database;
import Model.Transaction;
import Model.User;
import Vista.Tray;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

/**
 * ServidorDedicat a un client. Si el client vols jugar al blackJack, es aquesta clase qui gestiona la seva logica.
 * La classe client, a mes a mes, s'encarrega de gestionar el logIn, logOut i registre del usuari.
 */

public class Client extends Thread {

    /** Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_LOGIN = "login";

    /** Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_LOGIN_GUEST = "loginGuest";

    /** Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_LOGOUT = "logout";

    /** Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_SIGNUP = "signup";

    /** Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_BLACK_JACK = "blackjack";

    /** Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_BLACK_JACK_INIT = "blackjackinit";

    /** Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_BJ_IA = "blackjackIA";
    /**
     * Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_BJ_FINISH_USER = "blackjackFinish";

    /* Constant per a contextualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_TRANSACTION = "transaction";
    public static final String CONTEXT_GET_COINS = "userCoins";


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

    /** Inidica si s'ha de seguir en el loop del thread o no*/
    private boolean keepLooping;

    /** Baralla de cartes per al joc BlackJack*/
    private Stack<String> baralla;

    /** Nombre de cartes de l'usuari que esta jugant al BlackJack. Com a maxim aquest pot tenir 12*/
    private int numberOfUserCards;

    /** Valor de les cartes de l'usuari*/
    private int valorUsuari;

    /** Valor de les cartes de l'usuari*/
    private int valorIA;

    private boolean isIATurn;




    /** Inicialitza un nou client.*/
    public Client(ArrayList<Client> usuarisConnectats, Socket socket, Controller controller) {
        keepLooping = true;

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
        while ((user == null || user.isOnline()) && keepLooping) {
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
                    case CONTEXT_BJ_FINISH_USER:
                        blackJack(msg);
                        break;
                    case CONTEXT_LOGOUT:
                        logOut(msg);
                        break;
                    case CONTEXT_LOGIN_GUEST:
                        logInGuest(msg);
                        break;
                    case CONTEXT_GET_COINS:
                        //TODO revisar MERI
                        User user = (User) msg;
                        LinkedList<String> info = Database.getUserInfo(user.getUsername());
                        oos.writeLong(Long.parseLong(info.get(2)));
                        break;
                    case CONTEXT_TRANSACTION:
                        //TODO revisar MERI
                        Database.registerTransaction((Transaction) msg);
                        break;
                        default:
                            System.out.println("ERROR BUCLE !!!!!!!!!! \nCONTEXT NOT FOUND");

                }
                //TODO: FICAR EXCEPCIONS CONCRETES AMB SOLUCIONS UTILS
            } catch (Exception e) {
                Tray.showNotification("Usuari ha marxat inesperadament","una tragedia...");
                usuarisConnectats.remove(this);
                //Si es surt sense fer logIn, peta el read.
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * Gestiona el logIn d'un guest. Guarda una copia del logIn i el retorna amb les creedencials verificades
     * @param msg missatge del client que conte un usuari tipo guest.
     */

    private void logInGuest(Message msg) {
        //Es transforma el missatge en user
        User request = (User) msg;

        //Es guarda el user
        user = request;

        //Es verifica l'user
        request.setCredentialsOk(true);
        try {
            //Es torna al clinet l'user amb la verificacio
            oos.writeObject(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Permet al client registrar-se al servidor.
     * @param msg missatge del client que conte un usuari inflat amb les noves dades de la persona que vol
     * fer el registre.
     */

    private void signUp(Message msg) {

        //Es tradueix el missatge a usuari
        User request = (User) msg;
        //En el cas de sorgir algun error, impossibleRegistrar s'encarrega de enviar que no s'ha pogut fer el registre.
        boolean impossibleRegistrar = false;

        //En el cas de trobar una persona amb el mateix username que es vol registrar, es nega el registre al client.
        if (Database.usernamePicked(request.getUsername())) {
            impossibleRegistrar  = true;
        } else {
            //Si no existeix el nom, s'intenta crear el nou usuari
            try {
                Database.insertNewUser(request);
                //Es verifica el nou usuari i es reenvia al client amb el mateix ID amb el que s'ha demanat el registre
                request.setCredentialsOk(true);
                oos.writeObject(request);
            } catch (Exception e) {
                impossibleRegistrar  = true;
                e.printStackTrace();
            }
        }
        //En el cas de no ser possible registrar al nou client, es nega el registre i es notifica al client.
        if (impossibleRegistrar ) {
            try {
                request.setCredentialsOk(false);
                oos.writeObject(request);
            } catch (Exception e) {
                System.out.println("No s'ha pogut retornar la peticio de signup");
            }
        }
    }

    /**
     * Gestiona el logIn d'un usuari ja resgistrat al sistema.
     * @param reading usuari amb les creedencials que volen entrar al sistema
     */
    private void logIn(Message reading) {

        try {
            //Es tradueix el missatge a un user on es troben les creedencials
            User auxUser = (User) reading;



            //Es verifica l'existencia del usuari a la base de dades
            if (Database.checkUserLogIn(auxUser).areCredentialsOk()) {
                //Si tot es correcte, auxUser s'haura omplert amb creedentialsOk = true;

                user = auxUser;
                oos.writeObject(user);
            } else {
                //Sino, es retornara el mateix missatge del client, que ja internament esta indicat que creedentiasOk = false;
                oos.writeObject(auxUser);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestiona el logOut del client
     * @param msg user amb l'indicador online a false
     */
    private void logOut(Message msg) {
        try {
            //Es tradueix el missatge a usuari
            User request = (User) msg;

            //S'indica que s'ha desconectat un user
            Tray.showNotification("Usuari desconectat " + user.getUsername(),"Total de clients actius: " + (usuarisConnectats.size() - 1));

            //Modifiquem el setOnline per sortir de bucle infinit en el thread.
            if(user != null){
                user.setOnline(false);
            }

            //S'encarrega de sortir del bucle infinit en el cas de que user == null
            keepLooping = false;

            //S'inidca online false per verificar que ja es pot de desconectar el client
            request.setOnline(false);
            request.setContext(CONTEXT_LOGOUT);

            //Es retorna el missatge
            oos.writeObject(request);

            //Es tanca el socket i s'elimina l'usuari de la llista d'usuaris
            socket.close();
            usuarisConnectats.remove(this);
        } catch (Exception e) {
            System.out.println("Impossible desconectarse per les bones.");
        }
    }

    /**
     * Inicialitza una partida del joc blackJack. Creant una nova baralla de cartes i barrejant-les
     * @param reading Carta que conte la baralla al seu interior. Tambe es la primera carta del usuari.
     */

    private void blackJackInit(Message reading) {


        //TODO: Verificar 54 cartes
        //Es transforma el missatge a carta.
        Card carta = (Card)reading;

        baralla = new Stack<>();

        valorUsuari = 0;
        valorIA = 0;
        isIATurn = false;
        System.out.println("RESTARTING BJ!");

        //Es reinicia el nombre maxim de cartes d'una persona
        numberOfUserCards = 0;

        //Es copia la baralla
        baralla = carta.getNomCartes();

        //Es barreja la baralla
        Collections.shuffle(baralla);

        //S'afegeix la carta al joc
        blackJack(carta);
    }

    /**
     * Reparteix una carta al usuari o a la IA del blackJack.
     * @param reading Solicitud de carta buida. El servidor en aquest metode omple la carta amb la proxima carta de la baralla.
     */

    private void blackJack(Message reading){
        //Es transforma el missatge en carta
        Card carta = (Card)reading;

        try{
            //Si la baralla no esta buida o si el nombre de cartes que ha demanat l'usuari es menor de 12
            if(!baralla.isEmpty() && numberOfUserCards <= 12) {

                //Omplim la carta amb les dades necesaries
                carta.setReverseName(Database.getUserColor(user.getUsername()));
                carta.setCardName(baralla.pop());
                carta.setValue(calculaValorBlackJackCard(carta.getCardName()));
                carta.setGirada(isIATurn);

                //Si la carta es per a la IA, s'envia la carta girada
                if (carta.isForIA()){
                    valorIA += carta.getValue();
                    carta.setDerrota("false");
                    if(valorIA > 21){
                        carta.setDerrota("IA");
                        System.out.println("IA DONE");
                    }else{
                        if(valorIA > valorUsuari){
                            carta.setDerrota("user");
                            System.out.println("IA WON GAME");
                        }
                    }
                }else {
                    numberOfUserCards++;
                    valorUsuari += carta.getValue();
                    System.out.println("Valor user " + valorUsuari);
                    if(valorUsuari > 21) {
                        carta.setDerrota("user");
                        System.out.println("USER LOST");
                    }else{
                        carta.setDerrota("false");
                    }
                }
                oos.writeObject(carta);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Calucla el valor d'una carta del blackJack
     * @param cardName El nom de la carta de la que es vol calcular el valor
     * @return Un numero del 1 a 11. Sent valors correctes del 1 - 10 i el 11 es l'identificador de un A
     */
    private int calculaValorBlackJackCard(String cardName) {

        //Si al carta no es un numero o es un 10, es retorna el valor 10
        if(cardName.contains("king") || cardName.contains("queen") || cardName.contains("jack") || cardName.charAt(0) == '1'){
            return 10;
            //Si la carta es un as, es torna l'identificador d'as
        }else if(cardName.charAt(0) == 'a') {
            return 11;
        }else{
            //En el cas contrari, es retorna l'atoi del primer numero de la carta
            return Integer.parseInt(cardName.substring(0,1));
        }
    }
}