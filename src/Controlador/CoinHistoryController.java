package Controlador;

import Vista.ToDraw.Coin_History.CoinHistoryDraw;
import Vista.ToDraw.Coin_History.Point;
import Vista.ToDraw.ToDraw;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class CoinHistoryController extends GraphicsController {

    private CoinHistoryDraw draw;

    public void setDraw(ToDraw draw) {
        this.draw = (CoinHistoryDraw) draw;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        for (Point p: draw.getPoints()) p.updateMouse(e.getX(), e.getY());
    }
}
