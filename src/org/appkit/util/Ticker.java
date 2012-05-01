package org.appkit.util;


/**
 * A ticker that notifies stuff.
 *
 * @see SmartExecutor
 */
public interface Ticker {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/** Tells this Ticker to notify the {@link TickReceiver} */
	void notify(final TickReceiver receiver);

	/** Stops this Ticker */
	void stop();

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

	public interface TickReceiver {
		void tick();
	}
}