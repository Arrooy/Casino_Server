package Controlador.CustomGraphics;

import javax.swing.*;
import java.awt.*;


/**
 * Classe destinada a gestionar el funcionament d'un JPanel implementat
 * per a mostrar Graphics.
 */
@SuppressWarnings("serial")
public class GraphicsManager implements Runnable {

    /**Fil d'execució que actualitza constantment el mostrat al panell*/
    private Thread thread;

    /**Indicador de si el panell es troba en funcionalemnt*/
    private boolean running;

    /**Imatge en la que es pinten els grafics*/
    private Image image;

    /**Panell el que pintar la imatge generada*/
    private JPanel JPanelObjectiu;

    /**Gestor que decideix el que es pinta per pantalla*/
    private GraphicsController controlador_extern;

    /**Color amb el que es pinta el fons de pantalla*/
    private Color clearColor;

    private int width, height;

    /**
     * Crea un gestor per a controlar els grafics custom d'un jpanel extern.
     *
     * @param PanellObjectiu Panell on s'enganxará l'imatge resultant del update i render. MOLT IMPORTANT QUE EL PANELL TINGUI MIDA!
     * @param c Controlador que gestiona les interaccions(Mouse&Key listeners) de la persona amb el custom rendering panel.
     */
    public GraphicsManager(JPanel PanellObjectiu, GraphicsController c) {
        clearColor = Color.white;

        if(PanellObjectiu.getWidth() == 0 || PanellObjectiu.getHeight() == 0)
            System.out.println("Error ultrafatal. El panell que mhas donat no te mida especificada!"); //ets un primo arroyo
        JPanelObjectiu = PanellObjectiu;
        JPanelObjectiu.setBackground(Color.BLACK);
        JPanelObjectiu.setFocusable(true);
        JPanelObjectiu.requestFocus();
        registraControllador(c);
        controlador_extern = c;

        initGame();
    }

    /** Modifica el color del fons al borrar el contingut cada frame*/
    public void setClearColor(Color clearColor) {
        this.clearColor = clearColor;
    }

    public void resize(int width, int height) {
        image = JPanelObjectiu.createImage(width,height);
        JPanelObjectiu.updateUI();
    }

    private void initGame() {
        controlador_extern.init();
        running = true;
        thread = new Thread(this, "Game Thread");
        thread.start();
    }

    @Override
    public void run() {
        // These variables should sum up to 17 on every iteration
        long updateDurationMillis = 0; // Measures both update AND render
        long sleepDurationMillis = 0; // Measures sleep
        while (running) {
            long beforeUpdateRender = System.nanoTime();
            long deltaMillis = updateDurationMillis + sleepDurationMillis;

            updateAndRender(deltaMillis);

            updateDurationMillis = (System.nanoTime() - beforeUpdateRender) / 1000000L;
            sleepDurationMillis = Math.max(2, 17 - updateDurationMillis);

            try {
                Thread.sleep(sleepDurationMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void updateAndRender(long deltaMillis) {
        controlador_extern.update(deltaMillis / 1000f);
        prepareGameImage();
        controlador_extern.render(image.getGraphics());
        renderGameImage(JPanelObjectiu.getGraphics());
    }

    private void prepareGameImage() {
        int w = width <= 0 ? JPanelObjectiu.getWidth() : width;
        int h = height <= 0 ? JPanelObjectiu.getHeight() : height;
        if(image == null){
            image = JPanelObjectiu.createImage(w, h);//JPanelObjectiu.getWidth(), JPanelObjectiu.getHeight());
        }
        if (image.getWidth(null) != width || image.getHeight(null) != height) {
            image = JPanelObjectiu.createImage(w, h);
        }

        Graphics g = image.getGraphics();
        g.setColor(clearColor);
        g.fillRect(0, 0,width, height);//JPanelObjectiu.getWidth(), JPanelObjectiu.getHeight());
    }

    public void exit() {
        running = false;
    }

    private void renderGameImage(Graphics g) {
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
        g.dispose();
    }

    private void registraControllador(GraphicsController c) {
        if (JPanelObjectiu.getMouseListeners().length == 0)
            JPanelObjectiu.addMouseListener(c);

        if (JPanelObjectiu.getMouseMotionListeners().length == 0)
            JPanelObjectiu.addMouseMotionListener(c);

        if (JPanelObjectiu.getKeyListeners().length == 0)
            JPanelObjectiu.addKeyListener(c);
    }

    public void updateSize(int width, int height, boolean fully){
        this.width = width;
        this.height = height;
        JPanelObjectiu.setPreferredSize(new Dimension(width, height));
        if(fully)
            image = JPanelObjectiu.createImage(width, height);
    }
}