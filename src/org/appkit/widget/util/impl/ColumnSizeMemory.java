package org.appkit.widget.util.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.appkit.preferences.PrefStore;
import org.appkit.util.LoggingRunnable;
import org.appkit.util.SWTSyncedRunnable;
import org.appkit.util.Throttle;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ColumnSizeMemory {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L		   = LoggerFactory.getLogger(ColumnSizeMemory.class);
	private static final int THROTTLE_TIME = 100;

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final PrefStore prefStore;
	private final Throttle throttle;
	private final ColumnController colController;
	private final String memoryKey;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public ColumnSizeMemory(final ColumnController colController, final PrefStore prefStore,
							   final Throttle.Supplier throttleSupplier, final String key) {
		this.prefStore		   = prefStore;
		this.throttle		   = throttleSupplier.createThrottle(THROTTLE_TIME, TimeUnit.MILLISECONDS);
		this.colController     = colController;
		this.memoryKey		   = key + ".columnsizes";

		/* add initial listener for initial size-setting */
		colController.getControl().addControlListener(
			new ControlListener() {
					@Override
					public void controlResized(final ControlEvent event) {
						loadSizes();
						colController.getControl().removeControlListener(this);
					}

					@Override
					public void controlMoved(final ControlEvent event) {}
				});

		/* add listeners */
		for (int i = 0; i < colController.getColumnCount(); i++) {
			colController.installColumnControlListener(i, new ColumnResizeListener());
		}
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	private void loadSizes() {

		/* load the stored info */
		String sizeString = this.prefStore.get(this.memoryKey, "");
		L.debug("loading sizes: '{}'", sizeString);

		List<String> sizeStrings = Lists.newArrayList(Splitter.on(",").split(sizeString));
		if (sizeStrings.size() != colController.getColumnCount()) {
			return;
		}

		List<Integer> sizes = Lists.newArrayList();
		for (int i = 0; i < sizeStrings.size(); i++) {
			try {
				sizes.add(Integer.valueOf(sizeStrings.get(i)));
			} catch (final NumberFormatException e) {
				L.debug("no size: '{}'", sizeStrings.get(i));
				return;
			}
		}

		for (int i = 0; i < sizes.size(); i++) {
			L.debug("column {}: setting width to {}", i, sizes.get(i));
			this.colController.setWidth(i, sizes.get(i));
		}
	}

	private void saveWeights() {

		List<Integer> sizes = Lists.newArrayList();
		for (int i = 0; i < this.colController.getColumnCount(); i++) {
			sizes.add(this.colController.getWidth(i));
		}

		/* store */
		final String widthString = Joiner.on(",").join(sizes);

		Runnable runnable =
			new LoggingRunnable() {
				@Override
				public void runChecked() {
					L.debug("writing out weights {} to key {}", widthString, memoryKey);
					prefStore.store(memoryKey, widthString);
				}
			};

		this.throttle.throttledExecution(new SWTSyncedRunnable(Display.getCurrent(), runnable));
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private class ColumnResizeListener implements ControlListener {

		private int lastAvailWidth = -1;

		@Override
		public void controlMoved(final ControlEvent event) {}

		@Override
		public void controlResized(final ControlEvent event) {
			if (lastAvailWidth == -1) {
				lastAvailWidth = colController.getAvailWidth();
				return;
			}

			if (colController.getAvailWidth() == lastAvailWidth) {
				L.debug(
					"width {} didn't change -> columns resized -> saving new weights",
					colController.getAvailWidth());
				saveWeights();
			} else {
				lastAvailWidth = colController.getAvailWidth();
			}
		}
	}
}