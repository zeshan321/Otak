package callback;

import objects.ServerObject;

import javax.jmdns.JmDNS;

public abstract class OtakServerFoundCallback {

    public abstract void onFound(JmDNS jmDNS, ServerObject serverObject);

    public abstract void onRemove(JmDNS jmDNS, ServerObject serverObject);

}
