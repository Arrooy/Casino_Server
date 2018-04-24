package Network.Roulette;

import Controlador.CustomGraphics.GraphicsManager;
import Model.RouletteMessage;

import java.awt.*;
import java.util.LinkedList;

public class RouletteManager {

    private int width = 600, height = 600;

    private LinkedList<GRect> bars;
    private RouletteBall ball;

    private LinkedList<Point[]> tests = new LinkedList<>();

    private double vel, acc;
    private static double initVel = 40;
    private boolean winnerE;
    private int winner;

    private GraphicsManager gm;

    private RouletteThread thread;

    public RouletteManager(int width, int height, RouletteThread thread) {
        this.width = width;
        this.height = height;
        this.thread = thread;
        this.gm = gm;
    }

    public void init() {
        bars = new LinkedList<>();
        ball = new RouletteBall(width / 2 - 20, height / 2 - 50, width / 2, height / 2, this, 100, 80);

        for (int i = 0; i < 37; i++) {
            bars.add(new GRect(width / 2 - 100, height / 2 - 1, 20, 2, i * 2*Math.PI/37, width/2, height/2));
        }

        vel = initVel;
        acc = 0.1;
        winnerE = false;
    }

    public void update() {
        acc = vel > 100 ? vel > 300 ? 2 : 0.5 : 0.1;

        vel += acc;
        vel = Math.min(100000, vel);

        for (GRect r: bars) r.updateRotation( (float) vel, 0.017f, width/2, height/2);

        ball.update(0.017f, vel, width/2, height/2);

        boolean bool = true;
        for (int i = bars.size() - 1; i >= 0 && bool; i--) if (ball.rectCollision(bars.get(i))) bool = false;
    }

    public boolean winnerExists() {
        return winnerE && winner != 100;
    }

    public void setWinner(int winer) {
        winnerE = true;
        this.winner = winer;
    }

    public LinkedList<GRect> getBars() {
        return bars;
    }

    public void setRandomParams() {
        final int MAXRVEL = 35;
        final int MINRVEL = 65;

        final int MAXBVEL = 100;
        final int MINBVEL = 300;

        RouletteManager.initVel = (MINRVEL - MAXRVEL) * Math.random() + MAXRVEL;
        ball.setDefaultVelY((MINBVEL - MAXBVEL) * Math.random() + MAXBVEL);
    }

    public RouletteMessage genMessage(){
        return new RouletteMessage(initVel, RouletteBall.getInitVel(), winner);
    }

    public int getWinner() {
        return winner;
    }

    public void shootBall() {
        winner = 100;

        bars = new LinkedList<>();
        for (int i = 0; i < 37; i++) {
            bars.add(new GRect(width / 2 - 100, height / 2 - 1, 20, 2, i * 2*Math.PI/37, width/2, height/2));
        }

        ball = new RouletteBall(width / 2 - 20, height / 2 - 50, width / 2, height / 2, this, 100, 80);

        vel = initVel;
        winnerE = false;
    }
}
