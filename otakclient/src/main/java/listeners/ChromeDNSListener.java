package listeners;

import callbacks.ChromeCastCallback;
import callbacks.HTTPCallback;
import callbacks.OtakServerFoundCallback;
import objects.ServerObject;
import org.json.JSONObject;
import requests.HTTPGet;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class ChromeDNSListener implements ServiceListener {

    private ChromeCastCallback chromeCastCallback;

    public ChromeDNSListener(ChromeCastCallback chromeCastCallback) {
        this.chromeCastCallback = chromeCastCallback;
    }

    @Override
    public void serviceAdded(ServiceEvent event) {
        event.getDNS().requestServiceInfo(event.getInfo().getType(), event.getName());
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {}

    @SuppressWarnings("deprecation")
    @Override
    public void serviceResolved(final ServiceEvent event) {
        chromeCastCallback.onFound(event);
    }
}
