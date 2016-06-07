package callbacks;

public abstract class NetworkChangeCallback {

    public enum NetworkType {
        ONLINE, OFFLINE, UNKNOWN
    }

    public abstract void onNetworkChange(NetworkType networkType);
}
