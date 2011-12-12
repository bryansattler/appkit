package org.uilib.swt.memory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import org.uilib.swt.SWTSyncedRunnable;
import org.uilib.util.PrefStore;
import org.uilib.util.SmartExecutor;

public final class TreeColumnOrderMemory {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = Logger.getLogger(TreeColumnOrderMemory.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final PrefStore prefStore;
	private final Tree tree;
	private final String memoryKey;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private TreeColumnOrderMemory(final PrefStore prefStore, final Tree tree, final String memoryKey) {
		this.prefStore										 = prefStore;
		this.tree											 = tree;
		this.memoryKey										 = memoryKey + ".columnorder";

		/* reorder columns */
		String orderString     = this.prefStore.get(memoryKey, "");
		List<String> orderList = Lists.newArrayList(Splitter.on(",").split(orderString));
		if (orderList.size() == this.tree.getColumnCount()) {
			try {

				int order[] = new int[this.tree.getColumnCount()];
				int i	    = 0;
				for (final String pos : orderList) {
					order[i] = Integer.valueOf(pos);
					i++;
				}

				this.tree.setColumnOrder(order);
			} catch (final NumberFormatException e) {}
		}

		for (final TreeColumn column : this.tree.getColumns()) {
			/* set column movable */
			column.setMoveable(true);

			/* add listener */
			column.addControlListener(new ColumnMoveListener());
		}
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static void install(final PrefStore prefStore, final Tree tree, final String memoryKey) {
		new TreeColumnOrderMemory(prefStore, tree, memoryKey);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private class ColumnMoveListener implements ControlListener {
		@Override
		public void controlMoved(final ControlEvent event) {
			SmartExecutor.instance().throttle(
				memoryKey,
				50,
				TimeUnit.MILLISECONDS,
				new SWTSyncedRunnable() {
						@Override
						protected void runChecked() {
							if (tree.isDisposed()) {
								return;
							}

							List<Integer> order = Ints.asList(tree.getColumnOrder());
							prefStore.store(memoryKey, Joiner.on(",").join(order));
						}
					});
		}

		@Override
		public void controlResized(final ControlEvent event) {}
	}
}