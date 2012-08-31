package org.appkit.measure;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import java.util.Deque;

/**
 * This class takes care of starting, stopping and keeping track of measurements. Measurements can be nested,
 * the internal state is managed using {@link ThreadLocal} variables.
 *
 * Every method has a boolean switch to turn off measurement, so that it can be kept in the code
 * and turned off for performance reasons.
 *
 */
public final class Measurement {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Deque<Measurement> runningMeasurements = Lists.newLinkedList();
	private static Measurement.Listener listener			    = null;

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final String name;
	private final Object data;
	private long start;
	private Stopwatch watch;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private Measurement(final String name, final Object data) {
		this.name     = name;
		this.data     = data;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	public Object getData() {
		return data;
	}

	public long getStart() {
		Preconditions.checkState(this.watch != null, "Measurement has not been started yet");
		return start;
	}

	public long getDuration() {
		Preconditions.checkState(this.watch != null, "Measurement has not been stopped yet");
		if (this.watch.elapsedMillis() != 0) {
			return this.watch.elapsedMillis();
		} else {
			return 1;
		}
	}

	public void start() {
		Preconditions.checkState(this.watch == null, "Measurement has been started already");
		this.watch     = new Stopwatch();
		this.start     = System.nanoTime();
		this.watch.start();
	}

	public void end() {
		Preconditions.checkState(this.watch.isRunning(), "Measurement has been stopped already");
		this.watch.stop();
	}

	/**
	 * Sets a {@link Measurement.Listener} to be notified of a new measurement. It needs
	 * to be thread-safe, if Measurements occur over more than one thread.
	 *
	 * @see SimpleStatistic
	 */
	public static synchronized void setListener(final Measurement.Listener newListener) {
		listener = newListener;
	}

	/**
	 * starts a measurement with the given name
	 *
	 * @param doIt actually do the measurement
	 * @param name name of the measurement
	 */
	public static synchronized void start(final boolean doIt, final String name) {
		start(doIt, name, null);
	}

	/**
	 * starts a measurement with the given name and attached data
	 *
	 * @param doIt actually do the measurement
	 * @param name name of the measurement
	 */
	public static synchronized void start(final boolean doIt, final String name, final Object data) {
		if (! doIt) {
			return;
		}

		Measurement newM = new Measurement(name, data);
		newM.start();
		runningMeasurements.addFirst(newM);

		if (listener != null) {
			listener.notifyStart(newM);
		}
	}

	/**
	 * stops the currently running measurement = the last that was started
	 *
	 * @return the finished measurement
	 */
	public static synchronized Measurement stop() {
		if (runningMeasurements.isEmpty()) {
			return null;
		}

		Measurement md = runningMeasurements.removeFirst();
		md.end();
		if (listener != null) {
			listener.notifyData(md);
		}

		return md;
	}

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

	public static interface Listener {

		/**
		 * Notifies this Listener that a Measurement has been started. Use this to keep track
		 * of hierarchical Measurements.
		 */
		void notifyStart(final Measurement data);

		/**
		 * Notifies this Listener that a Measurement has been stopped.
		 */
		void notifyData(final Measurement data);
	}
}