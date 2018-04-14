package Controlador.Grafics_Controllers.CoinHistory;

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
 */
public class CoinHistoryController implements GraphicsController {

    private static final int DURATION = 3000;

    private long[] wallet;
    private LinkedList<Transaction> gains;

    private LinkedList<PointGain> pointGains;
    private int dist;

    private double timer;
    private int index;
    private boolean ended;

    private Color bgc, linesC;

    private int width, height;
    private int delay;
    private long maxValue;

    public CoinHistoryController(int width, int height, LinkedList<Transaction> gains) {
        this.width = width;
        this.height = height;
        this.gains = gains;

        wallet = new long[gains.size()];
        for (int i = 0; i < gains.size(); i++) {
            wallet[i] = i == 0 ? gains.get(i).getGain() : wallet[i - 1] + gains.get(i).getGain();
            if (maxValue < wallet[i]) maxValue = wallet[i];
        }

        delay = DURATION / wallet.length;
        ended = false;
    }

    @Override
    public void init() {
        pointGains = new LinkedList<>();

        dist = width / (wallet.length + 1);

        timer = System.nanoTime() / 1000000;
        index = 0;

        bgc = new Color(23, 24, 24);
        linesC = new Color(191, 191, 156);
    }

    @Override
    public void update(float delta) {
        dist = width / (wallet.length + 1);

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

    @Override
    public void render(Graphics g) {
        g.setColor(bgc);
        g.fillRect(0, 0, width, height);

        int base = (int) (height*0.85);

        g.setColor(linesC);
        g.drawLine(0, base, width, base);

        for(int i = 0; i < pointGains.size() - 1; i++) {
            PointGain p = pointGains.get(i);

            if (i != 0) {
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
        }

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
            if (pointGains.size() > 0) pointGains.get(pointGains.size() - 1).render(g, linesC, bgc, ended);

            final int[] xb = {x1, x1, x2, x2};
            final int[] yb = {base, y1, y2, base};

            g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 10));
            g.fillPolygon(new Polygon(xb, yb, 4));
        }

        for(int i = 0; i < pointGains.size() - 1; i++) {
            PointGain p = pointGains.get(i);
            p.render(g, linesC, bgc, ended);
        }
    }

    public void updateSize(int width, int height) {
        this.width = width;
        this.height = height;
        System.out.println(width + " x " + height);
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
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        for (PointGain p: pointGains) p.updateMouse(e.getX(), e.getY());
    }
}
