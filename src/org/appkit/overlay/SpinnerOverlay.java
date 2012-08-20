package org.appkit.overlay;

import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link Overlay} that displays a semi-transparent spinner.
 *
 */
public final class SpinnerOverlay implements AnimatedOverlaySupplier {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(SpinnerOverlay.class);
	private static final int SPINNER_SIDE					 = 70;
	private static final int INNER_CIRCLE_RADIUS			 = 18;

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private int step = 0;

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public void dispose() {}

	@Override
	public boolean copyBackground() {
		return false;
	}

	@Override
	public void tick() {

		int temp = this.step;
		temp++;
		if (temp == 12) {
			temp = 0;
		}
		this.step = temp;
	}

	@Override
	public long getTickerTime(final TimeUnit targetUnit) {
		return targetUnit.convert(70, TimeUnit.MILLISECONDS);
	}

	@Override
	public void paintBuffer(final Image buffer) {
		if ((buffer.getBounds().width > SPINNER_SIDE) && (buffer.getBounds().height > SPINNER_SIDE)) {
			this.drawSpinner(buffer);
		}
	}

	/* draw spinner */
	private final void drawSpinner(final Image buffer) {

		Color background = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

		GC gc = new GC(buffer);
		gc.setAntialias(SWT.ON);

		/* fill with BG */
		gc.setBackground(background);
		gc.fillRectangle(0, 0, buffer.getBounds().width, buffer.getBounds().height);

		/* draw arc */
		int x = rDiv(buffer.getBounds().width - SPINNER_SIDE, 2);
		int y = rDiv(buffer.getBounds().height - SPINNER_SIDE, 2);

		/* normal arc-angle = 10 */
		int spans[] = new int[12];
		for (int i = 0; i < 12; i++) {
			spans[i] = 10;
		}

		/* active arc: 25, previous: 20, previous: 15 */
		spans[step]				    = 15;
		spans[(step + 11) % 12]     = 13;
		spans[(step + 10) % 12]     = 11;

		/* draw the arcs */
		gc.setAlpha(150);
		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		for (int i = 0; i < 12; i++) {

			int startAngle = (i * 30) - rDiv(spans[i], 2) - 90;
			int diameter   = Math.round(SPINNER_SIDE * (float) 0.6);
			int arcX	   = rDiv(SPINNER_SIDE - diameter, 2);
			int arcY	   = arcX;
			gc.fillArc(x + arcX, y + arcY, diameter, diameter, -startAngle, -spans[i]);
		}

		/* draw circle in the middle */
		gc.setAlpha(255);
		gc.setBackground(background);
		gc.fillOval(
			(x + rDiv(SPINNER_SIDE, 2)) - rDiv(INNER_CIRCLE_RADIUS, 2),
			(y + rDiv(SPINNER_SIDE, 2)) - rDiv(INNER_CIRCLE_RADIUS, 2),
			INNER_CIRCLE_RADIUS,
			INNER_CIRCLE_RADIUS);

		gc.dispose();
	}

	/* utility function: division */
	private final int rDiv(final int dividend, final int divisor) {
		return Math.round(dividend / (float) divisor);
	}
}