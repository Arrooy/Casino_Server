package Model;

import java.util.ArrayList;

/** Usuari basic del casino. TipoMoneda defineix el tipo de variable per les monedes ex: int,long,double... */

public class User {

    private String name;
    private String mail;
    private long wallet;
    private ArrayList<Long> coinEvolution;

    public User(String name, String mail, long wallet){
        this.name = name;
        this.mail = mail;
        this.wallet = wallet;
        coinEvolution = new ArrayList<>();
    }

    /** GETTERS I SETTERS */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

}
