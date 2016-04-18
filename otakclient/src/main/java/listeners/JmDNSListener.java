package listeners;

import callback.HTTPCallback;
import callback.OtakServerFoundCallback;
import objects.ServerObject;
import org.json.JSONObject;
import requests.HTTPGet;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class JmDNSListener implements ServiceListener {

    private OtakServerFoundCallback otakServer;

    public JmDNSListener(OtakServerFoundCallback otakServer) {
        this.otakServer = otakServer;
    }

    @Override
    public void serviceAdded(ServiceEvent event) {
        event.getDNS().requestServiceInfo(event.getInfo().getType(), event.getName());
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        otakServer.onRemove(event.getDNS(), null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void serviceResolved(final ServiceEvent event) {
        boolean isHTTPS = event.getType().equals("_https._tcp.local.");

        String url;
        if (isHTTPS) {
            url = event.getInfo().getURL().replace("http://", "https://");
        } else {
            url = event.getInfo().getURL();
        }

        new HTTPGet(url).sendGet(new HTTPCallback() {
            @Override
            public void onSuccess(String IP, String response) {
                otakServer.onFound(event.getDNS(), parseJSON(url, response));
            }

            @Override
            public void onError() {
            }
        });
    }

    private ServerObject parseJSON(String IP, String response) {
        JSONObject jsonObject = new JSONObject(response);

        return new ServerObject(IP, jsonObject.getString("name"), jsonObject.getString("uuid"), jsonObject.getString("status"), jsonObject.getBoolean("setup"));
    }
}
