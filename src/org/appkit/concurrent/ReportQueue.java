package org.appkit.concurrent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportQueue {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(ReportQueue.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final BlockingQueue<Report> queue = Queues.newLinkedBlockingQueue();

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private ReportQueue() {}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static ReportQueue create() {
		return new ReportQueue();
	}

	public void report(final Report r) {
		this.queue.add(r);
	}

	public void report(final Enum<?> type, Object... data) {
		this.queue.add(new Report(type, ImmutableList.copyOf(data)));
	}

	public Report take() throws InterruptedException {
		return this.queue.take();
	}

	public Report poll() {
		return this.queue.poll();
	}

	public static ReportQueue funnel(final Executor executor, final ReportQueue reports,
									 final ReportQueue... moreReports) {

		List<ReportQueue> sources = Lists.newArrayList();
		sources.add(reports);
		sources.addAll(Arrays.asList(moreReports));

		ReportQueue funnel = create();
		L.debug("creating FunnelProcess for {} sources", sources.size());
		for (final ReportQueue source : sources) {
			L.debug("creating FunnelProcess for source");

			FunnelingProcess process = new FunnelingProcess();
			process.source     = source;
			process.funnel     = funnel;
			executor.execute(process);
		}

		return funnel;
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private static final class FunnelingProcess implements Runnable {

		private ReportQueue source;
		private ReportQueue funnel;

		@Override
		public void run() {
			L.debug("[FunnelingProcess] begin");
			try {
				while (true) {

					Report r = source.take();
					funnel.report(r);
				}
			} catch (final InterruptedException e) {
				L.error(e.getMessage(), e);
			}
			L.debug("[FunnelingProcess] exit");
		}
	}
}