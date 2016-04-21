package sync;

import callback.CompareCallback;
import objects.FileObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Compare {

    private Config config;
    private String server;

    public Compare(Config config, String server) {
        this.config = config;
        this.server = server;
    }

    /**
     * Compare two json arrays to see which files need to be uploaded or downloaded
     */
    public void compareData(CompareCallback compareCallback) {
        List<FileObject> filesUpload = new ArrayList<>();
        List<FileObject> filesDownload = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(server);
        JSONArray serverArray = jsonObject.getJSONArray("info");
        JSONArray clientArray = generateClient();

        for (int n = 0; n < clientArray.length(); n++) {
            JSONObject clientFile = clientArray.getJSONObject(n);

            String file = clientFile.getString("file");
            boolean isDir = clientFile.getBoolean("dir");
            long timestamp = clientFile.getLong("timestamp");
            boolean found = false;

            for (int e = 0; e < serverArray.length(); e++) {
                JSONObject serverFile = serverArray.getJSONObject(e);

                if (file.equals(serverFile.getString("file"))) {
                    if (!isDir) {
                        if (timestamp == serverFile.getLong("timestamp")) {
                            found = true;
                        }

                        if (timestamp > serverFile.getLong("timestamp")) {
                            break;
                        } else {
                            found = true;
                        }
                    } else {
                        found = true;
                    }
                    break;
                }
            }

            if (!found) {
                filesUpload.add(new FileObject(file, isDir, timestamp));
            }
        }

        for (int n = 0; n < serverArray.length(); n++) {
            JSONObject serverFile = serverArray.getJSONObject(n);

            String file = serverFile.getString("file");
            boolean isDir = serverFile.getBoolean("dir");
            long timestamp = serverFile.getLong("timestamp");
            boolean found = false;

            for (int e = 0; e < clientArray.length(); e++) {
                JSONObject clientFile = clientArray.getJSONObject(e);

                if (file.equals(clientFile.getString("file"))) {
                    if (!isDir) {
                        if (timestamp == clientFile.getLong("timestamp")) {
                            found = true;
                        }

                        if (timestamp > clientFile.getLong("timestamp")) {
                            break;
                        } else {
                            found = true;
                        }
                    } else {
                        found = true;
                    }
                    break;
                }
            }

            if (!found) {
                filesDownload.add(new FileObject(file, isDir, timestamp));
            }
        }

        compareCallback.onComplete(filesDownload, filesUpload);
    }


    /**
     * Created json array of files from client
     */
    private JSONArray generateClient() {
        JSONArray jsonArray = new JSONArray();

        Collection<File> filesList = FileUtils.listFilesAndDirs(new File(config.getString("dir")), TrueFileFilter.TRUE, TrueFileFilter.TRUE);

        for (File fileIter : filesList) {
            String fileName = fileIter.getAbsolutePath().replace(config.getString("dir"), "").replaceAll("\\\\", "/");

            if (!fileName.equals("")) {
                if (fileIter.isDirectory()) {
                    jsonArray.put(jsonOutput(fileName, fileIter.lastModified(), true));
                } else {
                    jsonArray.put(jsonOutput(fileName, fileIter.lastModified(), false));
                }
            }
        }

        return jsonArray;
    }

    private JSONObject jsonOutput(String file, long timestamp, boolean isDir) {
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("file", file);
            jsonObject.put("timestamp", timestamp);
            jsonObject.put("dir", isDir);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

}
