package Model;

import java.sql.*;
import java.util.LinkedList;

/** SQL STATIC CLASS */

public class BaseDades {

    private static final String dbUrl = "jdbc:mysql://localhost:3306/Casino_Database";
    private static final String username = "root";
    private static final String password = "root";

    private static Connection conn;

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
        final String[] pos = {"id", "nom", "cognoms", "username", "mail", "password", "wallet", "coinHistory"};
        boolean b = false;

        for (String s: pos) b = name.equals(s);
        return b;
    }

    /**
     * Funció que serveix per a aconseguir un conjunt d'informació de la base de dades.
     * El seu funcionament consisteix en demanar tots els camps dels quals es requereixi la
     * informació com a paràmetres, i finalment s'obtindrà com a resultat un llistat amb
     * tota la informació organitzada.
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
     *  Verifica que les creedencials al interior de user son correctes.
     *  Retorna el user que s'ha verificat amb el camp CredentialsOk a true en cas afirmatiu.
     *  De lo contrari, areCredentialsOk equival false
     */

    public static User checkUserLogIn(User user){
        // Testing
        //TODO: verificar good username/email i password
        user.setCredentialsOk(true);
        user.setOnline(true);
        return user;
    }

}
