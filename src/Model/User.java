package Model;

import Network.Message;
import java.util.ArrayList;

/** Usuari basic del casino*/

public class User extends Message {

    /** Defineix el identificador del missatge. Util per quan s'envia amb el networkManager*/
    private final double ID;

    /** Nom de l'usuari*/
    private String username;

    /** Password del usuari*/
    private String password;

    /** Indica si el servidor accepta el logIn de l'usuari*/
    private boolean credentialsOk;

    /** Indica si l'usuari desitja desconnectar-se del servidor*/
    private boolean online;

    /** Email de l'usuari*/
    private String mail;

    /** Diners del usuari*/
    private long wallet;

    /** Evolucio dels diners del usuari*/
    private ArrayList<Long> coinHistory;


    /**
     * Crea un usuari amb un nom i una password.
     * Aquest usuari se li adjudica un IdentificadorAleatori per a una millor comunicacio client - servidor.
     * El constructor es refereix a un usuari ja registrat prèviament.
     * @param name Username del usuari que es vol crear
     * @param password Password del usuari que es vol crear
     */
    public User(String name, String password) throws Exception {

        ID = Math.random();

        this.username = name;
        this.password = password;
        this.credentialsOk = false;

        Database.fillUser(this);
    }

    /**
     * Constructor per a un Usuari que s'acaba de registrar
     * @param username login i identificador de l'usuari (se suposa que no generarà col·lisió)
     * @param password contrassenya necessària per a realitzar el login
     * @param mail correu de contacte de l'usuari
     */
    public User(String username, String password, String mail) {

        coinHistory = new ArrayList<>();

        ID = Math.random();

        this.username = username;
        this.password = password;
        this.mail = mail;

        wallet = 0;
        long aux = wallet;
        coinHistory.add(aux);

        Database.insertNewUser(this);

        this.credentialsOk = false;
    }

    /** GETTERS I SETTERS */

    public boolean areCredentialsOk() {
        return credentialsOk;
    }

    public void setCredentialsOk(boolean credentialsOk) {
        this.credentialsOk = credentialsOk;
    }

    public double getID() {
        return ID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public long getWallet() {
        return wallet;
    }

    public void setWallet(long wallet) {
        this.wallet = wallet;
    }

    public ArrayList<Long> getCoinHistory() {
        return coinHistory;
    }

    public void setCoinHistory(ArrayList<Long> coinHistory) {
        this.coinHistory = coinHistory;
    }


    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
