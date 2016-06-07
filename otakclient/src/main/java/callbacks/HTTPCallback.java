package callbacks;

public abstract class HTTPCallback {

    public abstract void onSuccess(String IP, String response);

    public abstract void onError();
}
