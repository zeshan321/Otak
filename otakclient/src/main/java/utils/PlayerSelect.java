package utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import views.StreamView;

import java.io.IOException;

public class PlayerSelect {

    String url;
    boolean isRunning = false;

    public PlayerSelect(String url) {
        this.url = url;
    }

    public void startPlayer() {
        new Thread() {
            @Override
            public void run() {
                OSType os = new OSType();

                if (os.isMac()) {
                    System.out.println("open -a \"Quicktime Player\" \"" + url + "\"");
                    CommandLine cmdLine = CommandLine.parse("open -a \"Quicktime Player\" \"" + url + "\"");
                    DefaultExecutor executor = new DefaultExecutor();

                    try {
                        if (executor.execute(cmdLine) == 0) {
                            isRunning = true;
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (os.isWindows()) {
                    // 64 bit
                    CommandLine cmdLine = CommandLine.parse("\"C:/Program Files/VideoLAN/VLC/VLC.exe\" \"" + url + "\"");
                    DefaultExecutor executor = new DefaultExecutor();
                    try {
                        if (executor.execute(cmdLine) == 0) {
                            isRunning = true;
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // 32 bit
                    if (!isRunning) {
                        cmdLine = CommandLine.parse("\"C:/Program Files (x86)/VideoLAN/VLC/VLC.exe\" \"" + url + "\"");
                        executor = new DefaultExecutor();
                        try {
                            if (executor.execute(cmdLine) == 0) {
                                isRunning = true;
                                return;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Use Otak Player if VLC or QuickTime are not found.
                if (!isRunning) {
                    new StreamView(url).run();
                }
            }
        }.start();
    }
}
