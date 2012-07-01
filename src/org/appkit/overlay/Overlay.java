package org.appkit.overlay;

import com.google.common.base.Preconditions;

import java.util.concurrent.TimeUnit;

import org.appkit.util.SWTSyncedTickReceiver;
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

	private final Control control;
	private final ControlChangeListener controlChangeListener;
	private final OverlaySupplier overlaySupplier;

	/* for animation */
	private final Ticker.Supplier tickerSupplier;
	private Ticker ticker;

	/* overlay */
	private Shell overlayShell;
	private Image buffer;
	private boolean redrawBuffer							 = true;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private Overlay(final Control control, final OverlaySupplier overlaySupplier, final Ticker.Supplier tickerSupplier) {
		this.control				   = control;
		this.overlaySupplier		   = overlaySupplier;
		this.controlChangeListener     = new ControlChangeListener();
		this.tickerSupplier			   = tickerSupplier;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * creates a new overlay on the given {@link Control}
	 */
	public static Overlay createOverlay(final Control control, final OverlaySupplier overlaySupplier) {
		Preconditions.checkArgument(
			! (overlaySupplier instanceof AnimatedOverlaySupplier),
			"use the appropriate constructor for animated overlays");
		Preconditions.checkNotNull(control);
		Preconditions.checkNotNull(overlaySupplier);
		return new Overlay(control, overlaySupplier, null);
	}

	/**
	 * creates a new animated overlay on the given {@link Control}
	 */
	public static Overlay createAnimatedOverlay(final Control control, final AnimatedOverlaySupplier animatedSupplier,
												final Ticker.Supplier tickerSupplier) {
		Preconditions.checkNotNull(control);
		Preconditions.checkNotNull(animatedSupplier);
		Preconditions.checkNotNull(tickerSupplier);
		return new Overlay(control, animatedSupplier, tickerSupplier);
	}

	/**
	 * shows the overlay
	 */
	public void show() {
		this.overlayShell = new Shell(this.control.getShell(), SWT.NO_TRIM | SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND);
		this.overlayShell.setAlpha(this.overlaySupplier.getAlpha());
		this.overlayShell.addPaintListener(new OverlayPaintListener());
		this.overlayShell.addTraverseListener(
			new TraverseListener() {
					@Override
					public void keyTraversed(final TraverseEvent event) {
						event.doit = false;
					}
				});
		this.cover();
		this.overlayShell.open();

		/* adjust size of overlay-shell when control changes size or position */
		this.control.getShell().addControlListener(this.controlChangeListener);

		/* start animation if applicable */
		if (this.overlaySupplier instanceof AnimatedOverlaySupplier) {

			final AnimatedOverlaySupplier aSupplier = (AnimatedOverlaySupplier) this.overlaySupplier;

			/* create a ticker */
			this.ticker =
				this.tickerSupplier.createTicker(aSupplier.getTickerTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
			this.ticker.startNotifiying(new SWTSyncedTickReceiver(control.getDisplay(), new AnimationUpdateListener()));
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

		/* remove the control-listeners */
		this.control.removeControlListener(this.controlChangeListener);

		/* dispose the shell */
		this.overlayShell.dispose();
		this.buffer.dispose();

		/* dispose the supplier */
		this.overlaySupplier.dispose();
	}

	private void cover() {

		final Rectangle compBounds = this.control.getBounds();
		final Point absLocation    = this.control.getParent().toDisplay(compBounds.x, compBounds.y);
		this.overlayShell.setLocation(absLocation);
		this.overlayShell.setSize(compBounds.width, compBounds.height);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private final class AnimationUpdateListener implements TickReceiver {
		@Override
		public void tick() {

			AnimatedOverlaySupplier aSupplier = (AnimatedOverlaySupplier) overlaySupplier;
			aSupplier.tick();

			redrawBuffer = true;
			overlayShell.redraw();
		}
	}

	private final class ControlChangeListener implements ControlListener {
		@Override
		public void controlMoved(final ControlEvent event) {
			cover();
			redrawBuffer = true;
			overlayShell.redraw();
		}

		@Override
		public void controlResized(final ControlEvent event) {
			cover();
			redrawBuffer = true;
			overlayShell.redraw();
		}
	}

	private final class OverlayPaintListener implements PaintListener {
		@Override
		public void paintControl(final PaintEvent event) {

			/* if composite was resized or buffer isn't initialized, create a new buffer */
			boolean recreateBuffer = false;
			if ((buffer == null)) {
				recreateBuffer = true;
			} else {

				Point bufferSize = new Point(buffer.getBounds().width, buffer.getBounds().height);
				if (! control.getSize().equals(bufferSize)) {
					recreateBuffer = true;
				}
			}

			if (recreateBuffer) {
				if (buffer != null) {
					buffer.dispose();
				}
				buffer			 = new Image(Display.getCurrent(), control.getSize().x, control.getSize().y);
				redrawBuffer     = true;
			}

			if (redrawBuffer) {
				redrawBuffer = false;

				/* paint image on buffer */
				ImageData imageData = overlaySupplier.getImageData(control.getSize().x, control.getSize().y);
				Image temp		    = new Image(Display.getCurrent(), imageData);

				GC gcBuffer = new GC(buffer);
				gcBuffer.drawImage(temp, 0, 0);
				gcBuffer.dispose();
				temp.dispose();

			}
			event.gc.drawImage(buffer, 0, 0); //, event.width, event.height, 0, 0, event.width, event.height);
		}
	}
}