package org.appkit.templating.event;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EventContexts {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	/** the no-op event context, events fired in this context will go nowhere */
	public static final EventContext NOOP = new NoOpEventContext();

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static final EventContext forSendingTo(final Object o) {
		return new LocalEventContext(o);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private static final class NoOpEventContext implements EventContext {

		/** {@inheritDoc} */
		@Override
		public void postEvent(final Object event) {}
	}

	/**
	 * An {@link EventContext} which delivers events to the target-object passed in the constructor.
	 * It uses event-bus, the target-object has to define public void methods with the desired
	 * event-type as single parameter and the {@link Subscribe} annotation.
	 */
	private static final class LocalEventContext implements EventContext {

		private static final Logger L   = LoggerFactory.getLogger(LocalEventContext.class);
		private final EventBus localBus;

		/**
		 * a new LocalEventContext, which delivers events to an object
		 */
		public LocalEventContext(final Object object) {
			this.localBus			    = new EventBus();
			this.localBus.register(object);
			this.localBus.register(this);
		}

		/**
		 * <b>Not intended for public use.</b>
		 * This is public for now, because of EventBus restrictions.
		 */
		@Subscribe
		public void deadLocalEvent(final DeadEvent event) {
			L.debug("dead local event: " + event.getEvent());
		}

		@Override
		public void postEvent(final Object event) {
			this.localBus.post(event);
		}
	}
}