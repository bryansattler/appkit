package org.uilib.widget.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.uilib.util.LoggingRunnable;
import org.uilib.util.SWTSyncedRunnable;
import org.uilib.util.Throttle;
import org.uilib.util.prefs.PrefStore;

public final class SashFormWeightMemory {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L		   = LoggerFactory.getLogger(SashFormWeightMemory.class);
	private static final int THROTTLE_TIME = 100;

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final PrefStore prefStore;
	private final Throttle throttler;
	private final SashForm sashForm;
	private final String memoryKey;
	private final int defaultWeights[];

	//~ Constructors ---------------------------------------------------------------------------------------------------

	protected SashFormWeightMemory(final PrefStore prefStore, final Throttle throttler, final SashForm sashForm,
								   final String key, final int defaultWeights[]) {
		this.prefStore		    = prefStore;
		this.throttler		    = throttler;
		this.sashForm		    = sashForm;
		this.memoryKey		    = key + ".sashsizes";
		this.defaultWeights     = defaultWeights;

		/* position shell */
		String weightString     = this.prefStore.get(this.memoryKey, "");
		List<String> weightList = Lists.newArrayList(Splitter.on(",").split(weightString));
		if (weightList.size() == this.sashForm.getChildren().length) {
			try {

				int weights[] = new int[this.sashForm.getChildren().length];
				int i		  = 0;
				for (final String weight : weightList) {
					weights[i] = Integer.valueOf(weight);
					i++;
				}

				this.sashForm.setWeights(weights);
			} catch (final NumberFormatException e) {
				this.sashForm.setWeights(this.defaultWeights);
			}
		} else {
			this.sashForm.setWeights(this.defaultWeights);
		}

		/* add listener */
		this.sashForm.getChildren()[0].addControlListener(new SashMovedListener());
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private class SashMovedListener implements ControlListener {
		@Override
		public void controlMoved(final ControlEvent event) {}

		@Override
		public void controlResized(final ControlEvent event) {

			int weights[]			  = sashForm.getWeights();
			final String weightString = Joiner.on(",").join(weights[0], weights[1]);

			Runnable runnable		  =
				new LoggingRunnable() {
					@Override
					public void runChecked() {
						L.debug("writing out weights {} to key {}", weightString, memoryKey);
						prefStore.store(memoryKey, weightString);
					}
				};

			throttler.throttle(
				memoryKey,
				THROTTLE_TIME,
				TimeUnit.MILLISECONDS,
				new SWTSyncedRunnable(Display.getCurrent(), runnable));
		}
	}
}