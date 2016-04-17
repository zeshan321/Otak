package callback;

public abstract class DownloadCallback {

    public abstract void onRequestComplete();

    public abstract void onProgress(int progress);

    public abstract void onRequestFailed();
}
