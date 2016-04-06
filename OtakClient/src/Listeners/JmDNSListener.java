package Listeners;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class JmDNSListener implements ServiceListener {

    @Override
    public void serviceAdded(ServiceEvent event) {
    	System.out.println(event.getName());
    	System.out.println(event.getInfo().getKey());
    	System.out.println(event.getInfo().getQualifiedName());
    	System.out.println("-----");
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
    }
}
