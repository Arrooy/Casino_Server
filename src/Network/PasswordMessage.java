package Network;

public class PasswordMessage extends Message {
    private String context;
    private boolean passwordOK;
    private String passwordError;

    public PasswordMessage(String context, String passwordError, boolean passwordOK){
        this.context = context;
        this.passwordError = passwordError;
        this.passwordOK = passwordOK;

    }

    public boolean isPasswordOK() {
        return passwordOK;
    }

    public String getPasswordError() {
        return passwordError;
    }


    @Override
    public String getContext() {
        return context;
    }

    @Override
    public double getID() {
        return 0;
    }
}
