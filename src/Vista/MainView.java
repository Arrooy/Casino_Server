package Vista;

import Controlador.Controller;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {

    private JButton jbTop5;
    private JButton jbRankingBalance;

    /**
     *  Crea la vista del servidor amb una amplada i una alçada determinades per width i height
     * @param width indica l'amplada de la vista
     * @param height indica l'altura de la vista
     */

    public MainView(int width,int height){

        Tray.init();

        generaVista();

        //Es determinen les dimensions de la finestra
        setSize(width,height);

        //Es centra la finestra en el centre de la pantalla
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getSize().height / 2);
        setTitle("Casino_Servidor");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void generaVista(){

        this.setLayout(new BorderLayout());

        //Panell amb els botons de les opcions centrades al mig de la pantalla
        JPanel jpgblBotons = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        jbRankingBalance = new JButton("USERS RANKING & BALANCE");
        jbRankingBalance.setFocusable(false);
        jbTop5 = new JButton("TOP 5");
        jbTop5.setFocusable(false);

        //Marges
        c.insets = new Insets(0,0,0,20);

        //S'afegeixen els botons de les opcions
        c.gridy = 0;
        c.gridx = 0;
        jpgblBotons.add(jbRankingBalance, c);

        c.gridx = 1;
        jpgblBotons.add(jbTop5, c);

        this.add(jpgblBotons, BorderLayout.CENTER);

        //Panell amb el titol centrat al mig
        JPanel jpTitle = new JPanel();
        JPanel jpgblTitle = new JPanel(new GridBagLayout());
        JLabel jlTitle = new JLabel("MENU");
        jlTitle.setFont(new Font("ArialBlack", Font.BOLD, 24));
        c.insets = new Insets(20,0,0,0);
        jpgblTitle.add(jlTitle, c);
        jpTitle.add(jpgblTitle);
        this.add(jpTitle, BorderLayout.NORTH);
    }

    /** Afegeix el controlador del programa a la vista*/
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
