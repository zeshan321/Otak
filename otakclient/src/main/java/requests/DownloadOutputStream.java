package requests;

import callback.DownloadCallback;
import org.apache.commons.io.output.CountingOutputStream;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;

public class DownloadOutputStream extends CountingOutputStream {

    public DownloadCallback callback;
    public int fileLength;
    private ActionListener listener = null;

    public DownloadOutputStream(OutputStream out, int fileLength, DownloadCallback callback) {
        super(out);

        this.fileLength = fileLength;
        this.callback = callback;
    }

    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    @Override
    protected void afterWrite(int n) throws IOException {
        super.afterWrite(n);
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, 0, null));
        }
    }

}