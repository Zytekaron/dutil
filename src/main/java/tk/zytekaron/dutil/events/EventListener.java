package tk.zytekaron.dutil.events;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventListener extends ListenerAdapter {
    private IdentityHashMap<Class<? extends GenericEvent>, Collection<Event<GenericEvent>>> events = new IdentityHashMap<>();
    
    public <T extends GenericEvent> SubscribedListener add(Class<T> type, Predicate<T> predicate, Consumer<T> consumer) {
        Collection<Event<GenericEvent>> collection = events.getOrDefault(type, new ArrayList<>());
        Event<T> listener = new Event<>(consumer, predicate);
        collection.add((Event<GenericEvent>) listener);
        events.put(type, collection);
        return () -> {
            Collection<Event<GenericEvent>> coll = events.get(type);
            coll.remove(listener);
        };
    }
    
    public <T extends GenericEvent> SubscribedListener add(Class<T> type, Consumer<T> consumer) {
        Collection<Event<GenericEvent>> collection = events.getOrDefault(type, new ArrayList<>());
        Event<T> listener = new Event<>(consumer);
        collection.add((Event<GenericEvent>) listener);
        events.put(type, collection);
        return () -> {
            Collection<Event<GenericEvent>> coll = events.get(type);
            coll.remove(listener);
        };
    }
    
    @Override
    public void onGenericEvent(GenericEvent event) {
        dispatch(event);
    }
    
    private <T extends GenericEvent> void dispatch(GenericEvent event) {
        Class<? extends GenericEvent> type = event.getClass();
        events.forEach((registered, coll) -> {
            if (registered.isAssignableFrom(type)) {
                coll.forEach(it -> it.handle(event));
            }
        });
    }
}