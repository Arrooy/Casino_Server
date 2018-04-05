package Vista;

import Controlador.Controller;

import javax.swing.*;
import java.awt.*;

public class Top5OptionsView extends View {
    private JButton jbMenu;
    private JButton jbHorseRace;
    private JButton jbBlackJack;
    private JButton jbRoulette;

    public Top5OptionsView(){
        this.setLayout(new BorderLayout());

        //Panell per col·locar el botó Menu a la part baixa a l'esquerra
        JPanel jpgblBack = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        //Marges
        c.insets = new Insets(20,20,20,0);
        c.fill = GridBagConstraints.BOTH;
        jbMenu = new JButton("MENU");
        jbMenu.setFocusable(false);
        jpgblBack.add(jbMenu, c);
        //Flow Layout per a que el botó quedi a l'esquerra
        JPanel jpBack = new JPanel(new FlowLayout(FlowLayout.LEADING));
        jpBack.add(jpgblBack);
        this.add(jpBack, BorderLayout.SOUTH);

        //Panell que té el títol de la pantalla a dalt a la dreta al mig
        JPanel jpTitle = new JPanel();
        JPanel jpgblTitle = new JPanel(new GridBagLayout());
        JLabel jlTitle = new JLabel("TOP 5");
        jlTitle.setFont(new Font("ArialBlack", Font.BOLD, 24));
        //Marges
        c.insets = new Insets(20,0,0,0);
        jpgblTitle.add(jlTitle, c);
        jpTitle.add(jpgblTitle);
        this.add(jpTitle, BorderLayout.NORTH);

        //Panell amb els botons dels jocs centrats al mig de la pantalla
        JPanel jpgblBotons = new JPanel(new GridBagLayout());
        jbBlackJack = new JButton("BLACKJACK");
        jbBlackJack.setFocusable(false);
        jbHorseRace = new JButton("HORSE RACE");
        jbHorseRace.setFocusable(false);
        jbRoulette = new JButton("ROULETTE");
        jbRoulette.setFocusable(false);

        //Marges
        c.insets = new Insets(0,0,0,20);

        //S'afegeixen els botons dels jocs
        c.gridy = 0;
        c.gridx = 0;
        jpgblBotons.add(jbBlackJack, c);

        c.gridx = 1;
        jpgblBotons.add(jbHorseRace, c);

        c.gridx = 2;
        c.insets = new Insets(0,0,0,0);
        jpgblBotons.add(jbRoulette, c);

        this.add(jpgblBotons, BorderLayout.CENTER);
    }
    @Override
    public void addController(Controller c){

    }
}
