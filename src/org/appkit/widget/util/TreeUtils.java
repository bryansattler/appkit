package org.appkit.widget.util;

import com.google.common.base.Preconditions;

import org.appkit.preferences.PrefStore;
import org.appkit.util.Throttle;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various functions for working with {@link Tree}s
 *
 */
public final class TreeUtils {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(TreeUtils.class);

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * restores column-sizes, tracks and saves changes.
	 *
	 * @param prefStore the prefStore used to load and save sizes
	 * @param throttleSupplier used to create a {@link Throttle} for the save function
	 * @param memoryKey prefStore key to use
	 */
	public static void rememberColumnSizes(final Tree tree, final String memoryKey, final PrefStore prefStore,
										   final Throttle.Supplier throttleSupplier) {
		new ColumnSizeMemory(new ColumnController.TreeColumnController(tree), prefStore, throttleSupplier, memoryKey);
	}

	/**
	 * restores column-order, tracks and saves changes.
	 *
	 * @param prefStore the prefStore used to load and save order
	 * @param throttleSupplier used to create a {@link Throttle} for the save function
	 * @param memoryKey prefStore key to use
	 */
	public static void rememberColumnOrder(final Tree tree, final String memoryKey, final PrefStore prefStore,
										   final Throttle.Supplier throttleSupplier) {
		new ColumnOrderMemory(new ColumnController.TreeColumnController(tree), prefStore, throttleSupplier, memoryKey);
	}

	/**
	 * resizes all columns equally to fill the entire width of the tree
	 *
	 */
	public static void fillTreeWidth(final Tree tree) {

		final ControlListener controlListener =
			new ControlListener() {
				@Override
				public void controlResized(final ControlEvent event) {
					Preconditions.checkState(tree.getColumnCount() != 0, "no columns in tree!");

					int width = tree.getClientArea().width;
					width = width - (tree.getBorderWidth() * 2);

					int colWidth = width / tree.getColumnCount();

					L.debug("fillTreeWidth: set column width to {}", colWidth);
					for (int i = 0; i < tree.getColumnCount(); i++) {
						tree.getColumn(i).setWidth(colWidth);
					}

					tree.removeControlListener(this);
					L.debug("fillTreeWidth: done and listener removed");
				}

				@Override
				public void controlMoved(final ControlEvent event) {}
			};

		tree.addControlListener(controlListener);
	}
}