package Model;

/** SQL STATIC CLASS */

public class BaseDades {


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
