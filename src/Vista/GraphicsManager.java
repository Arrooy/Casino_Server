package Vista;

import Controlador.CustomGraphics.GraphicsController;

import javax.swing.*;
import java.awt.*;


/**
 * Classe que serveix com a intermediari entre un GraphicsController i una View,
 * ja que segons les instruccions del controlador hi pinta una imatge o altre al panell.
 *
 * El seu funcionament es basa en executar un fil d'execució indefinit, que actualitza la imatge
 * pintada per pantalla cada 17ms (teoricament), és a dir que es manté un framerate de 60fps.
 *
 * Al generar un GraphicsManager, se l'hi indica un Controlador que implementi la interficie
 * GraphicsController, ja que requereix usar tres funcions bàsiques que implementa la interficie.
 * La primera d'elles consisteix en init(), que s'executa al iniciar el panell. Seguidament s'inicia
 * el fil d'execució que constantment repeteix el procés d'executar la funció update() que actualitza
 * la lògica del panell gràfic i les funcionalitats necessàries, i seguidament el que es realitza
 * consisteix en executar la fincio render() del controlador, ja que aquesta el que realitza consisteix
 * en generar una imatge a partir de tots els components que ofereix la classe Graphics o bé Graphics2D.
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

    /** dimensions del panell */
    private int width, height;

    /**
     * Crea un gestor per a controlar els grafics custom d'un jpanel extern.
     *
     * @param PanellObjectiu Panell on s'enganxará l'imatge resultant del update i render. MOLT IMPORTANT QUE EL PANELL TINGUI MIDA!
     * @param c Controlador que gestiona les interaccions(Mouse i Key listeners) de la persona amb el custom rendering panel
     */
    public GraphicsManager(JPanel PanellObjectiu, GraphicsController c) {
        clearColor = Color.white;

        if(PanellObjectiu.getWidth() == 0 || PanellObjectiu.getHeight() == 0)
            System.out.println("Error ultrafatal. El panell que mhas donat no te mida especificada!"); //ets un primo arroyo ;adria:like :)
        JPanelObjectiu = PanellObjectiu;
        JPanelObjectiu.setBackground(Color.BLACK);
        JPanelObjectiu.setFocusable(true);
        JPanelObjectiu.requestFocus();
        registraControllador(c);
        controlador_extern = c;

        initGame();
    }

    /**
     * Mètode per a iniciar el funcionament del fil d'execució
     */
    private void initGame() {
        controlador_extern.init();
        running = true;
        thread = new Thread(this, "Game Thread");
        thread.start();
    }

    /**
     * Fil d'execució en el que cada 17ms com a mínim, a no ser que la complexitat del codi /
     * potencia de l'ordinador, no ho permeti, provocant que funcioni amb més lentitud.
     * El fil d'execució actualitza la la lògica i el contingut grafic del joc, i a més a més
     * calcula constantment la duració en l'execució de l'actualitzacio, amb la finalitat de
     * per una banda fer que l'ordinador descansi el temps suficient com per ajustar-se als
     * 17ms a cada iteració, i per altre, en cas d'anar més lent del previst, notificar-ho a la
     * funció update amb la variable delta.
     *
     * La delta consisteix en la duració total que ha requerit updateAndRender() en executar-se,
     * i per tant, en cas de modificar-se momentàniament la frequencia de fotogrames, la funcio
     * update() escalaria els valors necessaris, per a evitar que el mateix codi s'executi amb
     * velocitats diferents en ordinadors diferents, només per que un d'ells sigui més potent que
     * l'altre.
     */
    @Override
    public void run() {
        // Aquestes variables haurien de sumar 17 sempre
        long updateDurationMillis = 0; // Mesura el temps d'execució d'updte() i render()
        long sleepDurationMillis = 0; // Mesura el temps que hade dormir la iteració per ajustar-se

        //Bucle principal d'actualització i renderització del panell
        while (running) {
            //S'inicien els temps de la iteració
            long beforeUpdateRender = System.nanoTime();
            long deltaMillis = updateDurationMillis + sleepDurationMillis;

            //S'executen update() i render()
            updateAndRender(deltaMillis);

            //Es calcula la durada de U&R i el temps de repos
            updateDurationMillis = (System.nanoTime() - beforeUpdateRender) / 1000000L;
            sleepDurationMillis = Math.max(2, 17 - updateDurationMillis);

            //Es reposa el thread
            try {
                Thread.sleep(sleepDurationMillis);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    /**
     * S'actualitza la lògica del panell, seguidament es reinicia la imatge a mostrar per pantalla,
     * a continuació es genera la nova imatge a mostrar, i finalment es renderitza aquesta a la finestra
     * @param deltaMillis Temps que ha passat entre el moment actual i la útima actualització executada.
     */
    private void updateAndRender(long deltaMillis) {
        controlador_extern.update(deltaMillis / 1000f);
        prepareGameImage();
        if(image != null) controlador_extern.render(image.getGraphics());
        if(JPanelObjectiu != null) renderGameImage(JPanelObjectiu.getGraphics());
    }

    /**
     * Mètode que elimina el contingut del panell i el prepara per a pintar-hi nou contingut.
     */
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

    /**
     * Mètode per a finalitzar el funcionament del thread
     */
    public void exit() {
        running = false;
        thread.interrupt();
    }

    /**
     * Mètode que mostra per pantalla la imatge construida per la funcio render()
     * del controlador
     * @param g Element on s'ha pintat la imatge
     */
    private void renderGameImage(Graphics g) {
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
        g.dispose();
    }

    /**
     * Mètode per a registrar els listeners que utilitza el controlador, per a poder
     * controlar el ratoli i teclat de manera còmode des del propi Graphics controller
     * @param c Controlador a registrar
     */
    private void registraControllador(GraphicsController c) {
        if (JPanelObjectiu.getMouseListeners().length == 0)
            JPanelObjectiu.addMouseListener(c);

        if (JPanelObjectiu.getMouseMotionListeners().length == 0)
            JPanelObjectiu.addMouseMotionListener(c);

        if (JPanelObjectiu.getKeyListeners().length == 0)
            JPanelObjectiu.addKeyListener(c);
    }

    /**
     * Mètode que actualitza les dimensions del panell al realitzar-se un resize,
     * i ho notifica al controlador i a la vista.
     * @param width Nova amplitud
     * @param height Nova Alçada
     * @param fully Pantalla completa?
     */
    public void updateSize(int width, int height, boolean fully){
        this.width = width;
        this.height = height;
        JPanelObjectiu.setPreferredSize(new Dimension(width, height));
        if(fully)
            image = JPanelObjectiu.createImage(width, height);
    }
}