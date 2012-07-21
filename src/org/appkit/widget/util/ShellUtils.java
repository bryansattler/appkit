package org.appkit.widget.util;

import org.appkit.preferences.PrefStore;
import org.appkit.util.Throttle;
import org.appkit.widget.util.impl.ShellAttacher;
import org.appkit.widget.util.impl.ShellMemory;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utilities for working with {@link Shell}s
 *
 */
public final class ShellUtils {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L = LoggerFactory.getLogger(ShellUtils.class);

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * restores the size and position of a shell, tracks and saves changes
	 *
	 * @param prefStore the prefStore used to load and save size and position
	 * @param throttleSupplier used to create a {@link Throttle} for the save function
	 * @param memoryKey prefStore key to save to
	 */
	public static void rememberSizeAndPosition(final Shell shell, final String memoryKey, final PrefStore prefStore,
											   final Throttle.Supplier throttleSupplier) {
		new ShellMemory(prefStore, throttleSupplier, shell, memoryKey, false);
	}

	/**
	 * restores the size of a shell, tracks and saves changes.
	 *
	 * @param prefStore the prefStore used to load and save size and position
	 * @param throttleSupplier used to create a {@link Throttle} for the save function
	 * @param memoryKey prefStore key to save to
	 */
	public static void rememberSize(final Shell shell, final String memoryKey, final PrefStore prefStore,
									final Throttle.Supplier throttleSupplier) {
		new ShellMemory(prefStore, throttleSupplier, shell, memoryKey, true);
	}

	/** alpha / testing */
	public static void smartAttachment(final Shell shell, final Control control) {
		new ShellAttacher(shell, control);
	}
}