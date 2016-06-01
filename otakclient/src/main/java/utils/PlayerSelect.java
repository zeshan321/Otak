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
                        String[] command = new String[]{
                                "C:/Program Files/VideoLAN/VLC/VLC.exe",
                                url
                        };

                        Runtime.getRuntime().exec(command);
                    } catch (IOException e) {
                        try {
                            String[] command = new String[]{
                                    "C:/Program Files (x86)/VideoLAN/VLC/VLC.exe",
                                    url
                            };

                            Runtime.getRuntime().exec(command);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            this.interrupt();
                        }
                    }
                } else {
                    try {
                        Runtime.getRuntime().exec("open -a VLC '" + url + "'");
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
                    String[] command = new String[]{
                            "open -a",
                            "QuickTime Player",
                            url
                    };

                    Runtime.getRuntime().exec(command);
                } catch (IOException e) {
                    e.printStackTrace();
                    this.interrupt();
                }

                this.interrupt();
            }
        }.start();
    }
}
