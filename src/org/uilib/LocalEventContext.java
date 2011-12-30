package org.uilib;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.apache.log4j.Logger;

public final class LocalEventContext implements EventContext {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = Logger.getLogger(LocalEventContext.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final EventBus localBus;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public LocalEventContext(final Object o) {
		this.localBus			  = new EventBus();
		this.localBus.register(o);
		this.localBus.register(this);
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Subscribe
	public void localEvent(final Object event) {
		L.debug("local event: " + event);
	}

	@Subscribe
	// FIXME: that makes no sense since we catch Object
	public void deadLocalEvent(final DeadEvent event) {
		L.debug("dead local event: " + event);
	}

	@Override
	public void postEvent(final Object event) {
		this.localBus.post(event);
	}

	@Override
	public void initController(final Controller subController) {
		throw new IllegalStateException("this is not a real context");
	}

	@Override
	public void backgroundTask(final Object task) {
		throw new IllegalStateException("this is not a real context");
	}

	@Override
	public void postLocal(final Object response) {
		throw new IllegalStateException("this is not a real context");
	}
}