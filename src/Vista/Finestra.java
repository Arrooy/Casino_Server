package Vista;

import Controlador.Controller;
import Model.Transaction;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;

//TODO: comentar
public class Finestra extends JFrame {

    private CardLayout layout;
    private MainView mainView;
    private Top5OptionsView top5;
    private RankingView ranking;
    private CoinHistoryView coinHistoryView;

    public Finestra() {

        Tray.init();

        try{
            setIconImage(ImageIO.read(new File("./data/ico.png")));
        }catch (Exception e){
            e.printStackTrace();
        }


        layout = new CardLayout();
        getContentPane().setLayout(layout);

        mainView = new MainView();
        top5 = new Top5OptionsView();
        ranking = new RankingView();
        coinHistoryView = new CoinHistoryView();

        add("main", mainView);
        add("top5", top5);
        add("ranking", ranking);
        add("coinHistory", coinHistoryView);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(640, 480);

        //Es centra la finestra en el centre de la pantalla
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getSize().height / 2);
        setTitle("Casino_Servidor");

    }

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

    public void setMainView() {
        layout.show(getContentPane(), "main");
    }

    public void setTop5() {
        layout.show(getContentPane(), "top5");
    }

    public void setRankings() { layout.show(getContentPane(), "ranking"); }

    /** Obra una finestra indicant un error*/
    public void displayError(String title,String errorText) {
        JOptionPane.showMessageDialog(this,title,errorText,JOptionPane.ERROR_MESSAGE);
    }

    public boolean displayQuestion(String message) {
        //Retorna true si
        //Retorn false no
        return JOptionPane.showConfirmDialog(this,message,"Are you sure?",JOptionPane.YES_NO_OPTION) == 0;
    }

    public void setCoinHistoryView(String username, LinkedList<Transaction> transactions) {
        setPreferredSize(getSize());
        setResizable(false);
        coinHistoryView.createCoinHistory(username, getWidth(), getHeight(), transactions);
        layout.show(getContentPane(), "coinHistory");
    }

    public void closeCoinHistory() {
        coinHistoryView.closeView();
    }
}
