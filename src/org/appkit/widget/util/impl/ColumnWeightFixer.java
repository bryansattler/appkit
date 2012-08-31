package org.appkit.widget.util.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ColumnWeightFixer {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(ColumnWeightFixer.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final ColumnController colController;
	private final ImmutableList<Integer> weights;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public ColumnWeightFixer(final ColumnController colController, final List<Integer> weights) {
		this.colController									 = colController;
		this.weights										 = ImmutableList.copyOf(weights);
		this.colController.installControlListener(new ControlChanged());

		int sum = 0;
		for (final int weight : weights) {
			sum = sum + weight;
		}
		Preconditions.checkArgument(
			(sum >= 0) && (sum <= 100),
			"sum of weights must be larger than 0 and less than 100%");

		int colCount = colController.getColumnCount();
		Preconditions.checkArgument(
			colCount == weights.size(),
			"column-count %s != weight count %s",
			colCount,
			weights.size());

		colController.setColumnsResizable(false);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private class ControlChanged implements ControlListener {
		@Override
		public void controlMoved(final ControlEvent event) {}

		@Override
		public void controlResized(final ControlEvent event) {

			double fraction = colController.getAvailWidth() / 100.0;
			for (int i = 0; i < weights.size(); i++) {

				double width = fraction * weights.get(i);
				colController.setWidth(i, (int) Math.floor(width));
			}
		}
	}
}