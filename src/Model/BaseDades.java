package Model;

import java.sql.*;
import java.util.LinkedList;

/**
 * Classe que gestiona la comunicació entre el programa i el servidor
 * de la base de dades. La classe implementa mètodes estàtics que permeten
 * interactuar amb la base de dades de la manera que es desitji.
 *
 * NOTA: Cal cridar el constructor obligatòriament abans de fer servir qualsevol
 * funció de la classe, ja que aquest estableix la connexió entre el programa i
 * la base de dades. El qual és essencial per a que funcioni tota la resta.
 *
 * @author Miquel Saula
 * @since 29/03/2018
 * @version 0.0.1
 */
public class BaseDades {

    public static final String CNAME_ID = "id";
    public static final String CNAME_NOM = "nom";
    public static final String CNAME_COGNOM = "cognoms";
    public static final String CNAME_USERNAME = "username";
    public static final String CNAME_MAIL = "mail";
    public static final String CNAME_PASSWORD = "password";
    public static final String CNAME_WALLET = "wallet";
    public static final String CNAME_COINHISTORY = "coinHistory";

    private static final String[] COLUMN_NAMES = {CNAME_ID, CNAME_NOM, CNAME_COGNOM, CNAME_USERNAME, CNAME_MAIL, CNAME_PASSWORD, CNAME_WALLET, CNAME_COINHISTORY};

    private static final String dbUrl = "jdbc:mysql://localhost:3306/Casino_Database";
    private static final String username = "root";
    private static final String password = "root";

    private static Connection conn;

    /**
     * Constructor de la classe. Tot i que aquesta consisteix en una classe de filosofia
     * estàtica, és necessari cridar aquest constructor en algun punt del codi previ a qualsevol
     * ús d'alguna de les funcions que ofereix la classe; ja que en aquest constructor s'estableix
     * la connexió entre la base de dades i el programa, que permetrà que aquest últim realitzi
     * peticions a la base de dades per a consultar, inserir, eliminar o modificar informació.
     */
    public BaseDades() {
        try {
            //TODO: Arreglar la connexió a la DataBase
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(dbUrl, username, password);
        } catch (Exception e) {
            //TODO: Fer algo aqui per a que no peti tot després
            e.printStackTrace();
        }
    }

    private static ResultSet insertQuery(String query){
        try {
            Statement s = conn.createStatement();
            return s.executeQuery(query);
        } catch (Exception e) {
            //TODO: gestionar degudament
            e.printStackTrace();
        }

        return null;
    }

    private static boolean comprovaColumnName(String name) {
        boolean b = false;

        for (String s: COLUMN_NAMES) b = name.equals(s);
        return b;
    }

    /**
     * Funció que serveix per a aconseguir un conjunt d'informació de la base de dades.
     * El seu funcionament consisteix en demanar tots els camps dels quals es requereixi la
     * informació com a paràmetres, i finalment s'obtindrà com a resultat un llistat amb
     * tota la informació organitzada.
     * Cal dir que aquesta funció només permet obtenir informació emmagatzemada com a Text,
     * és a dir que no es podràn obtenir les monedes (coin history sí).
     * @param columnNames Llistat de noms de les columnes a consultar
     * @return Llista d'arrays de Strings amb tots els camps demanats
     * @throws Exception En cas de demanar alguna informació inexistent.
     */
    public static LinkedList<String[]> getInfo(String ... columnNames) throws Exception {
        //Es fa la petició al servidor de la database
        ResultSet rs = insertQuery("SELECT * FROM `Usuaris`");

        //Es comprova que totes les columnes demanades siguin existents
        for (String s: columnNames) if (s.equals("wallet") || !comprovaColumnName(s))
            throw new Exception("La columna que es vol seleccionar no existeix");

        //Es recull tota la informació
        try {
            LinkedList<String[]> info = new LinkedList<>();

            while (rs.next()) {
                String[] aux = new String[columnNames.length];
                for (int i = 0; i < columnNames.length; i++) aux[i] = rs.getString(columnNames[i]);
                info.add(aux);
            }

            return info;
        } catch (Exception e) {
            //TODO: gestiona esto tete
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Funció que retorna un array amb tota la evolució de la cartera del usuari
     * indicat.
     * @param username Nom del usuari del que es vol cercar la informació
     * @return Array amb tots els valors registrats de l'usuari indicat
     * @throws Exception En cas de no haver trobat l'usuari indicat
     */
    public static long[] getUserCoinHistory(String username) throws Exception {
        LinkedList<String[]> info = getInfo(CNAME_USERNAME, CNAME_COINHISTORY);
        String coinHistory = null;

        for (String[] s: info) if (s[0].equals(username)) coinHistory = s[1];
        if (coinHistory == null) throw new Exception("No s'ha trobat l'usuari");

        String[] coins = coinHistory.split("_");
        long[] parsedHistory = new long[coins.length];

        for (int i = 0; i < coins.length; i++) parsedHistory[i] = Long.parseLong(coins[i]);

        return parsedHistory;
    }

    /**
     *  Verifica que les creedencials al interior de user son correctes.
     *  Retorna el user que s'ha verificat amb el camp CredentialsOk a true en cas afirmatiu.
     *  De lo contrari, areCredentialsOk equival false
     */
    public static User checkUserLogIn(User user){
        try {
            LinkedList<String[]> info = getInfo(CNAME_USERNAME, CNAME_MAIL, CNAME_PASSWORD);

            user.setCredentialsOk(false);
            user.setOnline(false);

            for (String[] s: info) if ((s[1].equals(user.getMail()) || s[0].equals(user.getUsername())) && s[2].equals(user.getPassword())) {
                user.setOnline(true);
                user.setCredentialsOk(true);
            }

            return user;
        } catch (Exception e) {
            user.setCredentialsOk(true);
            user.setOnline(true);
            return user;
        }
    }
}
