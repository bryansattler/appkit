package org.appkit.concurrent;

import java.util.concurrent.TimeUnit;


/**
 * A ticker that notifies stuff.
 *
 * @see SmartExecutor
 */
public interface Ticker {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/** Tells this Ticker to notify the {@link TickReceiver} */
	void startNotifiying(final TickReceiver receiver);

	/** Stops this Ticker */
	void stop();

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

	public interface TickReceiver {
		void tick();
	}

	public interface Supplier {
		Ticker createTicker(final long interval, final TimeUnit timeUnit);
	}
}