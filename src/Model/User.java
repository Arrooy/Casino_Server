package Model;

import Network.Message;

import java.io.Serializable;
import java.util.ArrayList;

/** Usuari basic del casino*/

public class User extends Message implements Serializable {

    private final double ID;
    private String username;
    private String password;
    private boolean credentialsOk;
    private boolean online;

    private String mail;
    private long wallet;
    private ArrayList<Long> coinEvolution;

    public User(String name, String password) {

        coinEvolution = new ArrayList<>();
        ID = Math.random();

        this.username = name;
        this.password = password;
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

    public ArrayList<Long> getCoinEvolution() {
        return coinEvolution;
    }

    public void setCoinEvolution(ArrayList<Long> coinEvolution) {
        this.coinEvolution = coinEvolution;
    }


    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
