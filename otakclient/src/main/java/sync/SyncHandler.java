package sync;

import callback.CompareCallback;
import callback.DownloadCallback;
import objects.FileObject;
import requests.HTTPDownload;
import utils.Config;

import java.io.File;
import java.util.List;

public class SyncHandler implements Runnable {

    private Config config;
    private String json;
    private String IP;
    private String pass;

    public SyncHandler(String json) {
        this.config = new Config();
        this.json = json;
        this.IP = config.getString("IP");
        this.pass = config.getString("pass");
    }

    @Override
    public void run() {
        new Compare(config, json).compareData(new CompareCallback() {
            @Override
            public void onComplete(List<FileObject> filesDownload, List<FileObject> filesUpload) {
                if (filesDownload == null && filesUpload == null) {
                    System.out.println("Download: ");
                    for (FileObject fileObject : filesDownload) {
                        // Download file
                        downloadFile(fileObject);
                    }

                    System.out.println("\nUpload: ");
                    for (FileObject fileObject : filesUpload) {
                        System.out.println(fileObject.file);
                    }
                } else {
                    System.out.println("No sync needed!");
                }
            }
        });
    }

    /**
     * Downloads file from Otak Server if the file is not a directory.
     * If it is a directory it will be created.
     */
    private void downloadFile(FileObject fileObject) {
        File file = new File(config.getString("dir") + File.separator + fileObject.file);

        if (fileObject.isDir) {
            file.mkdirs();

            // Set last modified to correct timestamp
            file.setLastModified(fileObject.timestamp);
        } else {
            new HTTPDownload(IP + "/download?pass=" + pass + "&file=" + fileObject.file).downloadFile(file, new DownloadCallback() {
                @Override
                public void onRequestComplete() {
                    // Set last modified to correct timestamp
                    file.setLastModified(fileObject.timestamp);
                }

                @Override
                public void onProgress(int progress) {

                }

                @Override
                public void onRequestFailed() {

                }
            });
        }

    }
}
