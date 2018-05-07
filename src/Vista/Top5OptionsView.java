package Vista;

import Controlador.Controller;
import Model.Database;
import Model.Transaction;
import Model.User;

import javax.swing.*;
import java.awt.*;

public class Top5OptionsView extends View {

    private JButton jbMenu;

    private JButton jbHorseRace;
    private JButton jbBlackJack;
    private JButton jbRoulette;

    private JPanel jpGraphicContainer;

    private String lastGraphSelected;
    private boolean resize;

    public Top5OptionsView(){
        this.setLayout(new BorderLayout());

        lastGraphSelected = "b";
        resize = false;
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

        /*//Panell que té el títol de la pantalla a dalt a la dreta al mig
        JPanel jpTitle = new JPanel();
        JPanel jpgblTitle = new JPanel(new GridBagLayout());
        JLabel jlTitle = new JLabel("TOP 5");
        jlTitle.setFont(new Font("ArialBlack", Font.BOLD, 24));
        //Marges
        c.insets = new Insets(20,0,0,0);
        jpgblTitle.add(jlTitle, c);
        jpTitle.add(jpgblTitle);*/

        this.add(Box.createVerticalStrut(50), BorderLayout.NORTH);

        //Panell amb els botons dels jocs centrats al mig de la pantalla
        JPanel jpgblBotons = new JPanel(new GridBagLayout());
        jbBlackJack = new JButton("BLACKJACK");
        jbBlackJack.setFocusable(false);
        jbHorseRace = new JButton("HORSE RACE");
        jbHorseRace.setFocusable(false);
        jbRoulette = new JButton("ROULETTE");
        jbRoulette.setFocusable(false);

        //Marges
        c.insets = new Insets(0,50,20,50);

        //S'afegeixen els botons dels jocs
        c.gridy = 0;
        c.gridx = 0;
        jpgblBotons.add(jbBlackJack, c);

        c.gridy = 1;
        jpgblBotons.add(jbHorseRace, c);

        c.gridy = 2;
        c.insets = new Insets(0,50,0,50);
        jpgblBotons.add(jbRoulette, c);

        this.add(jpgblBotons, BorderLayout.LINE_START);

        jpGraphicContainer = new JPanel(new GridBagLayout());
        c.gridy = 0;
        c.insets = new Insets(0,0,0,0);
        this.add(jpGraphicContainer, BorderLayout.CENTER);

        this.add(Box.createHorizontalStrut(100), BorderLayout.LINE_END);
    }

    @Override
    public void addController(Controller c){

        jbMenu.setActionCommand("returnMainView");
        jbMenu.addActionListener(c);

        jbBlackJack.setActionCommand("blackJackGraph");
        jbHorseRace.setActionCommand("horseGraph");
        jbRoulette.setActionCommand("rouletteGraph");

        jbBlackJack.addActionListener(c);
        jbHorseRace.addActionListener(c);
        jbRoulette.addActionListener(c);
    }

    public void generateGraph(String obj) {
        Graphics g =  jpGraphicContainer.getGraphics();
        lastGraphSelected = obj;

        switch (obj){
            case "b":
                createGraph(g,Database.getTop(Transaction.TRANSACTION_BLACKJACK),new Color(92, 131, 47),"BlackJack");
                break;
            case "h":
                createGraph(g,Database.getTop(Transaction.TRANSACTION_HORSES),Color.blue,"Horses");
                break;
            case "r":
                createGraph(g,Database.getTop(Transaction.TRANSACTION_ROULETTE),Color.red,"Roulette");
                break;
        }

        g.dispose();
    }

    //Donat un array d'usuaris, retorna el valor de la cartera més alta.
    private int maxWallet(User [] users){
        int max = 0;
        for(User u : users){
            if(u.getWallet() > max) max = (int)u.getWallet();
        }
        System.out.println("MAX IS " + max);
        return max;
    }

    private void createGraph(Graphics g,User[] user,Color color,String title) {

        g.setColor(new Color(54, 57,66));

        g.fillRect(0,0,jpGraphicContainer.getWidth(),jpGraphicContainer.getHeight());
        g.setFont(new Font(g.getFont().getFontName(),Font.PLAIN,15));
        FontMetrics metrics = g.getFontMetrics();

        int max = maxWallet(user) == 0 ? 500 : maxWallet(user),height = jpGraphicContainer.getHeight(),width = jpGraphicContainer.getWidth();
        int interval = max/10;
        int y = metrics.getAscent(),x = 0;

        g.setColor(Color.white);
        for(int i = max; i >= 0; i -= interval){

            g.drawString(i + "",0,y);
            g.drawLine(metrics.stringWidth(i + "") + 2,y - metrics.getAscent()/2,width,y - metrics.getAscent()/2);
            y += (height - metrics.getHeight()) / (max / interval);
        }

        for(int j = 0; j < 5; j++){
            String name = user[j].getUsername() == null ? "noData" : user[j].getUsername();
            if(name.length() > 12) {
                name = name.substring(0,9);
                name += "...";
            }
            g.setColor(color);
            int ypos = (int)map((int)user[j].getWallet(),0,max,height,0);
            g.fillRect(x + metrics.stringWidth(max + "") + 20 ,ypos,80,height - ypos - metrics.getAscent());

            g.setColor(Color.white);
            g.drawRect(x + metrics.stringWidth(max + "") + 20 ,ypos,80,height - ypos - metrics.getAscent());
            g.drawString(name,x + metrics.stringWidth(max + "") + 20 + 40 - metrics.stringWidth(name)/2,height - 3);
            x += (width - metrics.stringWidth(max + "") + 20) / 5;
        }



        g.setFont(new Font(g.getFont().getFontName(),Font.BOLD,width / 25));
        metrics = g.getFontMetrics();
        title += " top 5";
        g.drawString(title,width/2 - metrics.stringWidth(title)/2,metrics.getAscent() + 5);

    }


    private float map(float value, float istart, float istop, float ostart, float ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }

    public void updateGraph() {
        if(resize) generateGraph(lastGraphSelected);
    }

    public void enableResize(boolean resize){
        this.resize = resize;
    }
}
