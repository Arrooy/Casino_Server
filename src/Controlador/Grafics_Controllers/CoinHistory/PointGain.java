package Controlador.Grafics_Controllers.CoinHistory;

import Model.Database;
import Model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;

public class PointGain {

    private final float INITIAL_R = 1;
    private final float FINAL_R = 5;

    private long value;

    private Transaction transaction;
    private boolean drawInfo;


    private double r;

    private int x, y;

    public PointGain(Transaction gain, long value) {
        this.transaction = gain;

        drawInfo = false;
        r = INITIAL_R;

        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void update(float delta){
        if (r < FINAL_R) r += (FINAL_R - INITIAL_R) * 5 * delta;
    }

    public void updateMouse(int mx, int my){
        if (Math.sqrt(Math.pow(mx - x, 2) + Math.pow(my - y, 2)) < r) {
            r = 2 * FINAL_R;
            drawInfo = true;
        } else {
            r = FINAL_R;
            drawInfo = false;
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void render(Graphics g, Color color, Color bg, boolean ended){
        g.setColor(new Color(color.getRed() - 100, color.getGreen() - 100, color.getBlue() - 100));
        g.fillOval(x - (int) r,  y - (int) r,  2 * (int) r, 2 * (int) r);
        g.setColor(color);
        g.drawOval(x - (int) r,  y - (int) r,  2 * (int) r, 2 * (int) r);

        if (drawInfo && ended) pintaInfo(g, color);
    }

    private void pintaInfo(Graphics g, Color color) {
        String time = Database.getPassedTime(transaction.getTime());
        String gain = transaction.getGain() + "";
        String wallet = value + "";
        String type = genType(transaction.getType());

        g.setFont(new Font("Helvetica Neue", Font.BOLD, 15));

        int twidth = (int) g.getFontMetrics().getStringBounds(time, g).getWidth();
        int theight = (int) g.getFontMetrics().getStringBounds(time, g).getHeight();

        int gwidth = (int) g.getFontMetrics().getStringBounds(gain, g).getWidth();
        int gheight = (int) g.getFontMetrics().getStringBounds(gain, g).getHeight();

        Polygon polygon = calculaPolygon((int) r, Math.max(twidth, gwidth), theight + gheight);

        g.setColor(new Color(color.getRed() - 120, color.getGreen() - 120, color.getBlue() - 120));
        g.fillPolygon(polygon);

        g.setColor(color);
        g.drawString(time, x + (int) (r*1.3), y - (int) ((gheight + theight) * 1.1) - (int)r);

        if (transaction.getGain() > 0) g.setColor(new Color(40, 140, 40));
        else if (transaction.getGain() < 0) g.setColor(new Color(201, 33, 26));
        g.drawString(gain, x + (int) (r*1.3), y - (int) (gheight * 1.1) - (int)(r*1));

        g.setColor(color);
        g.drawPolygon(polygon);//TODO: acabar de completar la info
    }

    private String genType(int type) {//TODO: completar funcio
        if (type == 0) return "Type"; return "";
    }

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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}