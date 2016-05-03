package utils;

import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.input.MouseEvent;

public class WebDispatcher implements EventDispatcher{

    private EventDispatcher eventDispatcher;

    public WebDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public Event dispatchEvent(Event event, EventDispatchChain tail) {
        MouseEvent mouseEvent = (MouseEvent) event;

        // Mouse drag
        if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
            event.consume();
        }

        // Double click
        if (mouseEvent.getClickCount() > 1) {
            event.consume();
        }

        return eventDispatcher.dispatchEvent(event, tail);
    }

}
