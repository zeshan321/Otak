package objects;

public class ServerObject {

    public String name;
    public String UUID;
    public String status;
    public boolean setup;
    
    public ServerObject(String name, String UUID, String status, boolean setup) {
        this.name = name;
        this.UUID = UUID;
        this.status = status;
        this.setup = setup;
    }
}
