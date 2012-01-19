package org.uilib.application;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LocalEventContext implements EventContext {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(LocalEventContext.class);

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

	// FIXME: that makes no sense since we catch Object
	@Subscribe
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