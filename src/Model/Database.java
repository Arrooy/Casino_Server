package Model;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;

import static Model.Casino_Server.WELCOME_GIFT;

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
public class Database {

    //   ---   CONSTANTS QUE INDIQUEN EL NOM DE LES COLUMNES DE LA TAULA DE LA BDD   ---   //
    private static final String CNAME_USERNAME = "username";
    private static final String CNAME_MAIL = "mail";
    private static final String CNAME_PASSWORD = "password";
    private static final String CNAME_WALLET = "wallet";
    private static final String CNAME_COINHISTORY = "coinHistory";

    //   ---   CONSTANTS QUE INDIQUEN LA INFORMACIO A CERCAR DE LA LLISTA QUE RETORNA LA FUNCIO GETUSERINFO()   ---   //
    public static final int INDEX_PASSWORD = 0;
    public static final int INDEX_MAIL = 1;
    public static final int INDEX_WALLET = 2;
    public static final int INDEX_COINHISTORY = 3;

    private static final String[] COLUMN_NAMES = {CNAME_USERNAME, CNAME_MAIL, CNAME_PASSWORD, CNAME_WALLET, CNAME_COINHISTORY};

    //   ---   INFORMACIÓ PER A ESTABLIR LA CONNEXIÓ AMB LA BASE DE DADES   ---   //
    private static final String host = "localhost";
    private static final String port = "3306";
    private static final String database = "Casino_Database";
    private static final String dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useServerPrepStmts=true&useSSL=false";
    private static final String username = "root";
    private static final String password = "casino";

    private static Connection conn;
    private static long lastID = 0;

    /**
     * Aquest mètode consisteix en una classe de filosofiacestàtica, tot i així és necessari
     * cridar aquest mètode en algun punt del codi previ a qualsevol ús d'alguna de les funcions
     * que ofereix la classe; ja que en aquest mètode s'estableix la connexió entre la base de
     * dades i el programa, que permetrà que aquest últim realitza peticions a la base de dades
     * per a consultar, inserir, eliminar o modificar informació.
     * @throws Exception En cas de fallar la inicialització del driver o la connexió al servidor
     */
    public static void initBaseDades() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Class.forName("com.mysql.jdbc.Connection");
        conn = DriverManager.getConnection(dbUrl, username, password);

        ResultSet rs = conn.createStatement().executeQuery("select id from Transaccions");
        while (rs.next()) lastID = rs.getLong("id");
    }

    private static long getLastID() {
        lastID++;
        return lastID;
    }

/*
@deprecated
    //Mètode que executa una Query a la base de dades per demanar informació.
    private static ResultSet selectQuery(String query){
        try {
            Statement s = conn.createStatement();
            return s.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Mètode que executa una Query a la base de dades per modificar informació.
    private static void insertQuery(String query){
        try {
            Statement s = conn.createStatement();
            s.executeUpdate(query);
        } catch (Exception e) {
            //TODO: gestionar degudament
            e.printStackTrace();
        }
    }
*/
    public static void insertNewUser(User user) throws Exception { // TODO: fer transacció inicial de cash
        conn.createStatement().executeUpdate("insert into Usuaris (username, mail, password) values ('" +
                user.getUsername() + "', '" +
                user.getMail() + "', '" +
                user.getPassword() + "')");

        conn.createStatement().executeUpdate("insert into Transaccions (id, earnings, type, username) values (" +
                getLastID() + ", '" +
                Casino_Server.WELCOME_GIFT + "', " +
                "'0', '" + user.getUsername() +"')");

        user.setWallet(Casino_Server.WELCOME_GIFT);
    }

    /**
     * Mètode per a actualitzar la informació d'un usuari a la base de dades.
     * S'utilitza com a referencia el nom d'usuari, per tant tot el que s'hagi modificat
     * de l'usuari en sí es reescriurà a la base de dades.
     * @param online Indica si l'usuari esta o no en línea
     * @param user Usuari a actualitzar
     */
    public static void updateUser(User user, boolean online) throws Exception {

        String lastLogin = online ? "" : "'null'";

        conn.createStatement().executeUpdate("update Usuaris set " +
                        "lastLogin = " + lastLogin + ", " +
                        CNAME_MAIL + "='" + user.getMail() + "', " +
                        CNAME_PASSWORD + "='" + user.getPassword() + "'" +
                        "where " + CNAME_USERNAME + "='" + user.getUsername() + "'");
    }

    /**
     * Mètode per a eliminar un usuari de la base de dades
     * @param user Usuari a eliminar
     * @deprecated No es pot eliminar a un usuari
     */
    public static void deleteUser(User user) throws Exception {
        deleteUser(user.getUsername());
    }

    /**
     * Mètode per a eliminar un usuari de la base de dades a partir del nom d'usuari
     * @param username Nom de l'usuari
     * @deprecated No es pot eliminar a un usuari
     */
    public static void deleteUser(String username) throws Exception {
        conn.createStatement().executeUpdate("delete from Usuaris where username='" + username + "'");
    }
/*
@deprecated
    //Mètode que obté la String a escriure a la base de dades a partir de la llista oroginal
    private static String coinHistoryToString(ArrayList<Long> coinHistory) {
        String s = coinHistory.size() > 1 ? coinHistory.get(0).toString() : "";
        for (int i = 1; i < coinHistory.size(); i++) s += "_" + coinHistory.get(i);
        return s;
    }
*/
    //Funció que indica si un nom correspon a una possible columna de la taula de la bdd
    private static boolean comprovaColumnName(String name) { //Near to deprecation
        boolean b = false;

        for (String s: COLUMN_NAMES) if (name.equals(s)) b = true;
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
     * @deprecated
     */
    public static LinkedList<String[]> getInfo(String ... columnNames) throws Exception {
        //Es fa la petició al servidor de la database
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Usuaris");

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

            return new LinkedList<>();
        }
    }

    public static LinkedList<String> getUserInfo(String username) throws SQLException{
        ResultSet rs = conn.createStatement().executeQuery(
                "select * from Usuaris where username='" + username + "'");

        LinkedList<String> info = new LinkedList<>();
        rs.next();
        info.add(rs.getString(CNAME_PASSWORD));
        info.add(rs.getString(CNAME_MAIL));
        info.add(rs.getString(CNAME_WALLET));
        info.add(rs.getString(CNAME_COINHISTORY));

        return info;
    }

    /**
     * Funció que retorna un array amb tota la evolució de la cartera del usuari
     * indicat.
     * @param username Nom del usuari del que es vol cercar la informació
     * @return Array amb tots els valors registrats de l'usuari indicat
     * @throws Exception En cas de no haver trobat l'usuari indicat
     * @deprecated
     */
    public static ArrayList<Long> getUserCoinHistory(String username) throws Exception {
        LinkedList<String[]> info = getInfo(CNAME_USERNAME, CNAME_COINHISTORY);
        String coinHistory = null;

        for (String[] s: info) if (s[0].equals(username)) coinHistory = s[1];
        if (coinHistory == null) throw new Exception("No s'ha trobat l'usuari");

        String[] coins = coinHistory.split("_");
        ArrayList<Long> parsedHistory = new ArrayList<>();

        for (int i = 0; i < coins.length; i++) parsedHistory.add(Long.parseLong(coins[i]));

        return parsedHistory;
    }

    public static void test() {
        try {
            ResultSet rs = conn.createStatement().executeQuery("select password from Usuaris where username = 'miquelsaula'");
            while(rs.next()) {
                System.out.println(rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     *  Verifica que les creedencials al interior de user son correctes.
     *  Retorna el user que s'ha verificat amb el camp CredentialsOk a true en cas afirmatiu.
     *  De lo contrari, areCredentialsOk equival false
     */
    public static User checkUserLogIn(User user){
        try {
            ResultSet rs = conn.createStatement().executeQuery("select password from Usuaris where username = '" + user.getUsername() + "'");
            rs.next();
            String pass = rs.getString("password");

            user.setCredentialsOk(user.getPassword().equals(pass));
            //user.setOnline(user.getPassword().equals(pass));

            return user;
        } catch (Exception e) {
            user.setCredentialsOk(false);
            //user.setOnline(false);
            return user;
        }
    }

    //Funció que converteix un coinHistory de format String a format usable
    private static ArrayList<Long> parseCoinHistory(String coinHistoryString) {
        String[] coins = coinHistoryString.split("_");
        ArrayList<Long> parsedHistory = new ArrayList<>();

        for (int i = 0; i < coins.length; i++) parsedHistory.add(Long.parseLong(coins[i]));

        return parsedHistory;
    }

    /**
     * Mètode per a completar la informació d'un usuari
     * @param user Usuari a completar
     * @throws Exception En cas de no coincidir la contrasenya
     */
    public static void fillUser(User user) throws Exception {
        LinkedList<String[]> info = getInfo("username", "password", "mail");

        for (String[] s: info) if (s[0].equals(user.getUsername())) {
            if (!user.getPassword().equals(s[1])) throw new Exception("No coincideix la contrassenya");
            user.setMail(s[2]);
            user.setWallet(getUserWallet(user.getUsername()));
        }
    }

    //Reconstrueix el wallet d'un usuari a partir de l'historial de transaccions
    private static long getUserWallet(String username) throws Exception {
        ResultSet rs = conn.createStatement().executeQuery("select earnings from Transactions where username = " + username);
        long wallet = 0;

        while (rs.next()) wallet += rs.getLong("earnings");

        return wallet;
    }

    public static boolean usernamePicked(String username) {
        try {
            ResultSet rs = conn.createStatement().executeQuery("select username from Usuaris");
            while (rs.next()) if (username.equals(rs.getString("username"))) return true;
        } catch (SQLException e) {
            //TODO
            e.printStackTrace();
        }
        return false;
    }

}