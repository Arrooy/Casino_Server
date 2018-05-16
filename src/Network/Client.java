package Network;

import Controlador.Controller;
import Controlador.HorseRaceThread;
import Model.*;
import Model.HorseRace_Model.HorseBet;
import Model.HorseRace_Model.HorseMessage;
import Model.RouletteModel.RouletteMessage;
import Model.RouletteModel.RouletteBetMessage;
import Network.Roulette.RouletteThread;
import Utils.Seguretat;
import Vista.Tray;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static Model.Casino_Server.OFF_LINE;

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
    /**
     * Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_BJ_FINISH_USER = "blackjackFinish";

    /** Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_TRANSACTION = "transaction";

    /** Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_GET_COINS = "userCoins";

    /** Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_WALLET_EVOLUTION = "walletEvolution";

    /** Constant per a contexualitzar els missatges entre client i servidor*/
    public static final String CONTEXT_CHANGE_PASSWORD = "change password";

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

    /** Valor de l'aposta del usuari*/
    private long userBet;

    private boolean connectedToRoulette;

    private boolean playingHorses;

    private RouletteThread rouletteThread;

    private HorseRaceThread horseRaceThread;

    /** Inicialitza un nou client.*/
    public Client(ArrayList<Client> usuarisConnectats, Socket socket, Controller controller, HorseRaceThread horseRaceThread, RouletteThread rouletteThread) {
        keepLooping = true;
        this.controller = controller;
        this.horseRaceThread = horseRaceThread;
        this.usuarisConnectats = usuarisConnectats;
        this.socket = socket;
        this.user = null;
        this.playingHorses = false;
        connectedToRoulette = false;
        this.rouletteThread = rouletteThread;


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
        HorseBet horseBet;
        try {
            while ((user == null || user.isOnline()) && keepLooping) {
                Message msg = (Message) ois.readObject();

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

                    case CONTEXT_TRANSACTION:
                        Database.registerTransaction((Transaction) msg);
                        break;

                    case CONTEXT_GET_COINS:
                        User user = (User) msg;
                        user.setWallet(Database.getUserWallet(this.user.getUsername()));
                        send(user);
                        break;

                    case CONTEXT_CHANGE_PASSWORD:
                        changePassword(msg);
                        break;

                    case "deposit":
                        deposit((Transaction) msg);
                        break;

                    case "HORSES-Connect":
                        if(!HorseRaceThread.isRacing()){
                            this.playingHorses =  true;
                            send(new HorseMessage(HorseRaceThread.getCountdown(),"Countdown" ));
                            System.out.println(HorseRaceThread.getCountdown());
                        }else{
                            HorseRaceThread.addPlayRequest(this);
                        }
                        break;

                    case "HORSES-Disconnect":
                        setPlayingHorses(false);
                        HorseRaceThread.removeBets(this.getName());
                        HorseRaceThread.removeRequests(this);
                        break;

                    case "HORSES-Bet":
                        horseBet = ((HorseMessage)msg).getHorseBet();
                        if(horseBet.getName().equals(this.user.getUsername())){
                            if(Database.getUserWallet(horseBet.getName()) >= ((HorseMessage)msg).getHorseBet().getBet()){
                                Database.registerTransaction(new Transaction("HorseBet", this.user.getUsername(), -((HorseMessage)msg).getHorseBet().getBet(), 1));
                                HorseRaceThread.addHorseBet(((HorseMessage)msg).getHorseBet());
                                send(new HorseMessage(new HorseBet(true), "HORSES-BetConfirm"));
                            }else{
                                send(new HorseMessage(new HorseBet(false), "HORSES-BetConfirm"));
                            }
                        }
                        break;

                    case "HORSES-Finished":
                        System.out.println("Finished ");
                        HorseRaceThread.addFinished();
                        this.horseRaceThread.sendResult(this);
                        break;

                    case "rouletteConnection":
                        ((RouletteMessage) msg).setTimeTillNext(RouletteThread.getTimeTillNext());
                        send(msg);
                        connectedToRoulette = true;
                        break;

                    case "rouletteDisconnection":
                        rouletteThread.cleanUserBets(this.user.getUsername());
                        connectedToRoulette = false;
                        break;

                    case "rouletteBet":
                        rouletteBet(msg);
                        break;

                    case CONTEXT_WALLET_EVOLUTION:
                        walletEvolutionResponse(msg);
                        break;

                    case "walletRequest":
                        ((User) msg).setWallet(Database.getUserWallet(this.user.getUsername()));
                        ((User) msg).setOnline(true);
                        send(msg);
                        break;
                }

            }
        } catch (Exception e) {
            //Usuari s'ha desconectat sense avisar al servidor
            Tray.showNotification("Usuari ha marxat inesperadament","una tragedia...");
            HorseRaceThread.removeBets(this.getName());

            if (connectedToRoulette) {
                rouletteThread.cleanUserBets(user.getUsername());
                connectedToRoulette = false;
            }

            HorseRaceThread.removeBets(this.getName());
            usuarisConnectats.remove(this);
        }
    }

    private void rouletteBet(Message msg) {
        RouletteBetMessage bet = (RouletteBetMessage) msg;
        bet.setSuccessful(false);

        try {
            if (Database.getUserWallet(user.getUsername()) - rouletteThread.getUserBet(user.getUsername()) > bet.getBet()) {
                bet.setSuccessful(true);
            }
        } catch (Exception e) {
            bet.setSuccessful(false);
            e.printStackTrace();
        }

        if (bet.isSuccessful() && RouletteThread.getTimeTillNext() - Timestamp.from(Instant.now()).getTime() > 3000)
            controller.getNetworkManager().getRouletteThread().addBet(user.getUsername(), bet.getBet(), bet.getCellID());
        else bet.setSuccessful(false);

        send(bet);
    }

    //Gestiona la solicitud del client per a canviar la password de l'usuari
    private void changePassword(Message msg) {

        User userPass = (User) msg;
        //Es modifica l'usuari per a indicar que esta connectat, de lo contrari, el client pensara que el servidor
        //vol la desconexio per part del client
        userPass.setOnline(true);

        try {
            //En el cas de ser una password valida, aquesta es modifica
            if (checkPassword((String) Seguretat.desencripta(userPass.getPassword()))) {

                Database.updateUser(userPass, true);
                //S'indica el correcte canvi de la password
                userPass.setCredentialsOk(true);

            } else {
                //S'inidica que la password no té el format adient
                userPass.setCredentialsOk(false);
            }

            //Es respon a la solicitud del client
            send(userPass);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Respon a la solicitud de l'evolucio monetaria de l'usuari
    private void walletEvolutionResponse(Message msg) {

        WalletEvolutionMessage wallet = (WalletEvolutionMessage)msg;

        //S'agafa el valor de la BBDD i es guarda a la cartera del client
        wallet.setTransactions(Database.getTransactions(user.getUsername()));

        //Es retorna al client la seva cartera
        send(wallet);
    }

    //Gestiona la solicitud d'ingres de diners
    private void deposit(Transaction transaction) {

        if(Seguretat.desencripta(user.getPassword()).equals(Seguretat.desencripta(transaction.getPassword()))){
            try {
                long wallet = Database.getUserWallet(transaction.getUsername());
                wallet += transaction.getGain();

                //Transaction ok indica si es possible fer l'ingres segons si s'ha passat el limit maxim
                transaction.setTransactionOk(wallet <= 100000);

                //Si la transaccio es correcte, es guarda a la base de dades el nou ingres
                if (transaction.isTransactionOk()) Database.registerTransaction(transaction);

                //Es retorna la solicitud al client
                send(transaction);

            } catch (Exception e) {
                //BBDD error
            }

        }else{
            transaction.setTransactionOk(false);
            //Al indicar type 5, el client interpreta que l'error esta en la password
            transaction.setType(5);
            send(transaction);
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
        user.setWallet(10000);

        //Es verifica l'user
        request.setCredentialsOk(true);

        //Es torna al clinet l'user amb la verificacio
        send(request);
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
        if (Database.usernamePicked(request.getUsername()) || Database.mailPicked(request.getMail())) {
            impossibleRegistrar  = true;
        } else {
            //Si no existeix el nom, s'intenta crear el nou usuari
            try {
                Database.insertNewUser(request);

                user = request;
                //Es verifica el nou usuari i es reenvia al client amb el mateix ID amb el que s'ha demanat el registre
                request.setCredentialsOk(true);
                Database.updateUser(request, true);
                send(request);
            } catch (Exception e) {
                impossibleRegistrar  = true;
                e.printStackTrace();
            }
        }
        //En el cas de no ser possible registrar al nou client, es nega el registre i es notifica al client.
        if (impossibleRegistrar ) {
            try {
                request.setCredentialsOk(false);
                send(request);
            } catch (Exception e) {

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
                send(user);
                Database.updateUser(user, true);

            } else {
                //Sino, es retornara el mateix missatge del client, que ja internament esta indicat que creedentiasOk = false;
                send(auxUser);
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

            Database.updateUser(request, false);

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
            send(request);

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

        //Es transforma el missatge a carta.
        Card carta = (Card)reading;

        long userBet = carta.getBet();

        try {
           long money = user.isGuest() ? user.getWallet() : Database.getUserWallet(user.getUsername());

           if(money < userBet || userBet < 10){
               carta.setBetOk(false);
           }else{
               this.userBet = userBet;
               carta.setWallet(money);
               carta.setBetOk(true);
           }
        } catch (Exception e) {
            e.printStackTrace();
        }

        baralla = new Stack<>();
        baralla.removeAllElements();

        valorUsuari = 0;
        valorIA = 0;

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
                if(OFF_LINE){
                    carta.setReverseName("back-red.png");
                }else{
                    carta.setReverseName(Database.getUserColor(user.getUsername()));
                }

                carta.setCardName(baralla.pop());
                carta.setValue(calculaValorBlackJackCard(carta.getCardName()));

                if(carta.getContext().equals(CONTEXT_BJ_FINISH_USER)){
                    carta.setForIA(true);
                    if(valorIA >= valorUsuari){
                        carta.setDerrota("user-instant");
                        acabaPartidaBlackJack(userBet * -1);
                    }else {
                        if (carta.getValue() == 11) {
                            if (valorIA + 11 <= 21) {
                                carta.setValue(11);
                                carta.setValent11(carta.getValent11() + 1);
                            } else {
                                carta.setValue(1);
                            }
                        }
                        valorIA += carta.getValue();
                        if (valorIA > 21) {
                            if (carta.getValent11() >= 1) {
                                carta.setValent11(carta.getValent11() - 1);
                                carta.setValue(carta.getValue() - 10);
                            } else {
                                carta.setDerrota("IA");
                                if(valorUsuari == 21 && numberOfUserCards == 2) {
                                    acabaPartidaBlackJack((long) (userBet * 1.5));
                                }else{
                                    acabaPartidaBlackJack(userBet * 2);
                                }
                            }
                        }else {
                            if (valorIA >= valorUsuari) {
                                carta.setDerrota("user");
                                acabaPartidaBlackJack(userBet * -1);
                            } else {
                                carta.setDerrota("false");
                            }
                        }
                        carta.setGirada(false);
                    }

                }else{
                    //Si una de les 4 cartes inicials es per a la IA, s'envia aquesta girada
                    if (carta.isForIA()){
                        if(carta.getValue() == 11){
                            if(valorIA + 11 <= 21)
                                carta.setValue(11);
                            else
                                carta.setValue(1);
                        }
                        valorIA += carta.getValue();
                        carta.setGirada(true);
                        carta.setDerrota("false");
                    }else {
                        numberOfUserCards++;
                        carta.setGirada(false);
                        if(carta.getValue() == 11) {
                            if (valorUsuari + 11 <= 21) {
                                carta.setValue(11);
                                carta.setValent11(carta.getValent11() + 1);
                            } else {
                                carta.setValue(1);
                            }
                        }

                        valorUsuari += carta.getValue();
                        if(valorUsuari > 21) {
                            if(carta.getValent11() >= 1){
                                carta.setValue(carta.getValue() - 10);
                                carta.setValent11(carta.getValent11() - 1);
                            }else{
                                carta.setDerrota("user");
                                acabaPartidaBlackJack(userBet * -1);
                            }
                        }else{
                            carta.setDerrota("false");
                        }
                    }
                }
                send(carta);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Acaba la partida del BJ i registra si sha perdut o guanyat
    private void acabaPartidaBlackJack(long money) {

        if(user.isGuest()){
            user.setWallet(user.getWallet() + money);
        }else{
            Timestamp time = Timestamp.from(Instant.now());
            Transaction finishGame = new Transaction(null,user.getUsername(),money,3);
            finishGame.setTime(time);
            Database.registerTransaction(finishGame);
        }
    }

    public void sendRouletteShot(RouletteMessage rouletteMessage) {
        if (connectedToRoulette) send(rouletteMessage);
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
    /**
     * Comprova que la contrasenya que reb com a parametre tingui el format correcte
     * @param password contrasenya a comprovar
     * @return boolea que indica si la contrasenya introduida es correcte
     */
    public boolean checkPassword(String password){
        char[] passwordChars = password.toCharArray();
        int numbers = 0;
        int lowerCase = 0;
        int upperCase = 0;
        int specialChar = 0;

        if(passwordChars.length >= 8){
            for (int i = 0; i < passwordChars.length; i++){
                if(Character.isDigit(passwordChars[i])) {
                    numbers++;
                }else{
                    if(Character.isLowerCase(passwordChars[i])){
                        lowerCase++;

                    }else{
                        if(Character.isUpperCase(passwordChars[i])){
                            upperCase++;
                        }else{
                            specialChar++;
                        }
                    }
                }
            }
            if(upperCase <= 0 || lowerCase <= 0 || numbers <= 0 || lowerCase + upperCase < 6 ){
                return false;
            }else{
                for(int i = 0; i < passwordChars.length; i++){
                    if(Character.isSpaceChar(passwordChars[i])){
                        return false;
                    }else if(!Character.isDefined(passwordChars[i])){
                        return false;
                    }
                }
                return true;
            }
        }else{
            return false;
        }
    }

    public User findUser(double id){
        double id_user;
        for(int i = this.usuarisConnectats.size() - 1; i >= 0; i--){
            id_user = this.usuarisConnectats.get(i).user.getID();
            if( id_user == id){
                return this.usuarisConnectats.get(i).user;
            }
        }
        return null;
    }

    public boolean isPlayingHorses(){
        return playingHorses;
    }


    public void send(Message message) {
       try {
           oos.writeObject(message);
       }catch(IOException e){
           System.out.println("Error sending message");
       }
    }

    public void setPlayingHorses(boolean b) {
        this.playingHorses = b;
    }

    public void sendRouletteList(String[][] info) {
        send(new BetList(info, BetList.ROULETTE));
    }

    /**
     *
     * @param betList Llista d'apostes de la carrera que s'esta corrent
     */
    public void sendHorseBetList(String[][] betList) {
        send(new BetList(betList, BetList.HORSES));
    }
}