package net.sopepsi.server.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.sopepsi.api.Logger;
import net.sopepsi.api.Plugin;
import net.sopepsi.api.event.Event;
import net.sopepsi.api.event.EventBus;
import net.sopepsi.api.event.EventHandler;
import net.sopepsi.api.event.EventPriority;
import net.sopepsi.api.event.Listener;

public final class EventBusImpl implements EventBus {

	private static final Logger LOG = new Logger("EventBus");

	private final List<RegisteredListener> handlers = new ArrayList<RegisteredListener>();
	private final Map<Class<?>, List<RegisteredListener>> handlersByEventType = new HashMap<Class<?>, List<RegisteredListener>>();
	private final Comparator<RegisteredListener> priorityComparator = new Comparator<RegisteredListener>() {
		public int compare(RegisteredListener a, RegisteredListener b) {
			return a.priority.ordinal() - b.priority.ordinal();
		}
	};

	@Override
	public void register(Listener listener, Plugin plugin) {
		for(Method method : listener.getClass().getMethods()) {
			EventHandler annotation = method.getAnnotation(EventHandler.class);
			if(annotation == null) {
				continue;
			}
			Class<?>[] params = method.getParameterTypes();
			if(params.length != 1 || !Event.class.isAssignableFrom(params[0])) {
				LOG.warning("Invalid @EventHandler on " + listener.getClass().getName() + "#" + method.getName());
				continue;
			}
			if(!method.isAccessible()) {
				method.setAccessible(true);
			}
			
			RegisteredListener registered = new RegisteredListener(plugin, listener, method, params[0], annotation.priority(), annotation.ignoreCancelled());
			this.handlers.add(registered);
			
			// Add to event type cache and sort
			Class<?> eventType = registered.eventType;
			List<RegisteredListener> eventHandlers = this.handlersByEventType.get(eventType);
			if(eventHandlers == null) {
				eventHandlers = new ArrayList<RegisteredListener>();
				this.handlersByEventType.put(eventType, eventHandlers);
			}
			eventHandlers.add(registered);
			Collections.sort(eventHandlers, this.priorityComparator);
		}
	}

	@Override
	public void unregister(Plugin plugin) {
		Iterator<RegisteredListener> iterator = this.handlers.iterator();
		while(iterator.hasNext()) {
			RegisteredListener listener = iterator.next();
			if(listener.plugin == plugin) {
				iterator.remove();
				// Also remove from event type cache
				List<RegisteredListener> eventHandlers = this.handlersByEventType.get(listener.eventType);
				if(eventHandlers != null) {
					eventHandlers.remove(listener);
				}
			}
		}
	}

	@Override
	public void call(Event event) {
		// Get pre-sorted handlers for this event type (no sorting on call)
		List<RegisteredListener> eventHandlers = this.handlersByEventType.get(event.getClass());
		if(eventHandlers == null || eventHandlers.isEmpty()) {
			return;
		}
		
		for(RegisteredListener entry : eventHandlers) {
			if(event.isCancelled() && entry.ignoreCancelled) {
				continue;
			}
			try {
				entry.method.invoke(entry.listener, event);
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Error in event handler " + entry.listener.getClass().getName() + "#" + entry.method.getName(), e);
			}
		}
	}

	private static final class RegisteredListener {
		final Plugin plugin;
		final Listener listener;
		final Method method;
		final Class<? extends Event> eventType;
		final EventPriority priority;
		final boolean ignoreCancelled;

		RegisteredListener(Plugin plugin, Listener listener, Method method, Class<?> eventType,
				EventPriority priority, boolean ignoreCancelled) {
			this.plugin = plugin;
			this.listener = listener;
			this.method = method;
			this.eventType = eventType.asSubclass(Event.class);
			this.priority = priority;
			this.ignoreCancelled = ignoreCancelled;
		}
	}
}
