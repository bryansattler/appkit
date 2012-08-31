package org.appkit.concurrent;

import com.google.common.base.Preconditions;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

final class SmartRunnable implements Delayed, Runnable {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Runnable runnable;
	private final long delay;
	private final TimeUnit delayUnit;
	private final boolean isRepeating;
	private final String throttleName;

	/* can be reset before re-schedule */
	private long endOfDelay;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public SmartRunnable(final Runnable runnable, final long delay, final TimeUnit delayUnit, final boolean repeat,
						 final String throttleName) {
		Preconditions.checkArgument(! repeat || (throttleName == null), "either specify repeat or a throttle name");
		this.runnable		  = runnable;
		this.delay			  = delay;
		this.delayUnit		  = delayUnit;
		this.isRepeating	  = repeat;
		this.throttleName     = throttleName;

		this.endOfDelay = delayUnit.toMillis(delay) + System.currentTimeMillis();
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public boolean isRepeating() {
		return this.isRepeating;
	}

	public boolean isThrottled() {
		return this.throttleName != null;
	}

	public String getThrottleName() {
		return this.throttleName;
	}

	public void reset() {
		Preconditions.checkState(this.isRepeating);
		this.endOfDelay = delayUnit.toMillis(delay) + System.currentTimeMillis();
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