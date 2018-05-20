package Vista;

import Controlador.Controller;
import Model.Transaction;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;

/**
 * Finestra principal del programa del servidor. Aquesta mostra
 * en un inici dues opcions, una per visualitzar el rànking de usuaris
 * ordenats de menys a més diners, i seguidament es veu un botó per accedir
 * als Top 5 d'usuaris que han guanyat més diners en jocs concrets.
 *
 * Dins el rànking, si es selecciona un usuari, es pot visualitzar la seva evolució
 * temporal en quant a diners, i en els Top 5, es pot seleccionar quina gràfica visualitzar
 * en tot moment.
 */
public class Finestra extends JFrame {

    /** Cardlayout que conte tots els JPanels de la finestra*/
    private CardLayout layout;

    /** Panell amb la vista que apareix al obrir el server*/
    private MainView mainView;

    /** Panell que conte les grafiques del Top5*/
    private Top5OptionsView top5;

    /** Panell que conte la JTable del ranking*/
    private RankingView ranking;

    /** Panell on es representa la funcio del coinHistory*/
    private CoinHistoryView coinHistoryView;

    /** Crea un nou JFrame amb un tray icon i una imatge d'icona. Tambe es crea el cardLayout i es configura la finestra*/
    public Finestra() {


        //Inicialitza la tray
        Tray.init();

        //Es carrega i es configura l'icono del programa
        try{
            setIconImage(ImageIO.read(new File("./data/ico.png")));
        }catch (Exception e){
            e.printStackTrace();
        }

        layout = new CardLayout();
        getContentPane().setLayout(layout);

        //Es crean les vistes
        mainView = new MainView();
        top5 = new Top5OptionsView();
        ranking = new RankingView();
        coinHistoryView = new CoinHistoryView();

        //S'afegeixen al cardLayout
        add("main", mainView);
        add("top5", top5);
        add("ranking", ranking);
        add("coinHistory", coinHistoryView);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(640, 480);

        //Es centra la finestra en el centre de la pantalla
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getSize().height / 2);
        setTitle("Casino Servidor");

    }

    /**
     * Afegeix el controlador de la finestra
     * @param c controlador que gestiona la finestra
     */
    public void addController(Controller c) {
        Tray.addController(c);

        mainView.addController(c);
        top5.addController(c);
        ranking.addController(c);
        coinHistoryView.addController(c);

        c.setMainView(mainView);
        c.setLogInView(top5);
        c.setRankingView(ranking);
        c.setCoinHistoryView(coinHistoryView);

        addWindowListener(c);

    }

    /** Obra una finestra per indicar un error*/
    public void displayError(String title,String errorText) {
        JOptionPane.showMessageDialog(this,title,errorText,JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Mostra un dialeg al usuari, preguntant de manera binaria (si/no) un message
     * @param message missatge que es vol preguntar a l'usuari
     * @return retorna true en cas afirmatiu, no en cas negatiu
     */
    public boolean displayQuestion(String message) {
        return JOptionPane.showConfirmDialog(this,message,"Are you sure?",JOptionPane.YES_NO_OPTION) == 0;
    }

    /** Mostra la vista del CoinHistoryView*/
    public void setCoinHistoryView(String username, LinkedList<Transaction> transactions) {
        setPreferredSize(getSize());
        setResizable(false);
        coinHistoryView.createCoinHistory(username, getWidth(), getHeight(), transactions);
        layout.show(getContentPane(), "coinHistory");
    }
    /** Mostra la main view*/
    public void setMainView() {
        layout.show(getContentPane(), "main");
    }

    /** Mostra el panell del top5*/
    public void setTop5() {
        layout.show(getContentPane(), "top5");
    }

    /** Mostra els rankings*/
    public void setRankings() { layout.show(getContentPane(), "ranking"); }
}
