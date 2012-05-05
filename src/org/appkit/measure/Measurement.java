package org.appkit.measure;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import java.util.Queue;

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

	private static final ThreadLocal<Queue<Measurement>> runningMeasurements =
		new ThreadLocal<Queue<Measurement>>() {
			@Override
			protected Queue<Measurement> initialValue() {
				return Lists.newLinkedList();
			}
		};

	private static Measurement.Listener listener = null;

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final String name;
	private final Stopwatch watch				 = new Stopwatch();
	private final long start;
	private final Object data;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private Measurement(final String name, final Object data) {
		this.start								 = System.currentTimeMillis();
		this.name								 = name;
		this.watch.start();
		this.data = data;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * Sets a {@link Measurement.Listener} to be notified of a new measurement. It needs
	 * to be thread-safe, if Measurements occur over more than one thread.
	 *
	 * @see SimpleStatistic
	 */
	public static void setListener(final Measurement.Listener newListener) {
		listener = newListener;
	}

	/**
	 * starts a measurement with the given name
	 *
	 * @param doIt actually do the measurement
	 * @param name name of the measurement
	 */
	public static void start(final boolean doIt, final String name) {
		start(doIt, name, null);
	}

	/**
	 * starts a measurement with the given name and attached data
	 *
	 * @param doIt actually do the measurement
	 * @param name name of the measurement
	 */
	public static void start(final boolean doIt, final String name, final Object data) {
		if (! doIt) {
			return;
		}

		if (listener != null) {
			listener.notifyStart();
		}

		runningMeasurements.get().add(new Measurement(name, data));
	}

	/**
	 * stops the currently running measurement = the last that was started in this thread
	 *
	 * @return the finished measurement
	 */
	public static MeasureData stop() {

		Queue<Measurement> rM = runningMeasurements.get();
		Preconditions.checkState(!rM.isEmpty(), "no measurment running!");

		MeasureData md = rM.poll().stopMeasurement();
		if (listener != null) {
			listener.notifyData(md);
		}

		return md;
	}

	private MeasureData stopMeasurement() {
		this.watch.stop();

		return new MeasureData(this.name, this.data, this.start, this.watch.elapsedMillis());
	}

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

	public static interface Listener {

		/**
		 * Notifies this Listener that a Measurement has been started. Use this to keep track
		 * of hierarchical Measurements.
		 */
		void notifyStart();

		/**
		 * Notifies this Listener of new MeasureData. If Measurements occur over multiple threads, this
		 * needs to be made thread-safe.
		 */
		void notifyData(final MeasureData data);
	}
}