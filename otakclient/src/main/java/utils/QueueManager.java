package utils;

import callback.TaskCallback;
import controllers.HomeController;
import objects.QueueObject;

import java.util.TreeMap;

public class QueueManager {

    private HomeController homeController;
    private TreeMap<String, QueueObject> files = new TreeMap<>();
    private int currentThreads = 0;
    private int maxThreads = 0;

    public QueueManager(HomeController homeController) {
        this.homeController = homeController;

        // Get from config
        this.maxThreads = 5;

        // Start thread
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!(currentThreads >= maxThreads)) {
                        if (!(files.isEmpty())) {
                            String loc = files.firstKey();
                            QueueObject queueObject = files.get(loc);

                            // Update counter
                            currentThreads++;

                            // Remove from map
                            files.remove(loc);
                            switch (queueObject.type) {
                                case DOWNLOAD:
                                    homeController.downloadFile(queueObject.file, loc, new TaskCallback() {
                                        @Override
                                        public void onComplete() {
                                            currentThreads--;
                                        }
                                    });
                                    break;

                                case UPLOAD:
                                    if (queueObject.file.isDirectory()) {
                                        homeController.createFolder(queueObject.file, loc, new TaskCallback() {
                                            @Override
                                            public void onComplete() {
                                                currentThreads--;
                                            }
                                        });
                                    } else {
                                        homeController.uploadFile(queueObject.file, loc, new TaskCallback() {
                                            @Override
                                            public void onComplete() {
                                                currentThreads--;
                                            }
                                        });
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        }.start();
    }

    public void add(String loc, QueueObject queueObject) {
        files.put(loc, queueObject);
        homeController.runScript("addFileProgress('" + loc + "','queue');");
    }

    public void remove(String loc) {
        files.remove(loc);
    }

    public boolean contains(String loc) {
        return files.containsKey(loc);
    }
}
