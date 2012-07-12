package org.appkit.widget.util;

import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
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

	//~ Constructors ---------------------------------------------------------------------------------------------------

	/**
	 * Ideas: If control isn't fully visible, shell slide out of the monitor too.
	 *        Until that happens, the shell is tried to displayed completely on the monitor.
	 *
	 */
	protected ShellAttacher(final Shell shell, final Control referenceControl) {
		this.shell											 = shell;
		this.referenceControl								 = referenceControl;

		this.adjustShellPosition();

		this.referenceControl.addControlListener(
			new ControlListener() {
					@Override
					public void controlResized(final ControlEvent arg0) {
						adjustShellPosition();
					}

					@Override
					public void controlMoved(final ControlEvent arg0) {
						adjustShellPosition();
					}
				});
		this.referenceControl.getShell().addControlListener(
			new ControlListener() {
					@Override
					public void controlResized(final ControlEvent arg0) {
						adjustShellPosition();
					}

					@Override
					public void controlMoved(final ControlEvent arg0) {
						adjustShellPosition();
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

		Range<Integer> yMonitorRange = Ranges.closed(0, referenceControl.getMonitor().getBounds().height);
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

		Range<Integer> yMonitorRange = Ranges.closed(0, referenceControl.getMonitor().getBounds().height);
		int refY					 = referenceControl.getParent().toDisplay(referenceControl.getLocation()).y;
		int shellHeight				 = shell.getBounds().height;
		Range<Integer> shellStretchY = Ranges.closed(refY - shellHeight, refY);

		if (shellStretchY.isConnected(yMonitorRange)) {
			return shellStretchY.intersection(yMonitorRange).asSet(DiscreteDomains.integers()).size();
		} else {
			return 0;
		}
	}

	@SuppressWarnings("unused")
	private int widthVisibleIfBefore() {

		Range<Integer> xMonitorRange = Ranges.closed(0, referenceControl.getMonitor().getBounds().width);

		int refX					 = referenceControl.toDisplay(referenceControl.getLocation()).x;
		int shellWidth				 = shell.getBounds().width;
		Range<Integer> shellStretchX = Ranges.closed(refX - shellWidth, refX);

		if (shellStretchX.isConnected(xMonitorRange)) {
			return shellStretchX.intersection(xMonitorRange).asSet(DiscreteDomains.integers()).size();
		} else {
			return 0;
		}
	}

	@SuppressWarnings("unused")
	private int widthVisibleIfAfter() {

		Range<Integer> xMonitorRange = Ranges.closed(0, referenceControl.getMonitor().getBounds().width);

		int refX					 = referenceControl.toDisplay(referenceControl.getLocation()).x;
		int shellWidth				 = shell.getBounds().width;
		int refWidth				 = referenceControl.getBounds().width;
		Range<Integer> shellStretchX = Ranges.closed(refX + refWidth, refX + refWidth + shellWidth);

		if (shellStretchX.isConnected(xMonitorRange)) {
			return shellStretchX.intersection(xMonitorRange).asSet(DiscreteDomains.integers()).size();
		} else {
			return 0;
		}
	}
}