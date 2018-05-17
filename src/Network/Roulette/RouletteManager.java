package Network.Roulette;

import Controlador.CustomGraphics.GraphicsManager;
import Model.RouletteModel.RouletteMessage;

import java.awt.*;
import java.util.LinkedList;

/**
 * Classe que imita lalgoritme generan en l'usuari que actualitza la lògica de
 * la simulació de la ruleta, per aixi calcular el guanyador amb la major
 * precissio possible.
 */
public class RouletteManager {

    /** Numero total de cel·les de laruleta */
    private static final int MAXCELLS = 37;

    /** Dimensions de la finestra */
    private int width, height;

    /** Llista que conté les barres separadores entre cel·les */
    private LinkedList<GRect> bars;

    /** Bola que es llença a la simulació */
    private RouletteBall ball;

    /** Atributs que controlen l'acceleració de la bola */
    private double vel, acc;

    /** Velocitat inicial de la bola */
    private static double initVel = 40;

    /** Atribut que indica si s'ha trobat un guanyador */
    private boolean winnerE;

    /** Guanyador de la partida */
    private int winner;

    /** Offset de cel·les amb el que s'inicia la tirada */
    private int shotOff;

    /**
     * Constructor que inicialitza l'objecte
     * @param width Amplada del suposat frame en el que es realitza la sumulacio
     * @param height Alçada del suposat frame en el que es realitza la sumulacio
     */
    public RouletteManager(int width, int height) {
        this.width = width;
        this.height = height;

        shotOff = 0;
    }

    /**
     * Mètode que inicia la simulació
     */
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

    /**
     * Mètode que actualitza l'avenç de la simulació per arribar a un guanyador final
     */
    public void update() {
        acc = vel > 100 ? vel > 300 ? 2 : 0.5 : 0.1;

        vel += acc;
        vel = Math.min(100000, vel);

        for (GRect r: bars) r.updateRotation( (float) vel, 0.017f, width/2, height/2);

        ball.update(0.017f, vel, width/2, height/2);

        boolean bool = true;
        for (int i = bars.size() - 1; i >= 0 && bool; i--) if (ball.rectCollision(bars.get(i))) bool = false;
    }

    /**
     * Funció que indica si s'ha trobat un guanyador
     * @return Guanyador de la tirada
     */
    public boolean winnerExists() {
        return winnerE && winner != 100;
    }

    /**
     * Mètode que fixa un guanyador
     * @param winer Guanyador de la tirada
     */
    public void setWinner(int winer) {
        winnerE = true;
        this.winner = winer;
    }

    /** Getter de la llista de barres separadores */
    public LinkedList<GRect> getBars() {
        return bars;
    }

    /**
     * Mètode que genera parametres aleatoris per a garantitzar una tirada
     * totalment aleatòria tot i no ser possible al 100% degut a que el sistema
     * és imperfecte, i la funció de Math random() conté una distribució Gaussiana
     * que no garantitza una aleatorietat pura.
     */
    public void setRandomParams() {
        final int MAXRVEL = 35;
        final int MINRVEL = 60;

        final int MAXBVEL = 100;
        final int MINBVEL = 400;

        RouletteManager.initVel = (MINRVEL - MAXRVEL) * Math.random() + MAXRVEL;
        ball.setDefaultVelY((MINBVEL - MAXBVEL) * Math.random() + MAXBVEL);
        shotOff = (int) (MAXCELLS * Math.random());
    }

    /**
     * Funció que genera el missatge a enviar a tots els clients amb la informació
     * necessària per a reproduir la simulació realitzada.
     * @return Missatge a enviar
     */
    public RouletteMessage genMessage(){
        return new RouletteMessage(initVel, RouletteBall.getInitVel(), getWinner(), shotOff);
    }

    /**
     * Mètode que retorna el guanyador de la partida tenint en compte l'offset inicial.
     */
    public int getWinner() {
        return (shotOff + winner) % MAXCELLS;
    }

    /**
     * Mètode que inicia la tirada amb els paràmetres establerts.
     */
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