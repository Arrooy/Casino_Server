package Vista;

import Controlador.Controller;

import javax.swing.*;
import java.awt.*;

public class MainView extends View {

    JButton jbRankingBalance;
    JButton jbTop5;

    /**
     *  Crea la vista del servidor amb una amplada i una alçada determinades per width i height
     * @param width indica l'amplada de la vista
     * @param height indica l'altura de la vista
     */

    public MainView(){

        Tray.init();

        this.setLayout(new BorderLayout());

        //Panell amb els botons de les opcions centrades al mig de la pantalla
        JPanel jpgblBotons = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        jbRankingBalance = new JButton("USERS RANKING & BALANCE");
        jbRankingBalance.setFocusable(false);
        jbTop5 = new JButton("TOP 5");
        jbTop5.setFocusable(false);

        //Marges
        c.insets = new Insets(0,0,0,20);

        //S'afegeixen els botons de les opcions
        c.gridy = 0;
        c.gridx = 0;
        jpgblBotons.add(jbRankingBalance, c);

        c.gridx = 1;
        jpgblBotons.add(jbTop5, c);

        this.add(jpgblBotons, BorderLayout.CENTER);

        //Panell amb el titol centrat al mig
        JPanel jpTitle = new JPanel();
        JPanel jpgblTitle = new JPanel(new GridBagLayout());
        JLabel jlTitle = new JLabel("MENU");
        jlTitle.setFont(new Font("ArialBlack", Font.BOLD, 24));
        c.insets = new Insets(20,0,0,0);
        jpgblTitle.add(jlTitle, c);
        jpTitle.add(jpgblTitle);
        this.add(jpTitle, BorderLayout.NORTH);
    }

    /** Afegeix el controlador del programa a la vista*/
    @Override
    public void addController(Controller c){

    }

}
