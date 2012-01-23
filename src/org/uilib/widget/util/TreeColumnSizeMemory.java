package org.uilib.widget.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.uilib.util.SWTSyncedRunnable;
import org.uilib.util.Throttle;
import org.uilib.util.prefs.PrefStore;

public final class TreeColumnSizeMemory {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(TreeColumnSizeMemory.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final PrefStore prefStore;
	private final Throttle throttler;
	private final Tree tree;
	private final String memoryKey;
	private final int defaultSize;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	protected TreeColumnSizeMemory(final PrefStore prefStore, final Throttle throttler, final Tree tree,
								   final String key, final int defaultSize) {
		this.prefStore										 = prefStore;
		this.throttler										 = throttler;
		this.tree											 = tree;
		this.memoryKey										 = key + ".columnsizes";
		this.defaultSize									 = defaultSize;

		/* install layout into parentComposite of Table */
		TreeColumnLayout layout = new TreeColumnLayout();
		this.tree.getParent().setLayout(layout);

		/* size all the columns */
		String widthString  = this.prefStore.get(memoryKey, "");
		List<String> widths = Lists.newArrayList(Splitter.on(",").split(widthString));
		if (widths.size() == this.tree.getColumnCount()) {
			for (int i = 0; i < this.tree.getColumnCount(); i++) {

				int wData = defaultSize;
				try {
					wData = Integer.valueOf(widths.get(i));
				} catch (final NumberFormatException e) {}

				layout.setColumnData(this.tree.getColumn(i), new ColumnWeightData(wData));
			}
		} else {

			/* default size */
			for (final TreeColumn column : this.tree.getColumns()) {
				layout.setColumnData(column, new ColumnWeightData(this.defaultSize));
			}
		}

		/* add listeners */
		for (final TreeColumn column : this.tree.getColumns()) {
			column.addControlListener(new ColumnResizeListener());
		}
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private class ColumnResizeListener implements ControlListener {
		@Override
		public void controlMoved(final ControlEvent event) {}

		@Override
		public void controlResized(final ControlEvent event) {
			throttler.throttle(
				memoryKey,
				50,
				TimeUnit.MILLISECONDS,
				new SWTSyncedRunnable() {
						@Override
						public void runChecked() {
							if (tree.isDisposed()) {
								return;
							}

							List<Integer> widths = Lists.newArrayList();
							for (final TreeColumn column : tree.getColumns()) {
								widths.add(column.getWidth());
							}
							prefStore.store(memoryKey, Joiner.on(",").join(widths));
						}
					});
		}
	}
}