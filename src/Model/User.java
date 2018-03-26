package Model;

import java.util.ArrayList;

/** Usuari basic del casino. TipoMoneda defineix el tipo de variable per les monedes ex: int,long,double... */

public class User <TipoMoneda>{

    private String name;
    private String mail;
    private TipoMoneda wallet;
    private ArrayList<TipoMoneda> coinEvolution;

    public User(String name, String mail, TipoMoneda wallet){
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

    public TipoMoneda getWallet() {
        return wallet;
    }

    public void setWallet(TipoMoneda wallet) {
        this.wallet = wallet;
    }

    public ArrayList<TipoMoneda> getCoinEvolution() {
        return coinEvolution;
    }

    public void setCoinEvolution(ArrayList<TipoMoneda> coinEvolution) {
        this.coinEvolution = coinEvolution;
    }

}
