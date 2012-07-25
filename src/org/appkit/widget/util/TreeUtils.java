package org.appkit.widget.util;

import java.util.Arrays;

import org.appkit.concurrent.Throttle;
import org.appkit.preferences.PrefStore;
import org.appkit.widget.util.impl.ColumnController;
import org.appkit.widget.util.impl.ColumnOrderMemory;
import org.appkit.widget.util.impl.ColumnSizeMemory;
import org.appkit.widget.util.impl.ColumnWeightFixer;

import org.eclipse.swt.widgets.Tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utilities for working with {@link Tree}s
 *
 */
public final class TreeUtils {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
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
	 * fixes weights on a table, also sets setResizable(false) on the columns
	 */
	public static void fixColumnWeights(final Tree tree, final Integer weights[]) {
		new ColumnWeightFixer(new ColumnController.TreeColumnController(tree), Arrays.asList(weights));
	}
}