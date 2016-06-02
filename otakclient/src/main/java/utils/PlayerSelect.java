package utils;

import java.io.IOException;

public class PlayerSelect {

    private String url;

    public PlayerSelect(String url) {
        this.url = url;
    }

    public void startVLC() {
        new Thread() {
            @Override
            public void run() {
                OSType os = new OSType();

                if (os.isWindows()) {
                    try {
                        ProcessBuilder processBuilder = new ProcessBuilder("C:/Program Files/VideoLAN/VLC/VLC.exe", url);
                        processBuilder.start();
                    } catch (IOException e) {
                        try {
                            ProcessBuilder processBuilder = new ProcessBuilder("C:/Program Files (x86)/VideoLAN/VLC/VLC.exe", url);
                            processBuilder.start();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            this.interrupt();
                        }
                    }
                } else {
                    try {
                        ProcessBuilder processBuilder = new ProcessBuilder("open", "-a", "VLC", url);
                        processBuilder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        this.interrupt();
                    }
                }

                this.interrupt();
            }
        }.start();
    }

    public void startQuickTime() {
        new Thread() {
            @Override
            public void run() {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder("open", "-a", "QuickTime Player", url);
                    processBuilder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    this.interrupt();
                }

                this.interrupt();
            }
        }.start();
    }
}
