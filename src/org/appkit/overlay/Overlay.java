package org.appkit.overlay;

import java.util.concurrent.TimeUnit;

import org.appkit.util.SWTSyncedTickReceiver;
import org.appkit.util.SmartExecutor;
import org.appkit.util.Ticker;
import org.appkit.util.Ticker.TickReceiver;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An overlay that can be displayed on top of an existing {@link Composite}.
 *
 * It currently uses a second shell that is modified to reflect size and position of the composite.
 *
 * <b>This is unfinished</b>
 *
 */
public final class Overlay {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(Overlay.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final SmartExecutor executor;
	private final Control topControl;
	private final OverlaySupplier supplier;
	private final ControlChangeListener controlChangeListener;
	private Shell overlayShell;
	private Point lastControlSize							 = new Point(0, 0);
	private Ticker ticker;
	private Image currentImage;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	/**
	 * creates a new overlay on the given {@link Composite}
	 * @param executor
	 */
	public Overlay(final SmartExecutor executor, final Control comp, final OverlaySupplier supplier) {
		this.executor										 = executor;
		this.topControl										 = comp;
		this.supplier										 = supplier;
		this.controlChangeListener							 = new ControlChangeListener();
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * shows the overlay
	 */
	public void show() {
		this.overlayShell = new Shell(this.topControl.getShell(), SWT.NO_TRIM);
		this.overlayShell.addPaintListener(new OverlayPaintListener());
		this.overlayShell.addTraverseListener(
			new TraverseListener() {
					@Override
					public void keyTraversed(final TraverseEvent event) {
						event.doit = false;
					}
				});
		this.topControl.addControlListener(this.controlChangeListener);

		cover();
		overlayShell.open();

		if (this.supplier instanceof AnimatedOverlaySupplier) {

			final AnimatedOverlaySupplier aSupplier = (AnimatedOverlaySupplier) this.supplier;

			/* create a ticker */
			this.ticker =
				this.executor.createTicker(aSupplier.getTickerTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
			this.ticker.notify(new SWTSyncedTickReceiver(topControl.getDisplay(), new AnimationUpdateListener()));
		}
	}

	/**
	 * disposes the overlay
	 */
	public void dispose() {

		/* stop the animator */
		if (this.ticker != null) {
			this.ticker.stop();
		}

		/* remove the paint-listeners */
		this.topControl.removeControlListener(this.controlChangeListener);

		/* dispose the supplier */
		this.supplier.dispose();
	}

	private void cover() {

		final Rectangle compBounds = this.topControl.getBounds();
		final Point absLocation    = this.topControl.getParent().toDisplay(compBounds.x, compBounds.y);
		this.overlayShell.setLocation(absLocation);
		this.overlayShell.setSize(compBounds.width, compBounds.height);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private final class AnimationUpdateListener implements TickReceiver {
		@Override
		public void tick() {

			AnimatedOverlaySupplier aSupplier = (AnimatedOverlaySupplier) supplier;
			aSupplier.tick();

			if (currentImage != null) {
				currentImage.dispose();
				currentImage = null;
			}

			overlayShell.redraw();
			overlayShell.update();
		}
	}

	private final class ControlChangeListener implements ControlListener {
		@Override
		public void controlMoved(final ControlEvent event) {
			cover();
			overlayShell.redraw();
		}

		@Override
		public void controlResized(final ControlEvent event) {
			cover();
			overlayShell.redraw();
		}
	}

	private final class OverlayPaintListener implements PaintListener {
		@Override
		public void paintControl(final PaintEvent event) {

			/* check if composite was resized */
			if ((currentImage == null) || ! topControl.getSize().equals(lastControlSize)) {
				lastControlSize = topControl.getSize();

				ImageData imageData = supplier.getImageData(topControl.getSize().x, topControl.getSize().y);
				imageData.alpha = supplier.getAlpha();

				if (currentImage != null) {
					currentImage.dispose();
				}

				/* make overlay image */
				currentImage = new Image(Display.getCurrent(), imageData);
			}

			Image buffer = new Image(Display.getCurrent(), topControl.getBounds());

			/* take photo of composite into image */
			GC gc = new GC(topControl.getParent());
			gc.copyArea(buffer, 0, 0);
			gc.dispose();

			/* draw add the overlay image */
			GC gcBuffer = new GC(buffer);
			gcBuffer.drawImage(currentImage, 0, 0, event.width, event.height, 0, 0, event.width, event.height);
			gcBuffer.dispose();

			event.gc.drawImage(buffer, 0, 0);
			buffer.dispose();
		}
	}
}