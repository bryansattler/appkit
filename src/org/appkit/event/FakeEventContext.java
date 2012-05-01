package org.appkit.event;

/**
 * A no-op {@link EventContext}.
 */
public final class FakeEventContext implements EventContext {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/** {@inheritDoc} */
	@Override
	public void postEvent(final Object event) {}
}