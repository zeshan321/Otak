package objects;

public class FileObject {

    public String file;
    public boolean isDir;
    public long timestamp;

    public FileObject(String file, boolean isDir, long timestamp) {
        this.file = file;
        this.isDir = isDir;
        this.timestamp = timestamp;
    }
}
