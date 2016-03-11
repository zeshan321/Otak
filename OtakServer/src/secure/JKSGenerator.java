package secure;

import java.io.File;

import utils.Config;

public class JKSGenerator {

	private Config config;
	private File file;
	
	public JKSGenerator() {
		config = new Config();
		file = new File(config.getString("dir") + File.separator + "keystore.jks");
	}
	
    public void generateKeyPair(){
    	new File("keystore.jks").delete();
    	
        String command = " -genkey "+
                         " -alias selfsigned "+
                         " -keyalg RSA "+
                         " -keysize 2048 "+
                         " -dname CN=Otak "+
                         " -storetype JKS "+
                         " -keypass "+ config.getString("pass") + " "+
                         " -keystore keystore.jks " +
                         " -storepass " + config.getString("pass");
        execute(command);
        
        new File("keystore.jks").renameTo(file);
    }
     
    private void execute(String command){
        try{
            sun.security.tools.keytool.Main.main(parse(command));
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
     
    private String[] parse(String command){
        String[] options = command.trim().split("\\s+");
        return options;
    }
}