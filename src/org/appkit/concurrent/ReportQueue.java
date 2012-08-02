package org.appkit.concurrent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportQueue<E extends Enum<E>> {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(ReportQueue.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final DelayQueue<Report<E>> queue = new DelayQueue<Report<E>>();

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private ReportQueue() {}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static <T extends Enum<T>> ReportQueue<T> create() {
		return new ReportQueue<T>();
	}

	public void report(final Report<E> r) {
		this.queue.put(r);
	}

	public void report(final E type, Object... data) {
		this.queue.put(new Report<E>(type, ImmutableList.copyOf(data)));
	}

	public Report<E> take() throws InterruptedException {
		return this.queue.take();
	}

	public Report<E> poll() {
		return this.queue.poll();
	}

	public static ReportQueue<?> funnel(final Executor executor, final ReportQueue<?> reports,
										final ReportQueue<?>... moreReports) {

		List<ReportQueue<?>> sources = Lists.newArrayList();
		sources.add(reports);
		sources.addAll(Arrays.asList(moreReports));

		ReportQueue<?> funnel = create();
		for (final ReportQueue<?> source : sources) {

			FunnelingProcess process = new FunnelingProcess();
			process.source     = source;
			process.funnel     = funnel;
			executor.execute(process);
		}

		return funnel;
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private static final class FunnelingProcess implements Runnable {

		@SuppressWarnings("rawtypes")
		private ReportQueue source;
		@SuppressWarnings("rawtypes")
		private ReportQueue funnel;

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			L.debug("running FunnelingProcess");
			try {

				Report<?> r = source.take();
				funnel.report(r);
			} catch (final InterruptedException e) {
				L.error(e.getMessage(), e);
			}
		}
	}
}