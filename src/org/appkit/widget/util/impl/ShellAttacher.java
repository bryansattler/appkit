package org.appkit.widget.util.impl;

import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShellAttacher {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(ShellAttacher.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Shell shell;
	private final Control referenceControl;
	private final ControlListener moveListener;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public ShellAttacher(final Shell shell, final Control referenceControl) {
		this.shell											 = shell;
		this.referenceControl								 = referenceControl;

		this.adjustShellPosition();
		this.moveListener =
			new ControlListener() {
					@Override
					public void controlResized(final ControlEvent event) {
						adjustShellPosition();
					}

					@Override
					public void controlMoved(final ControlEvent event) {
						adjustShellPosition();
					}
				};

		this.referenceControl.addControlListener(this.moveListener);
		this.referenceControl.getShell().addControlListener(this.moveListener);

		shell.addDisposeListener(
			new DisposeListener() {
					@Override
					public void widgetDisposed(final DisposeEvent event) {
						referenceControl.removeControlListener(moveListener);
						referenceControl.getShell().removeControlListener(moveListener);
					}
				});
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	private void adjustShellPosition() {

		int refX = referenceControl.getParent().toDisplay(referenceControl.getLocation()).x;
		int refY = referenceControl.getParent().toDisplay(referenceControl.getLocation()).y;

		if (heightVisibleIfAbove() > heightVisibleIfBelow()) {

			/* position above */
			int x = refX + ((this.referenceControl.getBounds().width - this.shell.getBounds().width) / 2);
			int y = refY - this.shell.getBounds().height;

			this.shell.setLocation(x, y);

		} else {

			/* position below */
			int x = refX + ((this.referenceControl.getBounds().width - this.shell.getBounds().width) / 2);
			int y = refY + this.referenceControl.getBounds().height;

			this.shell.setLocation(x, y);

		}
	}

	private int heightVisibleIfBelow() {

		Range<Integer> yMonitorRange = Ranges.closed(0, referenceControl.getMonitor().getClientArea().height);
		int refY					 = referenceControl.getParent().toDisplay(referenceControl.getLocation()).y;
		int shellHeight				 = shell.getBounds().height;
		int refHeight				 = referenceControl.getBounds().height;
		Range<Integer> shellStretchY = Ranges.closed(refY + refHeight, refY + refHeight + shellHeight);

		if (shellStretchY.isConnected(yMonitorRange)) {
			return shellStretchY.intersection(yMonitorRange).asSet(DiscreteDomains.integers()).size();
		} else {
			return 0;
		}
	}

	private int heightVisibleIfAbove() {

		Range<Integer> yMonitorRange = Ranges.closed(0, referenceControl.getMonitor().getClientArea().height);
		int refY					 = referenceControl.getParent().toDisplay(referenceControl.getLocation()).y;
		int shellHeight				 = shell.getBounds().height;
		Range<Integer> shellStretchY = Ranges.closed(refY - shellHeight, refY);

		if (shellStretchY.isConnected(yMonitorRange)) {
			return shellStretchY.intersection(yMonitorRange).asSet(DiscreteDomains.integers()).size();
		} else {
			return 0;
		}
	}
}