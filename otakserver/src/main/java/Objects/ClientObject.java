package Objects;

import java.net.InetAddress;

public class ClientObject {

    public String UUID;
    public InetAddress IP;
    public int port;

    public ClientObject(String UUID, InetAddress IP, int port) {
        this.UUID = UUID;
        this.IP = IP;
        this.port = port;
    }
}
