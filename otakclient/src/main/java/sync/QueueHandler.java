package sync;

import objects.QueueObject;
import org.json.JSONObject;

import java.io.*;
import java.util.Scanner;

public class QueueHandler {

    private File file;

    public QueueHandler() {
        file = new File("otakqueue.properties");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void add(String type, String file) {
        try {
            PrintWriter printWriter = new PrintWriter(file);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", type);
            jsonObject.put("file", file);

            printWriter.println(jsonObject);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public QueueObject getNext() {
        try {
            Scanner scan = new Scanner(file);

            while(scan.hasNextLine()) {
                JSONObject jsonObject = new JSONObject(scan.nextLine());

                remove();
                return new QueueObject(jsonObject.getString("type"), jsonObject.getString("file"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void remove() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        //Initial write position
        long writePosition = raf.getFilePointer();
        raf.readLine();
        // Shift the next lines upwards.
        long readPosition = raf.getFilePointer();

        byte[] buff = new byte[1024];
        int n;
        while (-1 != (n = raf.read(buff))) {
            raf.seek(writePosition);
            raf.write(buff, 0, n);
            readPosition += n;
            writePosition += n;
            raf.seek(readPosition);
        }
        raf.setLength(writePosition);
        raf.close();
    }
}
