package utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.io.IOException;

public class PlayerSelect {

    String url;
    boolean isRunning = false;

    public PlayerSelect(String url) {
        this.url = url;
    }

    public void startVLC() {
        new Thread() {
            @Override
            public void run() {
                OSType os = new OSType();

                if (os.isWindows()) {
                    CommandLine cmdLine = CommandLine.parse("'C:/Program Files/VideoLAN/VLC/VLC.exe' '" + url + "'");
                    DefaultExecutor executor = new DefaultExecutor();
                    try {
                        if (executor.execute(cmdLine) == 0) {
                            isRunning = true;
                        }
                    } catch (IOException e) {
                        cmdLine = CommandLine.parse("'C:/Program Files (x86)/VideoLAN/VLC/VLC.exe' '" + url + "'");
                        executor = new DefaultExecutor();
                        try {
                            if (executor.execute(cmdLine) == 0) {
                                isRunning = true;
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            this.interrupt();
                        }
                    }
                } else {
                    CommandLine cmdLine = CommandLine.parse("open");
                    cmdLine.addArgument("-a 'VLC'");
                    cmdLine.addArgument(url);

                    DefaultExecutor executor = new DefaultExecutor();
                    try {
                        if (executor.execute(cmdLine) == 0) {
                            isRunning = true;
                        }
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
                CommandLine cmdLine = CommandLine.parse("open -a 'QuickTime Player'");
                DefaultExecutor executor = new DefaultExecutor();

                try {
                    if (executor.execute(cmdLine) == 0) {
                        isRunning = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    this.interrupt();
                }

                this.interrupt();
            }
        }.start();
    }
}
