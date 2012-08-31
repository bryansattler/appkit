package org.appkit.overlay;

import com.google.common.base.Preconditions;

import java.util.concurrent.TimeUnit;

import org.appkit.concurrent.SWTSyncedRunnable;
import org.appkit.concurrent.SmartExecutor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
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
	private final SmartExecutor smartExecutor;

	/* overlay */
	private Shell shell;
	private Runnable animationRunnable;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private Overlay(final Control control, final OverlaySupplier overlaySupplier, final SmartExecutor smartExecutor) {
		this.control										 = control;
		this.overlaySupplier								 = overlaySupplier;
		this.controlChangeListener							 = new ControlChangeListener();
		this.smartExecutor									 = smartExecutor;
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
												final SmartExecutor smartExecutor) {
		Preconditions.checkNotNull(control);
		Preconditions.checkNotNull(animatedSupplier);
		Preconditions.checkNotNull(smartExecutor);

		return new Overlay(control, animatedSupplier, smartExecutor);
	}

	/**
	 * shows the overlay
	 */
	public void show() {
		this.shell =
			new Shell(
				this.control.getShell(),
				SWT.DOUBLE_BUFFERED | SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND | SWT.NO_TRIM);
		this.shell.setLayout(new FillLayout());
		this.shell.addPaintListener(new OverlayPaintListener());
		this.shell.addTraverseListener(
			new TraverseListener() {
					@Override
					public void keyTraversed(final TraverseEvent event) {
						event.doit = false;
					}
				});
		this.shell.addFocusListener(
			new FocusAdapter() {
					@Override
					public void focusGained(final FocusEvent event) {
						control.getShell().getDisplay().asyncExec(
							new Runnable() {
									@Override
									public void run() {
										control.getShell().setFocus();
									}
								});
					}
				});

		this.cover();

		/* adjust size of overlay-shell when control changes size or position */
		this.control.getShell().addControlListener(this.controlChangeListener);

		/* start animation if applicable */
		if (this.overlaySupplier instanceof AnimatedOverlaySupplier) {

			final AnimatedOverlaySupplier aSupplier = (AnimatedOverlaySupplier) this.overlaySupplier;

			/* create a ticker */
			this.animationRunnable = new SWTSyncedRunnable(this.control.getDisplay(), new AnimationUpdateListener());
			this.smartExecutor.scheduleAtFixedRate(
				aSupplier.getTickerTime(TimeUnit.MILLISECONDS),
				TimeUnit.MILLISECONDS,
				this.animationRunnable);
		}

		this.shell.setVisible(true);
	}

	/**
	 * disposes the overlay
	 */
	public void dispose() {

		/* stop the animator */
		if (this.animationRunnable != null) {
			this.smartExecutor.cancelRepeatingRunnable(this.animationRunnable);
		}

		/* remove the control-listeners */
		this.control.getShell().removeControlListener(this.controlChangeListener);

		/* dispose the stuff */
		this.shell.dispose();
		this.overlaySupplier.dispose();
	}

	private void cover() {

		Rectangle compBounds = this.control.getBounds();
		Point absLocation    = this.control.getParent().toDisplay(compBounds.x, compBounds.y);
		this.shell.setLocation(absLocation);
		this.shell.setSize(compBounds.width, compBounds.height);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private final class AnimationUpdateListener implements Runnable {
		@Override
		public void run() {
			if (shell.isDisposed()) {
				return;
			}

			AnimatedOverlaySupplier aSupplier = (AnimatedOverlaySupplier) overlaySupplier;
			aSupplier.tick();

			shell.redraw();
		}
	}

	private final class ControlChangeListener implements ControlListener {
		@Override
		public void controlMoved(final ControlEvent event) {
			cover();
			shell.redraw();
		}

		@Override
		public void controlResized(final ControlEvent event) {
			cover();
			shell.redraw();
		}
	}

	private final class OverlayPaintListener implements PaintListener {
		@Override
		public void paintControl(final PaintEvent event) {

			Image buffer = null;
			if (overlaySupplier.copyBackground()) {
				buffer = new Image(Display.getCurrent(), control.getBounds().width, control.getBounds().height);

				GC controlGC = new GC(buffer);
				control.print(controlGC);
				controlGC.dispose();

			} else {
				buffer = new Image(Display.getCurrent(), control.getBounds().width, control.getBounds().height);
			}

			overlaySupplier.paintBuffer(buffer);
			event.gc.drawImage(buffer, 0, 0);
			buffer.dispose();
		}
	}
}