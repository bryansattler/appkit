package org.appkit.util;

import java.util.concurrent.TimeUnit;

/** repeating runnable */
final class RepeatingRunnable extends DelayedRunnable {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final long period;
	private final TimeUnit delayUnit;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public RepeatingRunnable(final Runnable runnable, final long period, final TimeUnit delayUnit) {
		super(runnable, period, delayUnit);

		this.period		   = period;
		this.delayUnit     = delayUnit;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public RepeatingRunnable reschedule() {
		return new RepeatingRunnable(this.runnable, this.period, this.delayUnit);
	}
}