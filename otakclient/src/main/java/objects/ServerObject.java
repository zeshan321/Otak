package objects;

public class ServerObject {

    public String IP;
    public String name;
    public String UUID;
    public String status;
    public boolean setup;

    public ServerObject(String IP, String name, String UUID, String status, boolean setup) {
        this.IP = IP;
        this.name = name;
        this.UUID = UUID;
        this.status = status;
        this.setup = setup;
    }
}
