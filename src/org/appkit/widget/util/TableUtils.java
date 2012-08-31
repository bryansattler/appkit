package org.appkit.widget.util;

import com.google.common.base.Objects;

import java.util.Arrays;

import org.appkit.concurrent.Throttle;
import org.appkit.preferences.PrefStore;
import org.appkit.widget.util.impl.ColumnController;
import org.appkit.widget.util.impl.ColumnOrderMemory;
import org.appkit.widget.util.impl.ColumnSizeMemory;
import org.appkit.widget.util.impl.ColumnWeightFixer;
import org.appkit.widget.util.impl.TableScrollDetector;

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

	@SuppressWarnings("unused")
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
	 * fixes weights on a table, also sets setResizable(false) on the columns
	 */
	public static void fixColumnWeights(final Table table, final Integer weights[]) {
		new ColumnWeightFixer(new ColumnController.TableColumnController(table), Arrays.asList(weights));
	}

	/**
	 * installs a ScrollListener on the table
	 */
	public static void installScrollListener(final Table table, final ScrollListener listener) {
		new TableScrollDetector(table, listener);
	}

	/**
	 * returns the last visible row
	 */
	public static int getBottomIndex(final Table table) {

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

		private final int itemCount;
		private final int firstVisibleRow;
		private final int lastVisibleRow;

		public ScrollEvent(final int itemCount, final int firstVisibleRow, final int lastVisibleRow) {
			this.itemCount			 = itemCount;
			this.firstVisibleRow     = firstVisibleRow;
			this.lastVisibleRow		 = lastVisibleRow;
		}

		public int getItemCount() {
			return this.itemCount;
		}

		public int getFirstVisibleRow() {
			return firstVisibleRow;
		}

		public int getLastVisibleRow() {
			return lastVisibleRow;
		}

		public boolean isFirstRowVisible() {
			return firstVisibleRow == 0;
		}

		public boolean isLastRowVisible() {
			return ((lastVisibleRow + 1) >= itemCount);
		}

		@Override
		public String toString() {

			Objects.ToStringHelper helper = Objects.toStringHelper(this);
			helper.add("itemcount", this.itemCount);
			helper.add("first-vis", this.firstVisibleRow);
			helper.add("last-vis", this.lastVisibleRow);

			return helper.toString();
		}
	}
}