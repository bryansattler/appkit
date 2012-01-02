package org.uilib.util;

import org.eclipse.swt.widgets.Display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SWTSyncedRunnable implements Runnable {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public final void run() {
		if (Display.getDefault().getThread() == Thread.currentThread()) {
			try {
				this.runChecked();
			} catch (final RuntimeException e) {
				this.logger.error(e.getMessage(), e);
			}
		} else {
			Display.getDefault().syncExec(this);
		}
	}

	public abstract void runChecked();
}