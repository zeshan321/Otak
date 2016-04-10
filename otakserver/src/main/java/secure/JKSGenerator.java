package secure;

import java.io.File;

public class JKSGenerator {

    private String password;

    public JKSGenerator(String password) {
        this.password = password;
    }

    public void generateKeyPair() {
        new File("keystore.jks").delete();

        String command = " -genkey " +
                " -alias selfsigned " +
                " -keyalg RSA " +
                " -keysize 2048 " +
                " -dname CN=Otak " +
                " -storetype JKS " +
                " -keypass " + password + " " +
                " -keystore keystore.jks " +
                " -storepass " + password;
        execute(command);
    }

    private void execute(String command) {
        try {
            sun.security.tools.keytool.Main.main(parse(command));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String[] parse(String command) {
        String[] options = command.trim().split("\\s+");
        return options;
    }
}