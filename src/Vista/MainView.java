package Vista;

import Controlador.Controller;

import javax.swing.*;
import java.awt.*;

/**Classe que crea la vista del menu del servidor*/
public class MainView extends View {
    /**Boto que et porta al Ranking*/
    JButton jbRankingBalance;

    /**Boto que et porta al Top5*/
    JButton jbTop5;

   /**Constructor del panell que defineix on i com es col·loquen els elements*/
    public MainView(){
        //Es defineix el Layout
        this.setLayout(new BorderLayout());

        //Panell amb els botons de les opcions centrades al mig de la pantalla
        JPanel jpgblBotons = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        jbRankingBalance = new JButton("USERS RANKING & BALANCE");
        jbRankingBalance.setFocusable(false);
        jbTop5 = new JButton("TOP 5");
        jbTop5.setFocusable(false);

        //Marges dels botons
        c.insets = new Insets(0,0,0,20);

        //S'afegeixen els botons de les opcions

        //Posicio boto ranking
        c.gridy = 0;
        c.gridx = 0;
        jpgblBotons.add(jbRankingBalance, c);

        //Posicio boto top5
        c.gridx = 1;
        jpgblBotons.add(jbTop5, c);

        //S'afegeixen els botons al centre
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

    /**
     * Metode override que relaciona cada element amb el seu Listener, i per tant es relaciona la vista amb el controlador
     * @param c Controlador
     * */
    @Override
    public void addController(Controller c){
        jbRankingBalance.setActionCommand("rankings");
        jbTop5.setActionCommand("top5");

        jbTop5.addActionListener(c);
        jbTop5.addActionListener(c);
        jbRankingBalance.addActionListener(c);
    }

}
