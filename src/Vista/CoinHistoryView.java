package Vista;

import Controlador.Controller;
import Controlador.CustomGraphics.GraphicsManager;
import Controlador.Grafics_Controllers.CoinHistory.CoinHistoryController;
import Model.Database;
import Model.Transaction;

import java.util.LinkedList;

/**
 * Classe que gestiona els "coin history" del servidor.
 * Consisteix en un panell buit que s'afegeix en el card layout
 * de la finestra principal, per a carregar-hi un GraphicsController
 * en el moment en el que es vulgui accedir a la vista i visualitzar
 * el contingut corresponent.
 *
 * Important no crear coin history de no ser necessari, i tancar-lo
 * al deixar de veure-ho, per a optimitzar recursos.
 *
 * @version 2.0
 */
public class CoinHistoryView extends View {

    /**Panell que automatitza el funcionament dels grafics a mostrar*/
    private GraphicsManager gp;

    /**Controlador que dirigeix els elements a mostrar per pantalla*/
    private CoinHistoryController chc;

    private Controller cg;

    //Llista de prova
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

    /**
     * Mètode que inicia una coin history donat un nom d'usuari del que
     * obtindrà la informació necessària de la base de dades.
     * @param username Nom del usuari a generar la taula
     * @param width Amplada inicial de la pantalla
     * @param height Alçada inicial de la pantalla
     */
    public void createCoinHistory(String username, int width, int height) {
        LinkedList<Transaction> info = Database.getTransactions(username);

        chc = new CoinHistoryController(width, height, info, username, cg);
        gp = new GraphicsManager(this, chc);
    }

    public void closeView() {
        gp.exit();
        gp = null;
        chc = null;
        updateUI();
    }

    public void updateSize(boolean full) {
        if (gp != null) {
            chc.updateSize(getWidth(), getHeight());
            System.out.println("dins si");
            gp.updateSize(this.getWidth(), this.getHeight(), full);
            //gp.getCurrentDrawing().updateSize(getWidth(), getHeight());
            updateUI();
        }
        System.out.println("fora");
    }

    @Override
    public void addController(Controller c) {
        addComponentListener(c);
        cg = c;
    }
}
