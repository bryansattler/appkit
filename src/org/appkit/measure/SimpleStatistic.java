package org.appkit.measure;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;

import java.text.DecimalFormat;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a {@link Measurement.Listener} which keeps track of max, min and avg duration of a batch of measurements, grouped by their name.
 *
 */
public class SimpleStatistic implements Measurement.Listener {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(SimpleStatistic.class);
	private static final int COLSIZE						 = 12;
	private static final DecimalFormat decFormat			 = new DecimalFormat("0");

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Multimap<String, Measurement> data = HashMultimap.create();

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public synchronized void notifyStart(final Measurement m) {}

	@Override
	public synchronized void notifyData(final Measurement mData) {
		this.data.put(mData.getName(), mData);
	}

	public String getResults() {

		StringBuilder sb  = new StringBuilder();
		int maxNameLength =
			Ordering.natural().max(
				Collections2.transform(
					this.data.keys(),
					new Function<String, Integer>() {
							@Override
							public Integer apply(final String name) {
								return name.length();
							}
						}));

		int col1size = maxNameLength + 3;

		sb.append(Strings.padStart("", col1size, ' '));
		sb.append(Strings.padStart("count", COLSIZE, ' '));
		sb.append(Strings.padStart("min", COLSIZE, ' '));
		sb.append(Strings.padStart("avg", COLSIZE, ' '));
		sb.append(Strings.padStart("max", COLSIZE, ' '));
		sb.append("\n");

		for (final String name : Ordering.natural().sortedCopy(this.data.keySet())) {
			sb.append(Strings.padEnd(name + ":", col1size, ' '));

			Collection<Measurement> measurements = this.data.get(name);
			Collection<Long> durations			 =
				Collections2.transform(
					measurements,
					new Function<Measurement, Long>() {
							@Override
							public Long apply(final Measurement md) {
								return md.getDuration();
							}
						});

			int count  = 0;
			double avg = 0;
			for (final Long d : durations) {
				if (count == 0) {
					avg = d;
				} else {
					avg = ((avg * count) + d) / (count + 1);
				}
				count++;

			}
			sb.append(Strings.padStart(Integer.toString(count), COLSIZE, ' '));
			sb.append(Strings.padStart(decFormat.format(Collections.min(durations)), COLSIZE, ' '));
			sb.append(Strings.padStart(decFormat.format(avg), COLSIZE, ' '));
			sb.append(Strings.padStart(decFormat.format(Collections.max(durations)), COLSIZE, ' '));
			sb.append("\n");
		}

		return sb.toString();
	}
}