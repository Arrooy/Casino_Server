package Model;

import Utils.Seguretat;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
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
    private static final String host = "128.199.32.184";
    private static final String port = "3306";
    private static final String database = "Casino_Database";
    private static final String dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useServerPrepStmts=true&useSSL=false";
    private static final String username = "casino";
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
        //TODO: connect with json
        Class.forName("com.mysql.jdbc.Driver");
        Class.forName("com.mysql.jdbc.Connection");
        conn = DriverManager.getConnection(dbUrl, username, password);

        ResultSet rs = conn.createStatement().executeQuery("select id from Transactions");
        while (rs.next()) lastID = rs.getLong("id");
    }

    /**
     * Mètode que retorna el ID d'una nova transacció. Incrementa automàticament el valor
     * per automatitzar la obtenció de IDs.
     *
     * IMPORTANT: Aconseguir el ID nomès a partir d'aquesta funció, mai de l'atribut
     * @return ID d'una nova transacció
     */
    private static long getLastID() {
        lastID++;
        return lastID;
    }

    /**
     * Funció que afegeix un nou usuari a la base de dades a la taula d'Usuaris.
     * A més a més també afegeix una transacció inicial amb la quantitat de diners inicial,
     * i afegeix el nivell inicial de l'usuari.
     * La funció no comprova si l'usuai ja existeix, per tant en cas d'estar repetit generarà
     * una excepció al ser "username" una PK de la taula.
     * @param user Usuari a registrar
     * @throws Exception En cas d'estar repetit o bé de fallar una de les Querys.
     */
    public static void insertNewUser(User user) throws Exception {

        //Inserim l'usuari a Users
        conn.createStatement().executeUpdate("insert into Users (username, mail, password) values ('" +
                user.getUsername() + "', '" +
                user.getMail() + "', '" +
                user.getPassword() + "')");

        //Realitzem la transacció de diners inicials
        conn.createStatement().executeUpdate("insert into Transactions (id, earnings, type, username) values (" +
                getLastID() + ", '" +
                Casino_Server.WELCOME_GIFT + "', " +
                "'0', '" + user.getUsername() +"')");

        user.setWallet(Casino_Server.WELCOME_GIFT);

        //Iniciem l'usuari en el nivell inicial
        conn.createStatement().executeUpdate("insert into Levels (username, currentWallet) values ('" + user.getUsername() +
                "', '" + user.getWallet() + "')");
    }

    /**
     * Mètode per a actualitzar la informació d'un usuari a la base de dades.
     * S'utilitza com a referencia el nom d'usuari, per tant tot el que s'hagi modificat
     * de l'usuari en sí es reescriurà a la base de dades.
     *
     * Important: És necessari que l'usuari a modificar tingui com a mínim inserida la
     * informació del username i la password
     *
     * @param online Indica si l'usuari esta o no en línea
     * @param user Usuari a actualitzar
     */
    public static void updateUser(User user, boolean online) throws Exception {
        if (online) conn.createStatement().executeUpdate("update Users set " +
                        "lastLogin = NULL, " +
                        CNAME_PASSWORD + "='" + user.getPassword() + "'" +
                        "where " + CNAME_USERNAME + "='" + user.getUsername() + "'");
        else conn.createStatement().executeUpdate("update Users set " +
                "lastLogin = CURRENT_TIME, " +
                CNAME_PASSWORD + "='" + user.getPassword() + "'" +
                "where " + CNAME_USERNAME + "='" + user.getUsername() + "'");
    }

    /**
     * Mètode per a eliminar un usuari de la base de dades
     * @param user Usuari a eliminar
     * @deprecated No es pot eliminar a un usuari - test usage only
     */
    public static void deleteUser(User user) throws Exception {
        deleteUser(user.getUsername());
    }

    /**
     * Mètode per a eliminar un usuari de la base de dades a partir del nom d'usuari
     * @param username Nom de l'usuari
     * @deprecated No es pot eliminar a un usuari - test usage only
     */
    public static void deleteUser(String username) throws Exception {
        conn.createStatement().executeUpdate("delete from Users where username='" + username + "'");
    }

    /**
     *  Verifica que les creedencials al interior de user son correctes.
     *  Retorna el user que s'ha verificat amb el camp CredentialsOk a true en cas afirmatiu.
     *  De lo contrari, areCredentialsOk equival false
     */
    public static User checkUserLogIn(User user){
        try {
            ResultSet rs = conn.createStatement().executeQuery("select password from Users where username = '" + user.getUsername() + "'");
            rs.next();
            user.setCredentialsOk(Seguretat.desencripta( user.getPassword()).equals(Seguretat.desencripta(rs.getString("password"))));
            return user;
        } catch (Exception e) {
            user.setCredentialsOk(false);
            return user;
        }
    }

    /**
     * Mètode per a completar la informació d'un usuari
     * @param user Usuari a completar
     * @throws Exception En cas de no coincidir la contrasenya
     */
    public static void fillUser(User user) throws Exception {
        ResultSet rsm = conn.createStatement().executeQuery("select mail, password from Users where username = '" + user.getUsername() + "';");
        rsm.next();

        if (!rsm.getString("password").equals(user.getPassword())) throw new Exception("Incorrect password");

        user.setMail(rsm.getString("mail"));
        user.setWallet(getUserWallet(user.getUsername()));
    }

    /**
     * Reconstrueix el wallet d'un usuari a partir de l'historial de transaccions
     * @param username nom del usuari del que es desitja obtenir els diners
     * @return diners actuals de l'usuari
     * @throws Exception En cas de no existir l'usuari o fallar la connexió
     */
    public static long getUserWallet(String username) throws Exception {
        ResultSet rs = conn.createStatement().executeQuery("select earnings from Transactions where username = '" + username + "';");
        long wallet = 0;

        while (rs.next()) wallet += rs.getLong("earnings");
        return wallet;
    }

    /**
     * Mètode per a comprovar si un nom ja està usat per un usuari
     * @param username Nom a comprovar
     * @return Si ja està agafat el nom
     */
    public static boolean usernamePicked(String username) {
        try {
            ResultSet rs = conn.createStatement().executeQuery("select username from Users");
            while (rs.next()) if (username.equals(rs.getString("username"))) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mètode per a comprovar si un nom ja està usat per un usuari
     * @param mail Nom a comprovar
     * @return Si ja està agafat el nom
     */
    public static boolean mailPicked(String mail) {
        try {
            ResultSet rs = conn.createStatement().executeQuery("select mail from Users");
            while (rs.next()) if (mail.equals(rs.getString("mail"))) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mètode que registra una nova transacció a la base de dades.
     * @param transaction Transacció a registrar
     */
    public static void registerTransaction(Transaction transaction) {
        try {
            conn.createStatement().executeUpdate("insert into Transactions (username, id, earnings, type) values ('" + transaction.getUsername()
                    + "', '" + getLastID()
                    + "', '" + transaction.getGain()
                    + "', '" + transaction.getType() + "');");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mètode que donada una referencia de temps en format Timestamp,
     * obté una representació "user fiendly" del temps que ha passat
     * des del moment indicat fins al moment actual.
     * @param back Referencia de temps
     * @return Representació del temps passat
     */
    public static String getPassedTime(Timestamp back) {
        Timestamp now = Timestamp.from(Instant.now());

        long diff = (now.getTime() - (7200000) - back.getTime()) / 1000;

        if (diff < 60) return diff + "s ago";
        else if (diff < 60 * 60) return diff/60 + "min ago";
        else if (diff < 60 * 60 * 24) return diff/(60*60) + "h ago";
        else if (diff < 60 * 60 * 24 * 2) return diff/(60*60*24) + "day ago";
        else if (diff < 60 * 60 * 24 * 7) return diff/(60*60*24) + "days ago";
        else if (diff < 60 * 60 * 24 * 30) return diff/(60*60*24*7) + "weeks ago";
        else if (diff < 60 * 60 * 24 * 365) return diff/(60*60*24*30) + "moths ago";
        else return diff/(60*60*24*365) + "years ago";
    }

    public static String getDelayedTime(Timestamp back, int hours) {
        Timestamp now = Timestamp.from(Instant.now());
        System.out.print(now.getTime() + "  vs.  ");
        now.setTime(now.getTime() + hours * 1000 * 60 * 60);

        System.out.println(now.getTime());
        return getPassedTime(now);
    }

    /**
     * Busca en la DB el nivell del usuari. D'aquesta manera l'usuari te un color del
     * dorsal de les seves cartes segons el nivell amb el que es troba
     * @param username Usuari que vol obtenir una carta
     * @return El color del dorsal que ha d'obtenir, basat en el seu nivell actual.
     */
    public static String getUserColor(String username){
        try{
            int levelMax = 0;
            //Es demana a la DB l'evolucio del nivell de l'usuari amb el temps
            ResultSet rs = conn.createStatement().executeQuery("select level from Levels where username='" + username + "';");

            //S'agafa el nivell maxim d'aquesta evolucio
            while(rs.next()){
                if (rs.getInt("level") > levelMax) {
                    levelMax = rs.getInt("level");
                }
            }

            //Es retorna un color o unaltre, segons el nivell
            if(levelMax == 1)
                return "back-red.png";
            else if(levelMax == 2)
                return "back-orange.png";
            else if(levelMax == 4)
                return "back-blue.png";
            else if(levelMax == 5)
                return "back-green.png";
            else if(levelMax == 6)
                return "back-purple.png";
            else return "back-black.png";

        }catch (SQLException e){
            e.printStackTrace();
        }
        //Si no s'ha trobar l'usuari o ha sorgir algun error, es retorna el color del nivell 1
        return "back-red.png";
    }

    /**
     * Funció que retorna una taula amb tota la informació necessària a mostrar
     * a la taula de ranking d'usuaris
     * @return Matriu dobjectes amb la informació a mostrar
     */
    public static Object[][] getInfoRank(){
        try {
            ResultSet rs = conn.createStatement().executeQuery("select * from Users;");
            LinkedList<Object[]> users = new LinkedList<>();

            while(rs.next()){
                Object[] s = new Object[3];

                s[0] = rs.getString("username");

                try {
                    s[1] = getUserWallet((String) s[0]);
                } catch (Exception e) {
                    s[1] = 0;
                }

                s[2] = getPassedTime(rs.getTimestamp("lastLogin"));//getDelayedTime(rs.getTimestamp("lastLogin"), -2);

                users.add(s);
            }

            Object[][] list = new Object[users.size()][3];

            for(int i = 0; i < users.size(); i++) for (int j = 0; j < 3; j++) list[i][j] = users.get(i)[j];
            return list;
        } catch (Exception e) {
            System.out.println("Hellowis Bebitos " + e.getMessage());
            e.printStackTrace();
            Object[][] data = {};
            return  data;
        }
    }

    /**
     * Mètode que retorna el llistat de transaccions realitzades per l'usuari indicat
     * @param username Usuari del que es vol llistar les transaccions
     * @return Llista de transaccions
     */
    public static LinkedList<Transaction> getTransactions(String username) {
        try {
            LinkedList<Transaction> trans = new LinkedList<>();
            ResultSet rs = conn.createStatement().executeQuery("select * from Transactions where username = '" + username + "';");

            while (rs.next()) {
                Transaction t = new Transaction(rs.getLong("earnings"), rs.getTimestamp("date"), rs.getInt("type"));
                trans.add(t);
            }

            return trans;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User[] getTop(int type) {
        User[] top = new User[5];

        try {
            LinkedList<User> users = new LinkedList<>();
            ResultSet rs = conn.createStatement().executeQuery("select * from Transactions where type = '" + type + "';");
            while (rs.next()) {
                boolean found = false;
                //System.out.println("NAME:" + rs.getString("username"));

                for (User u: users) if (rs.getString("username").equals(u.getUsername())) {
                    u.setWallet(u.getWallet() + rs.getLong("earnings"));
                    found = true;
                }

                if (!found) {
                    User aux = new User();
                    aux.setUsername(rs.getString("username"));
                    aux.setWallet(rs.getLong("earnings"));
                    users.add(aux);
                }
            }

            for (int i = 0; i < 5; i++) {
                User max = new User();
                max.setWallet(-1);
                for (User u: users) max = u.getWallet() > max.getWallet() ? u : max;

                top[i] = max;
                users.remove(max);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return top;
    }
}