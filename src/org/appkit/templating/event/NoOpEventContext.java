package org.appkit.templating.event;


/**
 * A no-op {@link EventContext}.
 */
public final class NoOpEventContext implements EventContext {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/** {@inheritDoc} */
	@Override
	public void postEvent(final Object event) {}
}