package org.appkit.widget.util;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Various utility-functions.
 *
 */
public final class SWTUtils {

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static Point getPositionRelTo(final Control control, final Control referenceControl) {

		Point refPos     = referenceControl.toDisplay(0, 0);
		Point controlPos = control.toDisplay(0, 0);

		return new Point(controlPos.x - refPos.x, controlPos.y - refPos.y);
	}

	public static Point getPositionRelToDisplay(final Control control) {
		return control.toDisplay(0, 0);
	}

	/** adjusts the position of composite so it's visible on the display */
	public static Point moveOntoDisplay(final Composite composite) {

		Rectangle monitorBounds = composite.getDisplay().getPrimaryMonitor().getBounds();
		Rectangle compBounds    = composite.getBounds();

		int x = Math.max(0, compBounds.x);
		if (compBounds.width > monitorBounds.width) {
			x = 0;
		} else {

			int diff = (compBounds.x + compBounds.width) - monitorBounds.width;
			if (diff > 0) {
				x = compBounds.x - diff;
			}
		}

		int y = Math.max(0, compBounds.y);
		if (compBounds.height > monitorBounds.height) {
			y = 0;
		} else {

			int diff = (compBounds.y + compBounds.height) - monitorBounds.height;
			if (diff > 0) {
				y = compBounds.y - diff;
			}
		}

		return new Point(x, y);
	}

	/** returns the Point a composite need to be located at to be in the center of the screen (and a little up) */
	public static Point getCenterPosition(final Composite composite) {

		/* Position in the middle of screen (and a little up) */
		Rectangle monitorBounds = composite.getDisplay().getPrimaryMonitor().getBounds();
		Rectangle compBounds    = composite.getBounds();
		int x				    = monitorBounds.x + ((monitorBounds.width - compBounds.width) / 2);
		int y				    = (monitorBounds.y + ((monitorBounds.height - compBounds.height) / 2)) - 150;

		return new Point(x, y);
	}

	/** returns location to center a composite in the middle of another composite (and a little up) */
	public static Point getCenterPosition(final Composite composite, final Composite parentComp) {

		Rectangle parentBounds = parentComp.getBounds();
		Rectangle compBounds   = composite.getBounds();
		int x				   = parentBounds.x + ((parentBounds.width - compBounds.width) / 2);
		int y				   = parentBounds.y + ((parentBounds.height - compBounds.height) / 2);

		return new Point(x, y);
	}
}