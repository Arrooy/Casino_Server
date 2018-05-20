package Vista;

import Controlador.Controller;
import javax.swing.*;
import java.awt.*;

/**Classe que crea la vista que mostra les grafiques del top5 de cada joc*/
public class Top5OptionsView extends View {
    /**Boto per tornar al menu*/
    private JButton jbMenu;

    /**Boto per mostrar la grafica dels cavalls*/
    private JButton jbHorseRace;

    /**Boto per mostrar la grafica del BJ*/
    private JButton jbBlackJack;

    /**Boto per mostrar la grafica de la ruleta*/
    private JButton jbRoulette;

    /**Panell que conte la grafica associada a cada joc*/
    private JPanel jpGraphicContainer;

    /**String que indica quina grafica s'ha seleccionat la ultima*/
    private String lastGraphSelected;

    /**Boolean que indica si es vol fer o no resize de la pantalla*/
    private boolean resize;

    /**Constructor del panell que defineix on i com es col·loquen els elements*/
    public Top5OptionsView(){
        //Es defineix el Layout
        this.setLayout(new BorderLayout());

        lastGraphSelected = "b";
        resize = false;

        //Panell per col·locar el boto Menu a la part baixa a l'esquerra
        JPanel jpgblBack = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        //Marges boto per tornar al menu
        c.insets = new Insets(20,20,20,0);
        c.fill = GridBagConstraints.BOTH;
        jbMenu = new JButton("MENU");
        jbMenu.setFocusable(false);
        jpgblBack.add(jbMenu, c);

        //Flow Layout per a que el boto quedi a l'esquerra
        JPanel jpBack = new JPanel(new FlowLayout(FlowLayout.LEADING));
        jpBack.add(jpgblBack);
        this.add(jpBack, BorderLayout.SOUTH);

        this.add(Box.createVerticalStrut(50), BorderLayout.NORTH);

        //Panell amb els botons dels jocs centrats al mig de la pantalla
        JPanel jpgblBotons = new JPanel(new GridBagLayout());
        jbBlackJack = new JButton("BLACKJACK");
        jbBlackJack.setFocusable(false);
        jbHorseRace = new JButton("HORSE RACE");
        jbHorseRace.setFocusable(false);
        jbRoulette = new JButton("ROULETTE");
        jbRoulette.setFocusable(false);

        //Marges botons
        c.insets = new Insets(0,50,20,50);

        //S'afegeixen els botons dels jocs

        //Posicio boto BJ
        c.gridy = 0;
        c.gridx = 0;
        jpgblBotons.add(jbBlackJack, c);

        //Posicio boto cavalls
        c.gridy = 1;
        jpgblBotons.add(jbHorseRace, c);

        //Posicio boto ruleta
        c.gridy = 2;
        c.insets = new Insets(0,50,0,50);
        jpgblBotons.add(jbRoulette, c);

        //Els botons es col·loquen a l'esquerra de la pantalla
        this.add(jpgblBotons, BorderLayout.LINE_START);

        jpGraphicContainer = new JPanel(new GridBagLayout());
        c.gridy = 0;
        c.insets = new Insets(0,0,0,0);

        //Les grafiques es col·loquen al centre-dreta de la pantalla
        this.add(jpGraphicContainer, BorderLayout.CENTER);

        this.add(Box.createHorizontalStrut(100), BorderLayout.LINE_END);
    }

    /**
     * Metode override que relaciona cada element amb el seu Listener, i per tant es relaciona la vista amb el controlador
     * @param c Controlador
     * */
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


    /**
     * Crea una grafica de barres amb 5 usuaris i les seves 5 carteres.
     * @param g panell on es pinta la grafica
     * @param color color de les barres de la grafica
     * @param title titol de la grafica
     * @param noms noms de les 5 barres
     * @param maxWallet valor maxim de la grafica
     * @param wallets diners de cada usuari
     */
    public void createGraph(Graphics g,Color color,String title,String[] noms,long maxWallet, long [] wallets) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(54, 57,66));

        g.fillRect(0,0,jpGraphicContainer.getWidth(),jpGraphicContainer.getHeight());
        g.setFont(new Font(g.getFont().getFontName(),Font.PLAIN,15));
        FontMetrics metrics = g.getFontMetrics();

        long max = maxWallet == 0 ? 500 : maxWallet;
        int height = jpGraphicContainer.getHeight(),width = jpGraphicContainer.getWidth();
        long interval = max/10;
        int y = metrics.getAscent(),x = 0;

        g.setColor(Color.white);
        for(long i = max; i >= 0; i -= interval){

            g.drawString(i + "",0,y);
            g.drawLine(metrics.stringWidth(i + "") + 2,y - metrics.getAscent()/2,width,y - metrics.getAscent()/2);
            y += (height - metrics.getHeight()) / (max / interval);
        }

        for(int j = 0; j < 5; j++){
            String name = noms[j] == null ? "N/A" : noms[j];
            if(name.length() > 12) {
                name = name.substring(0,9);
                name += "...";
            }
            g.setColor(color);
            int ypos = (int)map((int)wallets[j],0,max,height,0);
            g.fillRect(x + metrics.stringWidth(max + "") + 20 ,ypos,40,height - ypos - metrics.getAscent());

            g.setColor(Color.white);
            g.drawRect(x + metrics.stringWidth(max + "") + 20 ,ypos,40,height - ypos - metrics.getAscent());
            g.drawString(name,x + metrics.stringWidth(max + "") + 20 + 20 - metrics.stringWidth(name)/2,height - 3);
            x += (width - metrics.stringWidth(max + "") + 20) / 5;
        }

        g.setFont(new Font(g.getFont().getFontName(),Font.BOLD,width / 25));
        metrics = g.getFontMetrics();
        title += " top 5";
        g.drawString(title,width/2 - metrics.stringWidth(title)/2,metrics.getAscent() + 5);
    }

    //TODO: comentar

    private float map(float value, float istart, float istop, float ostart, float ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }


    public void setLastGraphSelected(String lastGraph){
        lastGraphSelected = lastGraph;
    }

    public boolean isResize() {
        return resize;
    }

    public void enableResize(boolean resize){
        this.resize = resize;
    }

    public Graphics getGraphicsOfView() {
        return jpGraphicContainer.getGraphics();
    }

    public String getLastGraphSelected() {
        return lastGraphSelected;
    }
}
