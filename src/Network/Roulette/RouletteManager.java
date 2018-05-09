package Network.Roulette;

import Controlador.CustomGraphics.GraphicsManager;
import Model.RouletteModel.RouletteMessage;

import java.awt.*;
import java.util.LinkedList;

public class RouletteManager {

    public static final int MAXCELLS = 37;

    private int width = 600, height = 600;

    private LinkedList<GRect> bars;
    private RouletteBall ball;

    private LinkedList<Point[]> tests = new LinkedList<>();

    private double vel, acc;
    private static double initVel = 40;
    private boolean winnerE;
    private int winner;

    private int shotOff;

    private GraphicsManager gm;

    private RouletteThread thread;

    public RouletteManager(int width, int height, RouletteThread thread) {
        this.width = width;
        this.height = height;
        this.thread = thread;
        this.gm = gm;

        shotOff = 0;
    }

    public void init() {
        bars = new LinkedList<>();
        ball = new RouletteBall(width / 2 - 20, height / 2 - 50, width / 2, height / 2, this, 100, 80);

        for (int i = 0; i < MAXCELLS; i++) {
            bars.add(new GRect(width / 2 - 100, height / 2 - 1, 20, 2, i * 2*Math.PI/MAXCELLS, width/2, height/2));
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
        final int MINRVEL = 60;

        final int MAXBVEL = 100;
        final int MINBVEL = 400;

        RouletteManager.initVel = (MINRVEL - MAXRVEL) * Math.random() + MAXRVEL;
        ball.setDefaultVelY((MINBVEL - MAXBVEL) * Math.random() + MAXBVEL);
        shotOff = (int) (MAXCELLS * Math.random());
    }

    public RouletteMessage genMessage(){
        return new RouletteMessage(initVel, RouletteBall.getInitVel(), getWinner(), shotOff);
    }

    public int getWinner() {
        return (shotOff + winner) % MAXCELLS;
    }

    public void shootBall() {
        winner = 100;

        bars = new LinkedList<>();
        for (int i = 0; i < MAXCELLS; i++) {
            bars.add(new GRect(width / 2 - 100, height / 2 - 1, 20, 2, i * 2*Math.PI/MAXCELLS, width/2, height/2));
        }

        ball = new RouletteBall(width / 2 - 20, height / 2 - 50, width / 2, height / 2, this, 100, 80);

        vel = initVel;
        winnerE = false;
    }
}