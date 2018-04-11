package Vista.ToDraw.Coin_History;

import Vista.GraphicsPanel;
import Vista.ToDraw.ToDraw;

import java.awt.*;
import java.util.LinkedList;

public class CoinHistoryDraw implements ToDraw {

    public static final int DURATION = 2000;

    private long[] wallet;
    //private final long[] wallet = {10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000};

    private LinkedList<Point> points;
    private int dist;

    GraphicsPanel graphicsPanel;

    private double timer;
    private int index;
    private boolean ended;

    private Color bgc, linesC;

    private Integer width, height;
    private int delay;
    private long maxValue;

    public CoinHistoryDraw(Integer width, Integer height, LinkedList<Long> gains) {
        this.width = width;
        this.height = height;

        wallet = new long[gains.size()];
        for (int i = 0; i < gains.size(); i++) {
            if (maxValue < gains.get(i)) maxValue = gains.get(i);
            wallet[i] = i == 0 ? gains.get(i) : wallet[i - 1] + gains.get(i);
        }

        delay = DURATION / wallet.length;
        maxValue /= height/30;

        ended = false;
    }

    @Override
    public void init(GraphicsPanel graphicsPanel) {
        this.graphicsPanel = graphicsPanel;

        points = new LinkedList<>();

        //TODO: Omplir de la database
        //for (int i = 0; i < wallet.length; i++) points.add(new Point(i, wallet[i], 1));
        dist = width / (wallet.length + 1);//graphicsPanel.getBounds().width / (points.size() + 1);

        timer = System.nanoTime() / 1000000;
        index = 0;

        bgc = new Color(23, 24, 24);
        linesC = new Color(191, 191, 156);
    }

    @Override
    public void update(float delta) {

        double now = System.nanoTime() / 1000000;
        if (now - timer > delay && index < wallet.length) {
            //points.add(new Point(index, wallet[index++]));
            points.add(new Point(index, index > 1 ? wallet[index] - wallet[index - 1] : wallet[index], wallet[index++]));
            timer = System.nanoTime() / 1000000;
        } else if (now - timer > delay) {
            points.getLast().update(delta);
            ended = true;
        }

        for (int i = 0; i < points.size() - 1; i++) {
            Point p = points.get(i);
            p.update(delta);
        }
    }

    @Override
    public void render(Graphics g) {
        g.setColor(bgc);
        g.fillRect(0, 0, width, height);
        g.setColor(linesC);

        g.drawLine(0, height - 100, width, height - 100);

        for(int i = 0; i < points.size() - 1; i++) {
            Point p = points.get(i);

            if (i != 0) {
                int x1 = dist * (i);
                int y1 = height - 100 - (int) ((double)points.get(i-1).getValue() * (maxValue / 11000d));
                int x2 = dist * (i + 1);
                int y2 = height - 100 - (int) ((double)p.getValue() * (maxValue / 11000d));

                final int[] xa = {x1, x1, x2, x2};
                final int[] ya = {height - 100, y1, y2, height - 100};

                g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 10));
                g.fillPolygon(new Polygon(xa, ya, 4));

                g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 30));
                g.drawPolygon(new Polygon(xa, ya, 4));

                g.setColor(linesC);
                g.drawLine(x1, y1, x2, y2);
            }
        }

        int x1 = points.size() >= 2 ? dist * (points.size() - 1) : 0;
        int y1 = points.size() >= 2 ? height - 100 - (int) ((double)points.get(points.size()-2).getValue() * (maxValue / 11000d)) : 0;
        int x2 = points.size() >= 2 ? dist * (points.size()) : 0;
        int y2 = points.size() >= 2 ? height - 100 - (int) ((double)points.get(points.size()-1).getValue() * (maxValue / 11000d)) : 0;
        double t = System.nanoTime() / 1000000 - timer;

        int x0 = (int) (x1 + (dist * points.size() - x1) * t/delay);
        int y0 = points.size() > 1 ? (int) (y1 + (height - 100 - (int) ((double)points.getLast().getValue() * (maxValue / 11000d)) - y1) * t/delay) : y1;

        final int[] xa = {x1, x1, x0, x0};
        final int[] ya = {height - 100, y1, y0, height - 100};

        if (points.size() >= 2 && points.size() != wallet.length) {
            g.drawLine(x1, y1, (int) ((x2 - x1) * t/delay) + x1, (int) ((y2 - y1) * t/delay) + y1);

            g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 10));
            g.fillPolygon(new Polygon(xa, ya, 4));
        } else if (t < delay) {
            g.drawLine(x1, y1, (int) ((x2 - x1) * t/delay) + x1, (int) ((y2 - y1) * t/delay) + y1);

            g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 10));
            if (points.size() > 1) g.fillPolygon(new Polygon(xa, ya, 4));

        } else {
            g.drawLine(x1, y1, x2, y2);
            if (points.size() > 0) points.get(points.size() - 1).render(g, linesC, bgc, x2, y2, ended);

            final int[] xb = {x1, x1, x2, x2};
            final int[] yb = {height - 100, y1, y2, height - 100};

            g.setColor(new Color(linesC.getRed(), linesC.getGreen(), linesC.getBlue(), 10));
            g.fillPolygon(new Polygon(xb, yb, 4));
        }

        for(int i = 0; i < points.size() - 1; i++) {
            Point p = points.get(i);
            p.render(g, linesC, bgc, dist * (i + 1), height - 100 - (int) ((double)p.getValue() * (maxValue / 11000d)), ended);
        }
    }

    public LinkedList<Point> getPoints() {
        return points;
    }
}
