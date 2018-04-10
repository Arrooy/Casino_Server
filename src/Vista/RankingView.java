package Vista;

import Controlador.Controller;

import javax.swing.*;
import java.awt.*;

public class RankingView extends View {
    private JTable jtRanking;
    private static final String[] columnNames = {"User Name",
                                                "Wallet",
                                                "Last Connection"};
    private Object[][] rankingData;
    private JButton jbBack;

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
        jlTitle.setFont(new Font("ArialBlack", Font.BOLD, 100));
        //Marges
        c.insets = new Insets(20,0,0,0);
        jpgblTitle.add(jlTitle, c);
        jpTitle.add(jpgblTitle);
        this.add(jpTitle, BorderLayout.NORTH);

        //Object[][] data = {};

        Object[][] data = {{"Meri", "500", "Connected"}, {"John", "20", "3 days ago"}, {"Sue", "50","Connected"}};

        rankingData = data;
        jtRanking = new JTable(rankingData, columnNames);
        jtRanking.setBorder(null);
        jtRanking.setColumnSelectionAllowed(false);
        jtRanking.setFocusable(false);
        jtRanking.setSurrendersFocusOnKeystroke(false);

        JPanel jpgblTaula = new JPanel(new GridBagLayout());
        jpgblTaula.setBorder(null);
        c.insets = new Insets(20, 20, 20, 20);
        c.fill = GridBagConstraints.BOTH;

        JScrollPane jspRank = new JScrollPane(jtRanking);
        jspRank.setBorder(null);
        jpgblTaula.add(jspRank, c);
        add(jpgblTaula, BorderLayout.CENTER);


        jbBack = new JButton("BACK");
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
    }

    @Override
    public void addController(Controller c) {
        //jtRanking.addMouseListener(c);
        jbBack.setActionCommand("returnMainView");
        jbBack.addActionListener(c);
    }

    public Object[] getData(){
        Object[] o = new Object[3];
        jtRanking.getSelectedRow();
        return o;
    }
}
