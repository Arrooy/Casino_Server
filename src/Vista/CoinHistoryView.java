package Vista;

import Controlador.Controller;
import Controlador.Grafics_Controllers.CoinHistory.CoinHistoryController;
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

    /**
     * Mètode que inicia una coin history donat un nom d'usuari del que
     * obtindrà la informació necessària de la base de dades.
     * @param username Nom del usuari a generar la taula
     * @param width Amplada inicial de la pantalla
     * @param height Alçada inicial de la pantalla
     * @param info Informació de les transaccions
     */
    public void createCoinHistory(String username, int width, int height, LinkedList<Transaction> info) {
        if (chc == null) {
            chc = new CoinHistoryController(width, height, info, username, cg);
        } else {
            chc.initGraf(width, height, info, username);
        }

        gp = new GraphicsManager(this, chc);
    }

    public void closeView() {
        if(gp != null)  gp.exit();
        //gp = null;
        //chc = null;
        updateUI();
    }

    public void updateSize(boolean full) {
        if (gp != null) {
            chc.updateSize(getWidth(), getHeight());
            gp.updateSize(this.getWidth(), this.getHeight(), full);
            updateUI();
        }
    }

    @Override
    public void addController(Controller c) {
        addComponentListener(c);
        cg = c;
    }
}
