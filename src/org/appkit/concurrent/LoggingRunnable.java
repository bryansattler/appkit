package org.appkit.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Runnable that logs all RuntimeExceptions. */
public abstract class LoggingRunnable implements Runnable {

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public final void run() {
		try {
			this.runChecked();
		} catch (final RuntimeException e) {

			Logger L = LoggerFactory.getLogger(this.getClass());
			L.error(e.getMessage(), e);
		}
	}

	/** Overwrite this instead of the standard <code>run()</code> method. */
	public abstract void runChecked();
}