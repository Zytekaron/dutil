package tk.zytekaron.dutil.events;

import net.dv8tion.jda.api.events.GenericEvent;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Event<T extends GenericEvent> {
    private Predicate<T> predicate;
    private Consumer<T> handler;
    
    Event(Consumer<T> handler, Predicate<T> predicate) {
        this.predicate = predicate;
        this.handler = handler;
    }
    
    Event(Consumer<T> handler) {
        this(handler, __ -> true);
    }
    
    void handle(T event) {
        if (predicate.test(event)) {
            handler.accept(event);
        }
    }
}