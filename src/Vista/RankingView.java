package Vista;

import Controlador.Controller;
import javafx.scene.layout.Border;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;

//TODO comentar totes les classes vista que he fet
//TODO UML

public class RankingView extends View {
    private JTable jtRanking;
    private static final String[] columnNames = {"User Name",
                                                "Wallet",
                                                "Last Connection"};
    private Object[][] rankingData;
    private JButton jbBack;
    private JButton jbViewGraghic;

    public RankingView(){
        this.setLayout(new BorderLayout());
        //Panell per col·locar el botó Menu a la part baixa a l'esquerra
        JPanel jpgblBack = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        //Marges
        c.insets = new Insets(20,20,20,0);
        c.fill = GridBagConstraints.BOTH;

        //Panell que té el títol de la pantalla a dalt a la dreta al mig
        JPanel jpTitle = new JPanel();
        JPanel jpgblTitle = new JPanel(new GridBagLayout());
        JLabel jlTitle = new JLabel("RANKING");
        jlTitle.setFont(new Font("ArialBlack", Font.BOLD, 24));
        //Marges
        c.insets = new Insets(20,0,0,0);
        jpgblTitle.add(jlTitle, c);
        jpTitle.add(jpgblTitle);
        this.add(jpTitle, BorderLayout.NORTH);

        Object[][] data = {};

        //Creació de la taula
        rankingData = data;
        jtRanking = new JTable(rankingData, columnNames);
        jtRanking.setColumnSelectionAllowed(false);
        jtRanking.setFocusable(false);
        jtRanking.setPreferredScrollableViewportSize(new Dimension(400,150));
        jtRanking.setDefaultEditor(Object.class, null);

        JPanel jpgblTaula = new JPanel(new GridBagLayout());
        jpgblTaula.setBorder(BorderFactory.createEmptyBorder());
        c.gridy = 0;
        c.insets = new Insets(0, 0, 10, 0);
        c.fill = GridBagConstraints.CENTER;

        JScrollPane jspRank = new JScrollPane(jtRanking);
        jspRank.setBorder(BorderFactory.createEmptyBorder());
        jpgblTaula.add(jspRank, c);

        //Botó per visualitzar la gràfica
        jbViewGraghic = new JButton("View selected graphic");
        jbViewGraghic.setFocusable(false);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        jpgblTaula.add(jbViewGraghic, c);

        add(jpgblTaula, BorderLayout.CENTER);

        //Botó per tornar al menú
        jbBack = new JButton("MENU");
        jbBack.setFocusable(false);
        jbBack.setPreferredSize(new Dimension(100,30));

        //Panell per col·locar el botó exit a la part baixa a l'esquerra
        JPanel jpgblExit = new JPanel(new GridBagLayout());
        //Marges
        c.insets = new Insets(0,20,20,0);
        c.fill = GridBagConstraints.BOTH;
        jpgblExit.add(jbBack, c);
        JPanel jpExit = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jpExit.add(jpgblExit);
        this.add(jpExit, BorderLayout.SOUTH);
        updateUI();
    }

    @Override
    public void addController(Controller c) {
        //TODO doble clik obre grafica
        //jtRanking.addMouseListener(c);
        jbBack.setActionCommand("returnMainView");
        jbBack.addActionListener(c);

        jbViewGraghic.setActionCommand("viewCoinBalance");
        jbViewGraghic.addActionListener(c);
    }

    public String getUsername(){
        return (String) jtRanking.getValueAt(jtRanking.getSelectedRow(),0);
    }

    public void updateTable(Object[][] objects){
        //TODO ordenar per dineros
        JTable aux = new JTable(objects, columnNames);
        jtRanking.setModel(aux.getModel());
        jtRanking.revalidate();
        updateUI();
    }
}
