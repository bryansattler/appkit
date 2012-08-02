package org.appkit.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;

import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingThreadFactory implements ThreadFactory {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(LoggingThreadFactory.class);

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private LoggingThreadFactory() {}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static LoggingThreadFactory create() {
		return new LoggingThreadFactory();
	}

	@Override
	public Thread newThread(final Runnable r) {

		Thread thread = new Thread(r);
		thread.setUncaughtExceptionHandler(
			new UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(final Thread t, final Throwable e) {
						L.error(t.getName() + " " + e.getMessage(), e);
					}
				});

		return thread;
	}
}