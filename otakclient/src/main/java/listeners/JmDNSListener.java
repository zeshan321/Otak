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
        String url;
        if (event.getInfo().getURL().startsWith("http://")) {
            url = event.getInfo().getURL().replace("http://", "https://");
        } else {
            url = event.getInfo().getURL();
        }

        new HTTPGet(url).sendGet(new HTTPCallback() {
            @Override
            public void onSuccess(String IP, String response) {
                System.out.println("Test");
                otakServer.onFound(event.getDNS(), parseJSON(response));
            }

            @Override
            public void onError() {
            }
        });
    }

    private ServerObject parseJSON(String response) {
        JSONObject jsonObject = new JSONObject(response);

        return new ServerObject(jsonObject.getString("name"), jsonObject.getString("uuid"), jsonObject.getString("status"), jsonObject.getBoolean("setup"));
    }
}
