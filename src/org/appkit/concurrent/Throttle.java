package org.appkit.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * A throttle to limit expensive calls triggered by often-recurring events.
 *
 * @see SmartExecutor
 */
public interface Throttle {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * Schedules a Runnable to be executed a fixed period of time after it was scheduled.
	 * If a new Runnable is scheduled on this throttle, it will overwrite Runnables which were
	 * scheduled before but not yet run. This way only the last Runnable in a series will be executed.
	 *
	 */
	void throttledExecution(final Runnable runnable);

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

	interface Supplier {
		Throttle createThrottle(final long delay, final TimeUnit timeUnit);
	}
}