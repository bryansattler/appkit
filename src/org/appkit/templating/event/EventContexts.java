package org.appkit.templating.event;

public final class EventContexts {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	/** the no-op event context, events fired in this context will go nowhere */
	public static final EventContext NOOP = new NoOpEventContext();

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static final EventContext forSendingTo(final Object o) {
		return new LocalEventContext(o);
	}
}