package utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Data {

    private File file;
    private String content;

    public Data() {
        try {
            String fileName = "serverfiles.properties";

            file = new File(fileName);

            // Create file if it doesn't exist
            file.createNewFile();

            // Load content
            content = IOUtils.toString(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getContent() {
        if (content.equals("") || content.isEmpty()) {
            return null;
        }

        return content;
    }

    public void set(String content) {
        this.content = content;
    }

    public void save() {
        try {
            PrintWriter printWriter = new PrintWriter(file);

            printWriter.println(content);

            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}