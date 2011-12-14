package org.uilib.swt.memory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.uilib.swt.SWTSyncedRunnable;
import org.uilib.util.PrefStore;
import org.uilib.util.Throttler;

public final class TableColumnSizeMemory {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = Logger.getLogger(TableColumnSizeMemory.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final PrefStore prefStore;
	private final Throttler throttler;
	private final Table table;
	private final String memoryKey;
	private final int defaultSize;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private TableColumnSizeMemory(final PrefStore prefStore, final Throttler throttler, final Table table, final String memoryKey,
								  final int defaultSize) {
		this.prefStore										 = prefStore;
		this.throttler = throttler;
		this.table											 = table;
		this.memoryKey										 = memoryKey + ".columnsizes";
		this.defaultSize									 = defaultSize;

		/* install layout into parentComposite of Table */
		TableColumnLayout layout = new TableColumnLayout();
		table.getParent().setLayout(layout);

		/* size all the columns */
		String widthString  = this.prefStore.get(memoryKey, "");
		List<String> widths = Lists.newArrayList(Splitter.on(",").split(widthString));
		if (widths.size() == table.getColumnCount()) {
			for (int i = 0; i < table.getColumnCount(); i++) {

				int wData = defaultSize;
				try {
					wData = Integer.valueOf(widths.get(i));
				} catch (final NumberFormatException e) {}

				layout.setColumnData(table.getColumn(i), new ColumnWeightData(wData));
			}
		} else {

			/* default size */
			for (final TableColumn column : table.getColumns()) {
				layout.setColumnData(column, new ColumnWeightData(this.defaultSize));
			}
		}

		/* add listeners */
		for (final TableColumn column : table.getColumns()) {
			column.addControlListener(new ColumnResizeListener());
		}
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static void install(final PrefStore prefStore, final Throttler throttler, final Table table, final String memoryKey,
							   final int defaultSize) {
		new TableColumnSizeMemory(prefStore, throttler, table, memoryKey, defaultSize);
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
						protected void runChecked() {
							if (table.isDisposed()) {
								return;
							}

							List<Integer> widths = Lists.newArrayList();
							for (final TableColumn column : table.getColumns()) {
								widths.add(column.getWidth());
							}
							prefStore.store(memoryKey, Joiner.on(",").join(widths));
						}
					});
		}
	}
}