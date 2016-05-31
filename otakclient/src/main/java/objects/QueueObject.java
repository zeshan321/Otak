package objects;

import java.io.File;

public class QueueObject {

    public QueueType type;
    public File file;

    public QueueObject(QueueType type, File file) {
        this.type = type;
        this.file = file;
    }

    public enum QueueType {
        DOWNLOAD, UPLOAD, DELETE, TORRENT
    }
}
