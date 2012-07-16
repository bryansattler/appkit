package org.appkit.widget.util;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.appkit.preferences.PrefStore;
import org.appkit.util.Throttle;
import org.appkit.widget.util.impl.ColumnController;
import org.appkit.widget.util.impl.ColumnOrderMemory;
import org.appkit.widget.util.impl.ColumnSizeMemory;
import org.appkit.widget.util.impl.TableScrollDetector;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utilities for working with {@link Table}s
 *
 */
public final class TableUtils {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(TableUtils.class);

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * restores column-sizes, tracks and saves changes.
	 *
	 * @param prefStore the prefStore used to load and save sizes
	 * @param throttleSupplier used to create a {@link Throttle} for the save function
	 * @param memoryKey prefStore key to use
	 */
	public static void rememberColumnSizes(final Table table, final String memoryKey, final PrefStore prefStore,
										   final Throttle.Supplier throttleSupplier) {
		new ColumnSizeMemory(new ColumnController.TableColumnController(table), prefStore, throttleSupplier, memoryKey);
	}

	/**
	 * restores column-order, tracks and saves changes.
	 *
	 * @param prefStore the prefStore used to load and save order
	 * @param throttleSupplier used to create a {@link Throttle} for the save function
	 * @param memoryKey prefStore key to use
	 */
	public static void rememberColumnOrder(final Table table, final String memoryKey, final PrefStore prefStore,
										   final Throttle.Supplier throttleSupplier) {
		new ColumnOrderMemory(
			new ColumnController.TableColumnController(table),
			prefStore,
			throttleSupplier,
			memoryKey);
	}

	/**
	 * adds a controlListener that resizes all columns equally to fill the entire width of the table
	 *
	 */
	public static void fillTableWidth(final Table table) {

		final ControlListener controlListener =
			new ControlListener() {
				@Override
				public void controlResized(final ControlEvent event) {
					Preconditions.checkState(table.getColumnCount() != 0, "no columns in table!");

					int width = table.getClientArea().width;
					width = width - (table.getBorderWidth() * 2);

					int colWidth = width / table.getColumnCount();

					L.debug("fillTableWidth: set column width to {}", colWidth);
					for (int i = 0; i < table.getColumnCount(); i++) {
						table.getColumn(i).setWidth(colWidth);
					}

					table.removeControlListener(this);
					L.debug("fillTableWidth: done and listener removed");
				}

				@Override
				public void controlMoved(final ControlEvent event) {}
			};

		table.addControlListener(controlListener);
	}

	/**
	 * installs a ScrollListener on the table
	 */
	public static void installScrollListener(final Table table, final ScrollListener listener) {
		new TableScrollDetector(table, listener);
	}

	/**
	 * returns the last visible row
	 *
	 * @return -1 if no data in the table
	 */
	public static int getBottomIndex(final Table table) {
		if (table.getItemCount() == 0) {
			return -1;
		}

		Rectangle rect   = table.getClientArea();
		int itemHeight   = table.getItemHeight();
		int headerHeight = table.getHeaderHeight();

		int visibleCount = (((rect.height - headerHeight) + itemHeight) - 1) / itemHeight;

		return (table.getTopIndex() + visibleCount) - 1;
	}

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

		public interface ScrollListener {
			public void scrolled(final ScrollEvent event);
		}

		//~ Inner Classes --------------------------------------------------------------------------------------------------

		public static final class ScrollEvent {

			private final int totalRows;
			private final int firstVisibleRow;
			private final int lastVisibleRow;

			public ScrollEvent(final int totalRows, final int firstVisibleRow, final int lastVisibleRow) {
				this.totalRows			 = totalRows;
				this.firstVisibleRow     = firstVisibleRow;

				/* we want visible data-rows, not the blank one at the end */
				if (lastVisibleRow >= totalRows) {
					this.lastVisibleRow = totalRows - 1;
				} else {
					this.lastVisibleRow = lastVisibleRow;
				}
			}

			public int getFirstVisibleRow() {
				return firstVisibleRow;
			}

			public int getLastVisibleRow() {
				return lastVisibleRow;
			}

			public boolean isFirstRowVisible() {
				return (firstVisibleRow <= 0);
			}

			public boolean isLastRowVisible() {
				return ((lastVisibleRow + 1) == totalRows);
			}

			@Override
			public String toString() {

				Objects.ToStringHelper helper = Objects.toStringHelper(this);
				helper.add("total", this.totalRows);
				helper.add("first-vis", this.firstVisibleRow);
				helper.add("last-vis", this.lastVisibleRow);

				return helper.toString();
			}
		}
}