package org.appkit.widget.util.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.appkit.concurrent.SWTSyncedRunnable;
import org.appkit.concurrent.Throttle;
import org.appkit.preferences.PrefStore;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShellMemory {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L		   = LoggerFactory.getLogger(ShellMemory.class);
	private static final int THROTTLE_TIME = 100;

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final PrefStore prefStore;
	private final Throttle throttle;
	private final Shell shell;
	private final String memoryKey;
	private final boolean sizeOnly;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public ShellMemory(final PrefStore prefStore, final Throttle.Supplier throttleSupplier, final Shell shell,
					   final String memoryKey, final boolean sizeOnly) {
		this.prefStore     = prefStore;
		this.throttle	   = throttleSupplier.createThrottle(THROTTLE_TIME, TimeUnit.MILLISECONDS);
		this.shell		   = shell;
		this.memoryKey     = memoryKey;
		this.sizeOnly	   = sizeOnly;

		if (! sizeOnly) {

			/* position shell */
			String posString	  = this.prefStore.get(memoryKey + ".position", "");
			List<String> position = Lists.newArrayList(Splitter.on(",").split(posString));
			if (position.size() == 2) {
				try {

					int x = Integer.valueOf(position.get(0));
					int y = Integer.valueOf(position.get(1));
					this.shell.setLocation(x, y);

				} catch (final NumberFormatException e) {}
			}
		}

		/* size shell */
		String sizeString  = this.prefStore.get(memoryKey + ".size", "");
		List<String> sizes = Lists.newArrayList(Splitter.on(",").split(sizeString));
		if (sizes.size() == 2) {
			try {

				int width  = Integer.valueOf(sizes.get(0));
				int height = Integer.valueOf(sizes.get(1));
				this.shell.setSize(width, height);

			} catch (final NumberFormatException e) {}
		}

		/* set maximize shell */
		if (this.prefStore.exists(memoryKey + ".maximized")) {

			boolean maximized = this.prefStore.get(memoryKey + ".maximized", false);
			this.shell.setMaximized(maximized);
		}

		/* add listener */
		this.shell.addControlListener(new ShellChanged());
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private class ShellChanged implements ControlListener {
		@Override
		public void controlMoved(final ControlEvent event) {
			this.controlResized(event);
		}

		@Override
		public void controlResized(final ControlEvent event) {

			final String maximizedString = String.valueOf(shell.getMaximized());
			final String positionString;
			final String sizeString;

			/* if shell was maximized don't store position and size */
			if (shell.getMaximized()) {
				positionString			 = null;
				sizeString				 = null;
			} else {

				/* if size only, don't store position */
				if (sizeOnly) {
					positionString = null;
				} else {

					Point pos = shell.getLocation();
					positionString = Joiner.on(",").join(pos.x, pos.y);
				}

				Point size = shell.getSize();
				sizeString = Joiner.on(",").join(size.x, size.y);
			}

			Runnable runnable =
				new Runnable() {
					@Override
					public void run() {
						L.debug("writing out maximized {} to key {}", maximizedString, memoryKey);
						prefStore.store(memoryKey + ".maximized", maximizedString);

						if (positionString != null) {
							L.debug("writing out position {} to key {}", positionString, memoryKey);
							prefStore.store(memoryKey + ".position", positionString);
						}

						if (sizeString != null) {
							L.debug("writing out size {} to key {}", sizeString, memoryKey);
							prefStore.store(memoryKey + ".size", sizeString);
						}
					}
				};

			throttle.throttledExecution(new SWTSyncedRunnable(Display.getCurrent(), runnable));

		}
	}
}