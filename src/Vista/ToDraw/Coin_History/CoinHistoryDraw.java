package Vista.ToDraw.Coin_History;

import Vista.GraphicsPanel;
import Vista.ToDraw.ToDraw;

import java.awt.*;
import java.util.LinkedList;

public class CoinHistoryDraw implements ToDraw {

    public static final int DURATION = 3000;

    private long[] wallet;
    //private final long[] wallet = {10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000};

    private LinkedList<PointGain> pointGains;
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
        ended = false;
    }

    @Override
    public void init(GraphicsPanel graphicsPanel) {
        this.graphicsPanel = graphicsPanel;

        pointGains = new LinkedList<>();

        //TODO: Omplir de la database
        //for (int i = 0; i < wallet.length; i++) pointGains.add(new PointGain(i, wallet[i], 1));
        dist = width / (wallet.length + 1);//graphicsPanel.getBounds().width / (pointGains.size() + 1);

        timer = System.nanoTime() / 1000000;
        index = 0;

        bgc = new Color(23, 24, 24);
        linesC = new Color(191, 191, 156);

        graphicsPanel.setBackgroundColor(bgc);
    }

    @Override
    public void update(float delta) {
        dist = width / (wallet.length + 1);

        double now = System.nanoTime() / 1000000;
        if (now - timer > delay && index < wallet.length) {
            //pointGains.add(new PointGain(index, wallet[index++]));
            pointGains.add(new PointGain(index, index > 1 ? wallet[index] - wallet[index - 1] : wallet[index], wallet[index++]));

            timer = System.nanoTime() / 1000000;
        } else if (now - timer > delay) {
            pointGains.getLast().update(delta);
            ended = true;
        }

        for (int i = 0; i < pointGains.size() - 1; i++) {
            PointGain p = pointGains.get(i);
            p.update(delta);
        }// height*0.85 - height * 0.7 / maxValue

        for (int i = 0; i < pointGains.size(); i++) pointGains.get(i).setPosition(dist * (i + 1),
                (int) ((double)height*0.85 - (double)height * 0.6 * ((double)pointGains.get(i).getValue()/(double)maxValue)));
    }

    @Override
    public void render(Graphics g) {
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

        //TODO: update points position
    }

    public LinkedList<PointGain> getPointGains() {
        return pointGains;
    }
}
