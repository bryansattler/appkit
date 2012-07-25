package org.appkit.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link Executor} that provides commonly-used methods.
 * It uses a Scheduler-Thread to schedule and run tasks.
 *
 */
public final class SmartExecutor implements Executor, Throttle.Supplier {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(SmartExecutor.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final boolean executorCreatedInternally;
	private final ExecutorService executorService;
	private final DelayQueue<SmartRunnable> taskQueue = new DelayQueue<SmartRunnable>();
	private final Map<String, SmartRunnable> throttledTasks = Maps.newHashMap();
	private final Set<Runnable> cancelledTasks = Sets.newHashSet();
	private Thread schedulingThread;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private SmartExecutor(final ExecutorService executorService) {
		if (executorService != null) {
			this.executorService  = executorService;
			this.executorCreatedInternally = false;
		} else {
			this.executorService			   = Executors.newCachedThreadPool();
			this.executorCreatedInternally     = true;
		}
		this.executorService.execute(new Scheduler());
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	/** Creates a new instance based on a cached thread-pool. It has to be shutdown after use. */
	public static SmartExecutor start() {
		return new SmartExecutor(null);
	}

	/** Creates a new instance using the given executor-service */
	public static SmartExecutor startUsing(final ExecutorService executorService) {
		return new SmartExecutor(executorService);
	}

	/** Shuts the executor down ({@see ExecutorService#shutdownNow()})
	 *
	 * @throws IllegalStateException if this executor was created based on another ExecutorService
	 *
	 */
	public void shutdownNow() {
		Preconditions.checkState(
			this.executorCreatedInternally,
			"executor-service wasn't created within this instance");
		this.executorService.shutdownNow();
	}

	/** Shuts the executor down ({@see ExecutorService#shutdown()})
	 *
	 * @throws IllegalStateException if this executor was created based on another ExecutorService
	 *
	 */
	public void shutdown() {
		Preconditions.checkState(
			this.executorCreatedInternally,
			"executor-service wasn't created within this instance");
		this.schedulingThread.interrupt();
		this.executorService.shutdown();
	}

	/** Schedules a Runnable to run once */
	@Override
	public void execute(final Runnable runnable) {
		this.executorService.execute(runnable);
	}

	/** Schedules a Runnable to be executed after a fixed period of time */
	public void schedule(final long delay, final TimeUnit timeUnit, final Runnable runnable) {
		this.taskQueue.put(new SmartRunnable(runnable, delay, timeUnit, false, null));
	}

	/** Schedules a Runnable to be executed using a fixed delay between the end of a run and the start of the next */
	public void scheduleAtFixedRate(final long interval, final TimeUnit timeUnit, final Runnable runnable) {
		this.taskQueue.put(new SmartRunnable(runnable, interval, timeUnit, true, null));
	}

	/** Cancels a scheduled repeating runnable */
	public void cancelRepeatingRunnable(final Runnable runnable) {
		this.cancelledTasks.add(runnable);
	}

	/** Creates a new {@link Throttle} with the given delay */
	@Override
	public Throttle createThrottle(final long delay, final TimeUnit timeUnit) {
		return new SimpleThrottle(delay, timeUnit);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private final class SimpleThrottle implements Throttle {

		private final String uuid	    = UUID.randomUUID().toString();
		private final long delay;
		private final TimeUnit timeUnit;

		public SimpleThrottle(final long delay, final TimeUnit timeUnit) {
			this.delay				    = delay;
			this.timeUnit			    = timeUnit;
		}

		@Override
		public void throttledExecution(final Runnable runnable) {

			SmartRunnable thrTask = new SmartRunnable(runnable, this.delay, this.timeUnit, false, uuid);
			throttledTasks.put(thrTask.getThrottleName(), thrTask);
			taskQueue.put(thrTask);
		}
	}

	private final class Scheduler implements Runnable {
		@Override
		public void run() {
			schedulingThread = Thread.currentThread();
			try {
				while (true) {

					/* wait for the next runnable to become available */
					final SmartRunnable task = SmartExecutor.this.taskQueue.take();

					if (task.isRepeating()) {

						/* if runnable wasn't cancelled tell executor to run the action and reschedule it afterwards */
						if (cancelledTasks.contains(task.getRunnable())) {
							cancelledTasks.remove(task.getRunnable());
						} else {
							SmartExecutor.this.executorService.execute(
								new Runnable() {
										@Override
										public void run() {
											task.run();
											task.reset();
											SmartExecutor.this.taskQueue.put(task);
										}
									});
						}
					} else if (task.isThrottled()) {

						/* run only if this is the latest task in given throttle, otherwise skip execution */
						if (SmartExecutor.this.throttledTasks.get(task.getThrottleName()) == task) {
							SmartExecutor.this.executorService.execute(task);
						}
					} else {
						/* tell the executor to just run the action */
						SmartExecutor.this.executorService.execute(task);

					}
				}
			} catch (final InterruptedException e) {
				SmartExecutor.L.debug("scheduler interrupted (shutting down)");
			}
		}
	}
}