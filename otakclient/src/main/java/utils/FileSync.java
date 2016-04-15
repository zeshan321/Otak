package utils;

import objects.FileObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FileSync implements Runnable {

    private Config config;
    private String json;

    public FileSync(String json) {
        this.json = json;
        config = new Config();
    }

    @Override
    public void run() {
        filesNeeded();
    }

    private List<FileObject> filesNeeded() {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("info");

        List<FileObject> files = new ArrayList<>();

        for (int n = 0; n < jsonArray.length(); n++) {
            JSONObject jsonObject1 = new JSONObject(jsonArray.getString(n));
            System.out.println(jsonObject1.getString("file"));
        }

        return files;
    }
}
