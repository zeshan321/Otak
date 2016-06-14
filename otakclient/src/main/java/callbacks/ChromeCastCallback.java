package callbacks;

import javax.jmdns.ServiceEvent;

public abstract class ChromeCastCallback {

    public abstract void onFound(ServiceEvent event);

}
