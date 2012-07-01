package org.appkit.widget.util;

import org.appkit.preferences.PrefStore;
import org.appkit.util.Throttle;

import org.eclipse.swt.widgets.Shell;

/**
 * Various functions for working with {@link Shell}s
 *
 */
public final class ShellUtils {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * restores the size and position of a shell, tracks and saves changes
	 *
	 * @param prefStore the prefStore used to load and save size and position
	 * @param throttleSupplier used to create a {@link Throttle} for the save function
	 * @param memoryKey prefStore key to save to
	 */
	public static void rememberSizeAndPosition(final PrefStore prefStore, final Throttle.Supplier throttleSupplier,
											   final Shell shell, final String memoryKey, final int defaultWidth,
											   final int defaultHeight, final int defaultX, final int defaultY) {
		new ShellMemory(
			prefStore,
			throttleSupplier,
			shell,
			memoryKey,
			defaultWidth,
			defaultHeight,
			defaultX,
			defaultY,
			false,
			false);
	}

	/**
	 * restores the size of a shell, tracks and saves changes.
	 *
	 * @param prefStore the prefStore used to load and save size and position
	 * @param throttleSupplier used to create a {@link Throttle} for the save function
	 * @param memoryKey prefStore key to save to
	 */
	public static void rememberSize(final PrefStore prefStore, final Throttle.Supplier throttleSupplier,
									final Shell shell, final String memoryKey, final int defaultWidth,
									final int defaultHeight) {
		new ShellMemory(prefStore, throttleSupplier, shell, memoryKey, defaultWidth, defaultHeight, 0, 0, false, true);
	}
}