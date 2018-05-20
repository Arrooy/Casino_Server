package Controlador.Grafics_Controllers.CoinHistory;

import Controlador.Controller;
import Controlador.CustomGraphics.GraphicsController;
import Model.Transaction;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

/**
 * Controlador d'un Coin History. Genera un gràfic que mostra
 * l'evolució temporal de la quantitat de diners d'un usuari,
 * de manera animada i interactiva per a visualitzar informació
 * més concreta en cas de que l'usuari ho desitji.
 *
 * Aquesta classe implementa una funció que consisteix en evitar una sobrecrarrega de punts
 * ja que es pot donar la possibilitat de que un sol usuari realitzi centenars o milers d'apostes,
 * es donaria el cas de que tots els punts de la gràfica a mostrar quedarien sobreposats.
 * Per a evitar aquest problema, es simplifica el llistat d'apostes realitzades dividint per
 * dos la seva quantitat, tantes vegades comcalgui per a que finalment totes hi capiguen de manera
 * ordenada i repartida.
 *
 * Per veure com funciona la simplificació amb més detall, veure la funcio simplificaPoints().
 */
public class CoinHistoryController implements GraphicsController {

    /** Duració total de la animació */
    private static final int DURATION = 3000;

    /** Array que conté el valor del moneder per a cada punt a mostrar */
    private long[] wallet;

    /** Llistat de transaccions registrades a mostrar */
    private static LinkedList<Transaction> gains;

    /** Llista de punts que es mostren realment */
    private LinkedList<PointGain> pointGains;

    /** Llistat original de punts sense simplificar */
    private LinkedList<PointGain> originalPointGains;

    /** Nom del usuari de la grafica */
    private String username;

    /** Distància entre punts */
    private int dist;

    /** Timer que marca el temps de la animació */
    private double timer;

    /** Index de punt que està pintant l'animació */
    private int index;

    /** Indica si ha finalitzat l'animació */
    private boolean ended;

    /** Colors usats en la vista */
    private Color bgc, linesC;

    /** Dimensions de la finestra */
    private int width, height;

    /** Temps que triga la animació en afegir un nou punt */
    private int delay;

    /** Valor màxim al que arriba la gràfica */
    private long maxValue;

    /** Coordenades i dimensions del botò per sortir de la vista */
    private int bx, by, bw, bh;

    /** Indica si el botó està premut */
    private boolean pressed;

    /** Controlador general de la finestra */
    private Controller generalController;

    /** Numero total de punts, tamany real de transaccions i quantitat de
     * divisions realitzades per arribar a simplificar la llista */
    private int totalSize, realSize, division;

    /**
     * Constructor de la classe.
     * @param width Dimensions de la finestra
     * @param height Dimensions de la finestra
     * @param username Nom de l'usuari de la gràfica
     * @param c Controlador generic de la finestra
     */
    public CoinHistoryController(int width, int height, String username, Controller c) {
        this.generalController = c;
        initGraf(width, height, username);
    }

    /**
     * Mètode que s'executa al iniciar la finestra gràfica. S'encarrega
     * d'inicialitzar les variables necessàries.
     */
    @Override
    public void init() {
        pointGains = new LinkedList<>();
        originalPointGains = new LinkedList<>();

        for(int i = 0; i < wallet.length; i++) {
            originalPointGains.add(new PointGain(gains.get(i), wallet[i]));
        }

        dist = width / (wallet.length + 1);

        timer = System.nanoTime() / 1000000;
        index = 0;

        bgc = new Color(23, 24, 24);
        linesC = new Color(191, 191, 156);
    }

    /**
     * Constructor real de la classe. Aquest es troba separat de l'original, ja que
     * un cop generada la instància de l'objecte d'aquesta classe, aquest s'ha de mantenir
     * al llarg de tota l'execució, ja que de no ser així, es provocarien problemes en el
     * funcionament de la gràfica.
     * @param width Dimensions de la finestra
     * @param height Dimensions de la finestra
     * @param username Nom de l'usuari de la gràfica
     */
    public void initGraf(int width, int height, String username) {
        this.width = width;
        this.height = height;
        this.username = username;

        division = 1;
        totalSize = gains.size();

        while (gains.size() * PointGain.FINAL_R * 4 > width){
            LinkedList<Transaction> aux = new LinkedList<>();

            for(Transaction p : gains) aux.add(new Transaction(p.getGain(), p.getTime(), p.getType()));
            while (aux.size() * PointGain.FINAL_R * 4 > width) aux = simplificaPoints(aux);

            gains = aux;
        }

        realSize = gains.size();

        division = totalSize / realSize;
        maxValue = 0;

        wallet = new long[gains.size()];
        for (int i = 0; i < gains.size(); i++) {
            wallet[i] = i == 0 ? gains.get(i).getGain() : wallet[i - 1] + gains.get(i).getGain();
            if (maxValue < wallet[i]) maxValue = wallet[i];
        }

        delay = DURATION / wallet.length;
        ended = false;

        bx = 30;
        by = (int) (height * .88);
        bw = 200;
        bh = (int) (height * .08);
        pressed = false;
    }

    /**
     * Setter del llistat de guanys
     * @param gains Llista de transaccions
     */
    public static void setGains(LinkedList<Transaction> gains) {
        CoinHistoryController.gains = gains;
    }

    /**
     * Mètode que actualitza la lògica de la animació. Aquesta segueix un
     * funcionament en el que cada un cert temps (el just per a que tot la animació
     * tingui una durada de DURATION ms) s'afegeix un nou punt al llistat de punts,
     * fins que ja estigui la gràfica complerta.
     *
     * @param delta Periode d'actualitzacio de la pantalla (0.017s)
     */
    @Override
    public void update(float delta) {
        dist = width / (realSize + 1);

        bx = 30;
        by = (int) (height * .88);
        bh = (int) (height * .08);

        double now = System.nanoTime() / 1000000;
        if (now - timer > delay && index < wallet.length) {
            pointGains.add(new PointGain(gains.get(index), wallet[index++]));

            timer = System.nanoTime() / 1000000;
        } else if (now - timer > delay) {
            pointGains.getLast().update(delta);
            ended = true;
        }

        for (int i = 0; i < pointGains.size() - 1; i++) {
            PointGain p = pointGains.get(i);
            p.update(delta);
        }

        for (int i = 0; i < pointGains.size(); i++) pointGains.get(i).setPosition(dist * (i + 1),
                (int) ((double)height*0.85 - (double)height * 0.6 * ((double)pointGains.get(i).getValue()/(double)maxValue)));
    }

    /**
     * Mètode que simplifica la llista de transaccions dividint el seu tamany entre dos
     * amb la finalitat de reduir una possible sobrecàrrega de punts en la gràfica.
     *
     * La funció és cridada repetidament fins que es considera que l'estat de la llista es
     * pot visualitzar amb una distància entre punts minima.
     *
     * Al dividir entre dos, el que realment s'està realitzant consisteix en anar ajuntant
     * els punts en parelles, sumant el guany que han proporcionat cadascuna de les transaccions,
     * de manera que el conjunt d'elles mostri el mateix resultat que s'hagués obtingut, però
     * amb una transacció menys.
     * En quant a la data de la transacció s'agafa la més antiga.
     *
     * @param original Llista de transaccions original
     * @return Llista de transaccions dividida entre dos
     */
    private LinkedList<Transaction> simplificaPoints(LinkedList<Transaction> original) {
        int size = original.size();
        int sizeDivided = size / 2;

        LinkedList<Transaction> aux = new LinkedList<>();

        for(int i = 0; i < sizeDivided; i++){
            Transaction p0 = original.get(i * 2);
            Transaction p1 = original.get(i * 2 + 1);

            aux.add(new Transaction(p0.getGain() + p1.getGain(), p0.getTime(), p0.getType()));
        }

        if(size % 2 != 0) aux.getLast().setGain(aux.getLast().getGain() + original.getLast().getGain());

        return aux;
    }

    /**
     * Mètode que genera la gràfica visual i la renderitza. La grafica es genera de manera que
     * inicialment es carregui un punt però no es renderitzi, llavors al obtenir dos punts, es pinta
     * el primer punt, generant un cercle que augmenta el radi en funció del temps fins quedar-se atorat,
     * i una línia que creix linealment traçant una recta entre el punt que s'està generant i el següent punt.
     * Un cop la linia arriba al seu destí, el tercer punt ja s'ha afegit, i es repeteix el procés que
     * han seguit el primer i el segon, però amb el segon i el tercer, i així successivament fins arribar
     * al total de punts de la llista original. Al arribar al últim per aixó, es controla el fet de que simplement
     * ha de fer apareixer el seu cercle, però no ha de traçar cap linia.
     *
     * Una funcionalitat que implementa la gràfica consisteix en fer apareixer un requadre informatiu que indica
     * la quantitat de diners que s'han guanyat / perdut en aquella aposta en concret, i la quantitat de
     * temps que ha passat respecte aquella aposta.
     *
     * A més a més, també es pinten poligons semitransparents que apareixen per sota de la gràfica que es va
     * pintant omplint l'espai que hi ha entre les rectes de la gràfica i l'eix d'abcisses.
     *
     * Tot i ser una distribució tempral, aquesta gràfica recrea una representació no lineal respecte el temps,
     * ja que si un usuari juga 200 partides en un dia, i seguidament no torna a jugar per jugar-ne 300 al cap de
     * dues setmanes, la distribució dels punts quedaria massa desproporcionada, de manera que la representació
     * es realitza en funció de les apostes realitzades, indicant en l'eix de les abcisses la quantitat d'apostes
     * realitzades, i en l'eix d'ordenades el tamany del moneder de l'usuari.
     *
     * La gràfica es troba completament escalada respecte el tamany de la pantalla, i respecte el valor màxim que
     * assoleix el moneder en qualsevol dels punts, ja que aixi, un usuari que ha arribat als 4 billons de monedes
     * podria visualitzar una grafica similar a la d'un usuari que no ha passat mai dels 1000, sense que el segon
     * visualitzi només una linia recta a 0.
     *
     * Finalment, respecte a la UI cal dir que s'indica a la part superior-esquerra el nom de l'usuari, i a la
     * mateixa banda, a la part inferior es visualitza un botò per a retornar al menu del ranking. Ambdós es troben
     * totalment escalats al tamany de la pantalla.
     *
     * @param g Element en el que pintar el contingut
     */
    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Es pinta el fons
        g.setColor(bgc);
        g.fillRect(0, 0, width, height);

        //Es pinten els eixos
        renderAxis(g);
        int base = (int) (height*0.85);

        g.setColor(linesC);
        g.drawLine(0, base, width, base);

        //Es pinten les linies i poligons ja animats
        for(int i = 1; i < pointGains.size() - 1; i++) {
            PointGain p = pointGains.get(i);

            int x1 = pointGains.get(i - 1).getX();
            int y1 = pointGains.get(i - 1).getY();
            int x2 = p.getX();
            int y2 = p.getY();

            final int[] xa = {x1, x1, x2, x2};
            final int[] ya = {base, y1, y2, base};

            g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 10));
            g.fillPolygon(new Polygon(xa, ya, 4));

            g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 30));
            g.drawPolygon(new Polygon(xa, ya, 4));

            g.setColor(linesC);
            g.drawLine(x1, y1, x2, y2);
        }

        //Es calculen les coordenades de l'animació
        int x1 = 0, x2 = 0, y1 = 0, y2 = 0, x0 = 0, y0 = 0;

        double t = System.nanoTime() / 1000000 - timer;

        if (pointGains.size() > 1) {
            x1 = pointGains.get(pointGains.size() - 2).getX();
            y1 = pointGains.get(pointGains.size() - 2).getY();
            x2 = pointGains.getLast().getX();
            y2 = pointGains.getLast().getY();

            x0 = (int) (x1 + (x2 - x1) * t/delay);
            y0 = (int) (y1 + (y2 - y1) * t/delay);
        }

        final int[] xa = {x1, x1, x0, x0};
        final int[] ya = {base, y1, y0, base};

        //Es renderitza la linia i el poligon animats
        if (pointGains.size() >= 2 && pointGains.size() != wallet.length) {
            g.drawLine(x1, y1, x0, y0);

            g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 10));
            g.fillPolygon(new Polygon(xa, ya, 4));
        } else if (t < delay) {
            g.drawLine(x1, y1, x0, y0);

            g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 10));
            if (pointGains.size() > 1) g.fillPolygon(new Polygon(xa, ya, 4));
        } else {
            g.drawLine(x1, y1, x2, y2);
            if (pointGains.size() > 0) {
                pointGains.get(pointGains.size() - 1).render(g, linesC);
                pointGains.get(pointGains.size() - 1).renderInfo(g, ended, linesC, width);
            }

            final int[] xb = {x1, x1, x2, x2};
            final int[] yb = {base, y1, y2, base};

            g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 10));
            g.fillPolygon(new Polygon(xb, yb, 4));
        }

        //Es pinten els punts
        for (int i = 0; i < pointGains.size() - 1; i++) {
            PointGain p = pointGains.get(i);
            p.render(g, linesC);
        }

        //Es pinten els panells d'informació
        for (int i = 0; i < pointGains.size() - 1; i++) {
            PointGain p = pointGains.get(i);
            p.renderInfo(g, ended, linesC, width);
        }

        renderButton(g);

        g.setFont(new Font("Helvetica Neue", Font.BOLD, (int) (height*0.10)));
        g.setColor(linesC);
        g.drawString(username, 30, (int) (height * 0.13));
    }

    /**
     * Mètode que renderitza els eixos de la gràfica.
     * @param g Element en el que pintar el contingut
     */
    private void renderAxis(Graphics g) {//.25 -> .85
        int dist = 100;
        double numOfDivisions = height * 0.6 / dist;
        double inc = (long) (maxValue / numOfDivisions);

        for (int i = 0; i <= numOfDivisions; i++) {
            String val = "" + (int) (inc * i);
            int y = (int) (height*0.85 - i * dist);

            if (Math.abs(y - height*0.25) > 20) {
                g.setColor(new Color(191, 191, 156, 54));
                g.drawLine(0, y, width, y);

                g.setColor(new Color(191, 191, 156, 180));
                g.drawString(val, width/(4*realSize), y - 3);
            }
        }

        g.setColor(new Color(191, 191, 156, 40));
        g.drawLine(0, (int) (height*0.25), width, (int) (height*0.25));
        g.setColor(new Color(191, 191, 156, 180));
        g.drawString(maxValue + "", width/(4*realSize),  (int) (height*0.25) - 3);

        int preescaler = 1;
        if (pointGains.size() > 10) {
            if (g.getFontMetrics().getStringBounds("0000", g).getBounds().width > pointGains.get(1).getX() - pointGains.get(0).getX()) {
                preescaler = 2;
            }
        }

        for (int i = 0; i < pointGains.size() / preescaler; i++) {
            String val = "" + i * preescaler * division;
            int x = pointGains.get(i * preescaler).getX();

            g.drawString(val, x, (int) (height * .85 +15));
        }
    }

    /**
     * Mètode per a actualitzar el tamany de la finestra grafica
     * @param width Noves dimensions
     * @param height Noves dimensions
     */
    public void updateSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Mètode per a renderitzar el botó per a sortir del gràfic. Aquest s'escala en funció del tamany de
     * la pantalla, i en cas d'estar premut, es pinta e fons d'un color més clar
     * @param g Element en el que pintar el contingut
     */
    private void renderButton(Graphics g){
        String text = "Back";
        g.setFont(new Font("Helvetica Neue", Font.BOLD, (int) (height * .06)));
        int width = (int) (g.getFontMetrics().getStringBounds(text, g).getBounds().width + bx*0.4);
        bw = width;

        if (!pressed) g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 10));
        else g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 100));
        g.fillRect(bx, by, width, bh);

        g.setColor(linesC);
        g.drawRect(bx, by, width, bh);

        if (!pressed) g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 200));
        else g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 10));
        g.drawString("Back", (int)(bx*1.2), (int) (height * .94));
    }

    /**
     * Mètode per a detectar si s'ha apretat el botó per sortir
     * @param e Event generat
     */
    @Override
    public void mousePressed(MouseEvent e) {
        pressed = e.getX() > bx && e.getX() < (bx + bw) && e.getY() > by && e.getY() < (by + bh);
    }

    /**
     * Mètode que s'executa quan es deixa de prèmer el botó per sortir. S'encarrega de retornar al ranking
     * @param e Event generat
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        //S'actualitza l'estat del botó
        pressed = e.getX() > bx && e.getX() < (bx + bw) && e.getY() > by && e.getY() < (by + bh);

        //Es retorna al rànking
        if (e.getX() > bx && e.getX() < (bx + bw) && e.getY() > by && e.getY() < (by + bh)) generalController.viewRankingView();
    }

    /**
     * Es detecta si l'usuari passa per sobre d'un punt per així mostrar la seva informació
     * @param e Event generat
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        for (PointGain p: pointGains) p.updateMouse(e.getX(), e.getY());
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseDragged(MouseEvent e) {}
}
