package secure;

import com.sun.net.httpserver.HttpsConfigurator;
import utils.Config;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class TLSHandler {

    public HttpsConfigurator createTLSContext() throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, KeyManagementException, UnrecoverableKeyException {
        Config config = new Config();

        char[] ksPassword = config.getString("pass").toCharArray();
        char[] ctPassword = config.getString("pass").toCharArray();

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(new File("keystore.jks")), ksPassword);

        KeyManagerFactory keyManager = KeyManagerFactory.getInstance("SunX509");
        keyManager.init(keyStore, ctPassword);

        TrustManagerFactory trustManager = TrustManagerFactory.getInstance("SunX509");
        trustManager.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);

        return new HttpsConfigurator(sslContext);
    }
}