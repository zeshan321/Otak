package Objects;

import java.net.InetAddress;

public class ClientObject {

    public InetAddress IP;
    public int port;

    public ClientObject(InetAddress IP, int port) {
        this.IP = IP;
        this.port = port;
    }
}
