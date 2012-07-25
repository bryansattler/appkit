package org.appkit.concurrent;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

/** delayed runnable */
class DelayedRunnable implements Delayed, Runnable {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Runnable runnable;
	private final long delay;
	private final TimeUnit delayUnit;
	private final boolean repeat;
	private long endOfDelay;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public DelayedRunnable(final Runnable runnable, final long delay, final TimeUnit delayUnit, boolean repeat) {
		this.runnable	    = runnable;
		this.delay = delay;
		this.delayUnit = delayUnit;
		this.repeat = repeat;
		this.reset();
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public boolean isRepeating() {
		return this.repeat;
	}

	public void reset() {
		Preconditions.checkState(this.repeat);
		this.endOfDelay     = delayUnit.toMillis(delay) + System.currentTimeMillis();
	}

	@Override
	public int compareTo(final Delayed other) {

		final Long delay1 = this.getDelay(TimeUnit.MILLISECONDS);
		final Long delay2 = other.getDelay(TimeUnit.MILLISECONDS);

		return delay1.compareTo(delay2);
	}

	@Override
	public long getDelay(final TimeUnit unit) {
		return unit.convert(this.endOfDelay - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public Runnable getRunnable() {
		return this.runnable;
	}

	@Override
	public void run() {
		this.runnable.run();
	}
}