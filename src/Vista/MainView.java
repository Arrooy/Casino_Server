package Vista;

import Controlador.Controller;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {

    /**
     *  Crea la vista del servidor amb una amplada i una alçada determinades per width i height
     * @param width indica l'amplada de la vista
     * @param height indica l'altura de la vista
     */

    public MainView(int width,int height){

        Tray.init();

        //Es determinen les dimensions de la finestra
        setSize(width,height);

        //Es centra la finestra en el centre de la pantalla
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getSize().height / 2);
        setTitle("Casino_Servidor");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public void addController(Controller c){
        Tray.addController(c);
        addWindowListener(c);
    }

    /** Obra una finestra indicant un error*/
    public void displayError(String title,String errorText) {
        JOptionPane.showMessageDialog(this,title,errorText,JOptionPane.ERROR_MESSAGE);
    }

    public boolean displayQuestion(String message) {
        //Retorna true si
        //Retorn false no
        return JOptionPane.showConfirmDialog(this,message,"Are you sure?",JOptionPane.YES_NO_OPTION) == 0;
    }
}
