package org.appkit.measure;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CallTimeLogger implements Measurement.Listener {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(CallTimeLogger.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private int nestingLevel							  = 0;
	private final Multimap<Integer, Measurement> finished = HashMultimap.create();

	//~ Methods --------------------------------------------------------------------------------------------------------

	public abstract void output(final String str);

	@Override
	public final void notifyStart(final Measurement m) {
		this.nestingLevel++;
		this.output("'" + m.getName() + "' started");
	}

	@Override
	public final void notifyData(final Measurement data) {

		String str = "'" + data.getName() + "' finished: " + data.getDuration() + " ms";

		if (this.finished.containsKey(nestingLevel)) {
			str = str + " (";

			/* get submeasurements */
			Collection<Measurement> subs				 = this.finished.removeAll(this.nestingLevel);
			final Multimap<String, Measurement> combined =
				Multimaps.index(
					subs,
					new Function<Measurement, String>() {
							@Override
							public String apply(final Measurement md) {
								return md.getName();
							}
						});

			Iterable<String> namesByDuration =
				Ordering.natural().onResultOf(
					new Function<String, Long>() {
							@Override
							public Long apply(final String name) {

								long dur     = 0;
								for (final Measurement md : combined.get(name)) {
									dur = dur + md.getDuration();
								}
								return dur;
							}
						}).sortedCopy(combined.keySet());

			StringBuilder sb = new StringBuilder();
			for (final String name : namesByDuration) {

				long dur = 0;
				for (final Measurement md : combined.get(name)) {
					dur = dur + md.getDuration();
				}

				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append("'" + name + "': ");
				sb.append(Long.toString((dur * 100) / data.getDuration()));
				sb.append("%");
			}
			str     = str + sb.toString();
			str     = str + ")";
		}

		this.output(str);

		this.nestingLevel--;
		if (this.nestingLevel > 0) {
			this.finished.put(this.nestingLevel, data);
		}
	}
}