package org.appkit.widget.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * Various utilities for working with SWT.
 *
 */
public final class SWTUtils {

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static void moveToMonitorCenter(final Shell shell) {
		shell.setLocation(SWTUtils.getCenterPosition(shell));
	}

	public static void moveToCenterOf(final Control control, final Control referenceControl) {
		control.setLocation(SWTUtils.getRelativeCenterPosition(control, referenceControl));
	}

	/** adjusts a rectangle, so that it stays on the client-ares of a given monitor */
	public static Point moveOntoMonitor(final Rectangle controlBounds, final Monitor monitor) {

		Rectangle monitorBounds = monitor.getClientArea();

		int x = Math.max(0, controlBounds.x);
		if (controlBounds.width > monitorBounds.width) {
			x = 0;
		} else {

			int diff = (controlBounds.x + controlBounds.width) - monitorBounds.width;
			if (diff > 0) {
				x = controlBounds.x - diff;
			}
		}

		int y = Math.max(0, controlBounds.y);
		if (controlBounds.height > monitorBounds.height) {
			y = 0;
		} else {

			int diff = (controlBounds.y + controlBounds.height) - monitorBounds.height;
			if (diff > 0) {
				y = controlBounds.y - diff;
			}
		}

		return new Point(x, y);
	}

	/** Returns the Point a control need to be located at to be in the center of the primary monitor (and 150px up).
	 *  The point is also adjusted that the composite completely stays on the primary monitor.
	 */
	public static Point getCenterPosition(final Control control) {

		/* Position in the middle of screen (and a little up) */
		Rectangle monitorBounds = control.getDisplay().getPrimaryMonitor().getClientArea();
		Rectangle controlBounds = control.getBounds();
		int x				    = monitorBounds.x + ((monitorBounds.width - controlBounds.width) / 2);
		int y				    = (monitorBounds.y + ((monitorBounds.height - controlBounds.height) / 2)) - 150;

		Rectangle bounds = new Rectangle(x, y, controlBounds.width, controlBounds.height);

		return moveOntoMonitor(bounds, control.getDisplay().getPrimaryMonitor());
	}

	/** Returns the Point a control need to be located at to be in the center of the reference control (and 150px up).
	 *  The point is also adjusted that the composite completely stays on the monitor the reference control is on.
	 */
	public static Point getRelativeCenterPosition(final Control control, final Control referenceControl) {

		Rectangle referenceControlBounds = referenceControl.getBounds();
		Rectangle controlBounds			 = control.getBounds();
		int x							 =
			referenceControlBounds.x + ((referenceControlBounds.width - controlBounds.width) / 2);
		int y = (referenceControlBounds.y + ((referenceControlBounds.height - controlBounds.height) / 2)) - 150;

		Rectangle bounds = new Rectangle(x, y, controlBounds.width, controlBounds.height);

		return moveOntoMonitor(bounds, referenceControl.getMonitor());
	}

	@SuppressWarnings("unchecked")
	public static <E extends Control> ImmutableList<E> findAllChildren(final Composite parent, final Class<E> clazz) {

		List<E> results		   = Lists.newArrayList();
		List<Control> workList = Lists.newArrayList();
		workList.add(parent);
		while (! workList.isEmpty()) {

			Control c = workList.remove(0);
			if (c.getClass().isAssignableFrom(clazz)) {
				results.add((E) c);
			} else if (c instanceof Composite) {
				workList.addAll(Arrays.asList(((Composite) c).getChildren()));
			}
		}

		return ImmutableList.copyOf(results);
	}
}