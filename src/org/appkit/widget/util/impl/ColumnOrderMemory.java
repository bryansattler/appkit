package org.appkit.widget.util.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.appkit.concurrent.SWTSyncedRunnable;
import org.appkit.concurrent.Throttle;
import org.appkit.preferences.PrefStore;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ColumnOrderMemory {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L		   = LoggerFactory.getLogger(ColumnOrderMemory.class);
	private static final int THROTTLE_TIME = 100;

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final PrefStore prefStore;
	private final Throttle throttle;
	private final ColumnController colController;
	private final String memoryKey;

	/* save order */
	private List<Integer> lastOrder;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public ColumnOrderMemory(final ColumnController colController, final PrefStore prefStore,
							 final Throttle.Supplier throttleSupplier, final String key) {
		this.prefStore		   = prefStore;
		this.throttle		   = throttleSupplier.createThrottle(THROTTLE_TIME, TimeUnit.MILLISECONDS);
		this.colController     = colController;
		this.memoryKey		   = key + ".columnorder";

		/* create saver */

		/* reorder columns */
		String orderString = this.prefStore.get(this.memoryKey, "");
		L.debug("loading order: '{}'", orderString);

		List<String> orderList = Lists.newArrayList(Splitter.on(",").split(orderString));
		if (orderList.size() == this.colController.getColumnCount()) {
			L.debug("valid order {} -> reordering columns", orderList);
			try {

				int order[] = new int[this.colController.getColumnCount()];
				int i	    = 0;
				for (final String pos : orderList) {
					order[i] = Integer.valueOf(pos);
					i++;
				}

				this.colController.setColumnOrder(order);

			} catch (final NumberFormatException e) {}
		}

		/* save last order */
		this.lastOrder = Ints.asList(colController.getColumnOrder());

		/* set columns movable */
		this.colController.setColumnsMoveable(true);

		/* add listeners */
		for (int i = 0; i < this.colController.getColumnCount(); i++) {
			this.colController.installColumnControlListener(i, new ColumnMoveListener());
		}
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	private void saveOrder() {

		final List<Integer> order = Ints.asList(colController.getColumnOrder());
		if (order.equals(lastOrder)) {
			return;
		}

		this.lastOrder = order;

		final String orderString = Joiner.on(",").join(order);

		Runnable runnable =
			new Runnable() {
				@Override
				public void run() {
					L.debug("writing out order {} to key {}", orderString, memoryKey);
					prefStore.store(memoryKey, orderString);
				}
			};

		this.throttle.throttledExecution(new SWTSyncedRunnable(Display.getCurrent(), runnable));
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private class ColumnMoveListener implements ControlListener {
		@Override
		public void controlMoved(final ControlEvent event) {
			saveOrder();
		}

		@Override
		public void controlResized(final ControlEvent event) {}
	}
}