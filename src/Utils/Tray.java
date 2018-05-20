package Utils;

import Controlador.Controller;

import javax.swing.*;
import java.awt.*;

/** Gestor de la system Tray i de l'icono que apareix en ella*/
public class Tray {

    /** JButton per a sortir del casino*/
    private static MenuItem sortir;

    /** TrayIcon que apareix a la systemTray*/
    private static TrayIcon trayIcon;

    /** Si es possible crear la tray, es genera amb l'icono icon.png i es genera el boto per a sortir.
     * Finalment s'afegeix el boto a la Tray
     */
    public static void init(){
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(null,"Error adding tray","SystemTray is not supported",JOptionPane.ERROR_MESSAGE);
        }else{

            PopupMenu popup = new PopupMenu();

            trayIcon = new TrayIcon(new ImageIcon("data/ico.png").getImage());
            trayIcon.setImageAutoSize(true);

            SystemTray tray = SystemTray.getSystemTray();

            sortir = new MenuItem("Sortir del servidor");

            popup.add(sortir);

            trayIcon.setPopupMenu(popup);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.out.println("TrayIcon could not be added.");
            }
        }
    }

    /**
     * Realitza l'enllaç Vista amb Controlador
     * @param c controlador que gestiona el boto de la tray
     */
    public static void addController(Controller c) {
        sortir.addActionListener(c);
        sortir.setActionCommand("trayButtonExit");
    }

    /**
     * Genera una notificacio del sistema operatiu
     * @param title titol de la notificacio
     * @param content contingut d'aquesta
     */
    public static void showNotification(String title,String content){
        trayIcon.displayMessage(title,content, TrayIcon.MessageType.INFO);
    }

    /**
     * Borra l'icono de la tray
     */
    public static void exit(){
        SystemTray tray = SystemTray.getSystemTray();
        tray.remove(trayIcon);
    }
}