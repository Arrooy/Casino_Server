package Vista;

import Controlador.CoinHistoryController;
import Controlador.Controller;
import Vista.ToDraw.Coin_History.CoinHistoryDraw;
import Vista.ToDraw.ToDraw;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class CoinHistoryView extends View {

    private int width;
    private int height;

    private boolean isActive;

    GraphicsPanel gp;
    CoinHistoryDraw chd;
    CoinHistoryController chc;

    public CoinHistoryView() {

        isActive = false;
        chc = new CoinHistoryController();

        /*graphicsPanel = new GraphicsPanel(width, height);
        coinHistoryController = new CoinHistoryController();
        coinHistoryDraw = new CoinHistoryDraw(width, height, testArray());

        graphicsPanel = new GraphicsPanel(width, height);
        graphicsPanel.setCurrentDrawing(coinHistoryDraw, coinHistoryController);
        add(graphicsPanel);
        coinHistoryController.setDraw(coinHistoryDraw);*/
    }

    private LinkedList<Long> testArray() {
        LinkedList<Long> prova = new LinkedList<>();
        prova.add(200L);
        prova.add(2000L);
        prova.add(3000L);
        prova.add(-1500L);
        prova.add(5000L);
        prova.add(-500L);
        prova.add(-2000L);
        prova.add(-5000L);
        prova.add(2000L);
        prova.add(-3000L);
        prova.add(10000L);
        prova.add(-5000L);
        prova.add(1000L);
        prova.add(3000L);
        return prova;
    }

    public void createCoinHistory(String username, int width, int height) {
        this.width = width;
        this.height = height;

        gp = new GraphicsPanel(width, height);
        chd = new CoinHistoryDraw(width, height, testArray());
        chc.setDraw(chd);

        gp.setCurrentDrawing(chd, chc);
        add(gp);

        isActive = true;
    }

    public void closeView() {
        isActive = false;
        gp.exit();
        if (gp != null) remove(gp);
    }

    public void updateSize(boolean full) {
        if (gp != null) {
            gp.updateSize(this.getWidth(), this.getHeight(), full);
            gp.getCurrentDrawing().updateSize(getWidth(), getHeight());
            updateUI();
        }
    }

    @Override
    public void addController(Controller c) {
        addComponentListener(c);
    }
}
