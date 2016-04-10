package listeners;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class JmDNSListener implements ServiceListener {

    @Override
    public void serviceAdded(ServiceEvent event) {
        for (ServiceInfo.Fields s: event.getInfo().getQualifiedNameMap().keySet()) {
            System.out.println(event.getInfo().getQualifiedNameMap().get(s));
        }
        System.out.println("----------------");
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        System.out.println("Removed: " + event.getName());
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        System.out.println("Resolved: " + event.getInfo().getNiceTextString());
    }
}
