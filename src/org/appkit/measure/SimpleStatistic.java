package org.appkit.measure;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a simple Measurement.Listener which keeps track of max,min and avg values of each measurement
 * with a specific name
 *
 */
public class SimpleStatistic implements Measurement.Listener {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(SimpleStatistic.class);
	private static final int COLSIZE						 = 10;

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Multimap<String, MeasureData> data = HashMultimap.create();

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public synchronized void notify(final MeasureData mData) {
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

		sb.append(Strings.padStart("count", maxNameLength + 5, ' '));
		sb.append(Strings.padStart("min", COLSIZE, ' '));
		sb.append(Strings.padStart("avg", COLSIZE, ' '));
		sb.append(Strings.padStart("max", COLSIZE, ' '));

		for (final String name : Ordering.natural().sortedCopy(this.data.keys())) {
			sb.append(Strings.padEnd(name + ":", maxNameLength + 5, ' '));

			Collection<MeasureData> measurements = this.data.get(name);
			Collection<Long> durations			 =
				Collections2.transform(
					measurements,
					new Function<MeasureData, Long>() {
							@Override
							public Long apply(final MeasureData md) {
								return md.getDuration();
							}
						});

			int count							 = 0;
			double avg							 = 0;
			for (final Long d : durations) {
				if (count == 0) {
					avg = d;
				} else {
					avg = ((avg * count) + d) / (count - 1);
				}

				count++;
			}
			sb.append(Strings.padStart(Long.toString(Collections.min(durations)), COLSIZE, ' '));
			sb.append(Strings.padStart(Double.toString(avg), COLSIZE, ' '));
			sb.append(Strings.padStart(Long.toString(Collections.max(durations)), COLSIZE, ' '));
			sb.append("\n");
		}

		return sb.toString();
	}
}