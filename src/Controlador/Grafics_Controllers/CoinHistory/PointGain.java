package Controlador.Grafics_Controllers.CoinHistory;

import Model.Database;
import Model.Transaction;

import java.awt.*;

/**
 * Classe que representa un punt de la gràfica d'evolució temporal del
 * saldo d'un usuari. El seu funcionament es basa en apareixer progressivament
 * partint d'un radi null, i creixent fins atorar-se en un punt.
 *
 * Seguidament implementa la funcionalitat de mostrar informació d'aquella transacció en concret
 * de manera que quan l'usuari passi el ratolí per sobre, el radi creixi de cop, i aparegui
 * un requadre informatiu indicant el temps que ha passat desde la transacció, i el guany
 * que ha presentat aquella aposta.
 */
public class PointGain {

    /** Radi inicial del punt */
    private final float INITIAL_R = 1.0f;

    /** Radi final fins al que creix */
    public static final float FINAL_R = 5.0f;

    /** Valor en l'eix d'ordenades del punt (valor del wallet) */
    private long value;

    /** Transacció que gestiona */
    private Transaction transaction;

    /** Indica si es permet mostrar informació */
    private boolean drawInfo;

    /** Radi del cercle */
    private double r;

    /** Coordenades a la finestar */
    private int x, y;

    /**
     * Constructor de la classe.
     * @param gain Transacció que gestiona
     * @param value Valor del moneder resultant de la transacció
     */
    public PointGain(Transaction gain, long value) {
        this.transaction = gain;

        setDrawInfo(false);
        r = INITIAL_R;

        this.value = value;
    }

    /**
     * Mètode que actualitza el valor del radi del punt de manera lineal
     * @param delta Periode d'actualitzacio de la pantalla (0.017s)
     */
    public void update(float delta){
        if (r < FINAL_R) r += (FINAL_R - INITIAL_R) * 5 * delta;
    }

    /**
     * Mètode que detecta si el ratolí de l'usuari es troba per sobre del punt.
     * De ser cert, el que es realitza consisteix en augmentar el radi del cercle,
     * i permetre que es mostri la informació de la transacció
     * @param mx Coordenades del ratolí
     * @param my Coordenades del ratolí
     */
    public void updateMouse(int mx, int my){
        if (Math.sqrt(Math.pow(mx - x, 2) + Math.pow(my - y, 2)) < r) {
            r = 2 * FINAL_R;
            setDrawInfo(true);
        } else {
            r = FINAL_R;
            setDrawInfo(false);
        }
    }

    /** Setter que permet o no visualitzar la informació de la transacció */
    public synchronized void setDrawInfo(boolean b) {drawInfo = b;}

    /** Setter de la posició del punt en la pantalla */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Mètode que renderitza el cercle
     * @param g Element en el que es pinta el cercle (Gràfica)
     * @param color Color amb el que es vol pintar el punt
     */
    public void render(Graphics g, Color color){
        g.setColor(new Color(color.getRed() - 100, color.getGreen() - 100, color.getBlue() - 100));
        g.fillOval(x - (int) r,  y - (int) r,  2 * (int) r, 2 * (int) r);
        g.setColor(color);
        g.drawOval(x - (int) r,  y - (int) r,  2 * (int) r, 2 * (int) r);
    }

    /**
     * Mètode que pinta la informació de la informació per pantalla
     * @param g Element en el que pintar
     * @param ended Ha acabat la animació?
     * @param color Color amb el que pintar
     */
    public void renderInfo(Graphics g, boolean ended, Color color, int width) {
        if (drawInfo && ended) {
            pintaInfo(g, color, width);
        }
    }

    /**
     * Mètode que pinta la informació referent a la transacció. Pinta una bafarada amb informació dins.
     * La informació que mostra és referent a la quantitat de diners que s'ha guanyat en aquella transacció,
     * i també es mostra la quantitat de temps que ha passat desde llavors.
     *
     * En cas de que la bafarada quedes tallada pels limits de la finestra, aquesta es pintaria de manera
     * invertida, de manera que es pugui visualitzar de manera correcta.
     *
     * @param g Element en el que es pinta
     * @param color Color de la bafarada
     * @param width Amplada de la finestra
     */
    private void pintaInfo(Graphics g, Color color, int width) {
        String time = Database.getPassedTime(transaction.getTime());
        String gain = transaction.getGain() + "";

        g.setFont(new Font("Helvetica Neue", Font.BOLD, 15));

        int twidth = (int) g.getFontMetrics().getStringBounds(time, g).getWidth();
        int theight = (int) g.getFontMetrics().getStringBounds(time, g).getHeight();

        int gwidth = (int) g.getFontMetrics().getStringBounds(gain, g).getWidth();
        int gheight = (int) g.getFontMetrics().getStringBounds(gain, g).getHeight();

        Polygon polygon;
        int textx;

        if (width < x + Math.max(twidth, gwidth)) {
            polygon = calculaInvertedPolygon((int) r, Math.max(twidth, gwidth), theight + gheight);
            textx = x + (int) (r*1.3) - (int)(1.5*Math.max(twidth, gwidth));
        } else {
            polygon = calculaPolygon((int) r, Math.max(twidth, gwidth), theight + gheight);
            textx = x + (int) (r*1.3);
        }

        g.setColor(new Color(color.getRed() - 120, color.getGreen() - 120, color.getBlue() - 120));
        g.fillPolygon(polygon);

        g.setColor(color);
        g.drawString(time, textx, y - (int) ((gheight + theight) * 1.1) - (int)r);

        if (transaction.getGain() > 0) g.setColor(new Color(40, 140, 40));
        else if (transaction.getGain() < 0) g.setColor(new Color(201, 33, 26));
        g.drawString(gain, textx, y - (int) (gheight * 1.1) - (int)(r*1));

        g.setColor(color);
        g.drawPolygon(polygon);
    }

    /**
     * Calcula tots els vertex de la bafarada invertida
     * @param radi Radi del cercle
     * @param width Amplada de la bafarada
     * @param height Alçada
     * @return Poligon amb tots els vertex calculats
     */
    private Polygon calculaInvertedPolygon(int radi, int width, int height) {
        int[] xa = new int[7];
        int[] ya = new int[7];

        xa[0] = x - radi;
        ya[0] = y - radi * 2;

        xa[1] = xa[0];
        ya[1] = (int) (ya[0] - height * 1.2);

        xa[2] = (int) (xa[0] - width * 1.2);
        ya[2] = ya[1];

        xa[3] = xa[2];
        ya[3] = ya[0];

        xa[4] = xa[0] - 2*radi;
        ya[4] = ya[0];

        xa[5] = xa[0] - radi/2;
        ya[5] = ya[0] + radi;

        xa[6] = xa[0] - radi;
        ya[6] = ya[0];

        return new Polygon(xa, ya, 7);
    }

    /**
     * Calcula tots els vertex de la bafarada
     * @param radi Radi del cercle
     * @param width Amplada de la bafarada
     * @param height Alçada
     * @return Poligon amb tots els vertex calculats
     */
    public Polygon calculaPolygon(int radi, int width, int height){
        int[] xa = new int[7];
        int[] ya = new int[7];

        xa[0] = x + radi;
        ya[0] = y - radi * 2;

        xa[1] = xa[0];
        ya[1] = (int) (ya[0] - height * 1.2);

        xa[2] = (int) (xa[0] + width * 1.2);
        ya[2] = ya[1];

        xa[3] = xa[2];
        ya[3] = ya[0];

        xa[4] = xa[0] + 2*radi;
        ya[4] = ya[0];

        xa[5] = xa[0] + radi/2;
        ya[5] = ya[0] + radi;

        xa[6] = xa[0] + radi;
        ya[6] = ya[0];

        return new Polygon(xa, ya, 7);
    }

    /** Getter de la X */
    public int getX() {
        return x;
    }

    /** Getter de la y */
    public int getY() {
        return y;
    }

    /** Getter del valor del punt */
    public long getValue() {
        return value;
    }

    /** Getter de la transacció */
    public Transaction getTransaction() {
        return transaction;
    }
}