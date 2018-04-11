package Vista;

import Controlador.CoinHistoryController;
import Vista.ToDraw.Coin_History.CoinHistoryDraw;
import javax.swing.*;
import java.util.LinkedList;

public class CoinHistoryView extends JPanel {

    private Integer width = 1280;
    private Integer height = 720;

    public CoinHistoryView() {

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

        GraphicsPanel graphicsPanel = new GraphicsPanel(width, height);

        CoinHistoryController coinHistoryController = new CoinHistoryController();
        CoinHistoryDraw coinHistoryDraw = new CoinHistoryDraw(width, height, prova);
        graphicsPanel.setCurrentDrawing(coinHistoryDraw, coinHistoryController);

        coinHistoryController.setDraw(coinHistoryDraw);

        add(graphicsPanel);
    }

}
