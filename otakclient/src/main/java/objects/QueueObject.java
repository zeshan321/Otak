package objects;

import java.io.File;

public class QueueObject {

    public QueueType type;
    public File file;

    public enum QueueType {
        DOWNLOAD, UPLOAD
    }

    public QueueObject(QueueType type, File file) {
        this.type = type;
        this.file = file;
    }
}
