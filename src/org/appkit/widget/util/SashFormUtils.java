package org.appkit.widget.util;

import org.appkit.preferences.PrefStore;
import org.appkit.util.Throttle;
import org.appkit.widget.util.impl.SashFormWeightMemory;

import org.eclipse.swt.custom.SashForm;

/**
 * Various utilities for working with {@link SashForm}s
 *
 */
public final class SashFormUtils {

	/**
	 * restores SashForm weights and tracks and saves changes
	 *
	 * @param prefStore
	 *            the prefStore used to load and save the weights
	 * @param throttleSupplier
	 *            supplier used to create a {@link Throttle} for the save function
	 * @param memoryKey
	 *            the key to save to
	 * @param defaultWeights
	 *            default-weights if no saved are found
	 */
	public static void rememberWeights(final SashForm sashForm, final String memoryKey, final PrefStore prefStore,
									   final Throttle.Supplier throttleSupplier, final int defaultWeights[]) {
		new SashFormWeightMemory(prefStore, throttleSupplier, sashForm, memoryKey, defaultWeights);
	}
}