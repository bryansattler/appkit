package org.appkit.widget.util;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;

/**
 * Various utilities for working with {@link GridLayout}s
 *
 */
public final class GridUtils {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * includes a control in the GridLayout and adjusts the columns-number accordingly
	 * <b>Caution:</b> this is meant to be used in single-row layouts only
	 *
	 */
	public static void singleRowShow(final Control control) {

		GridData data     = (GridData) control.getLayoutData();
		GridLayout layout = (GridLayout) control.getParent().getLayout();

		if (data.exclude) {
			layout.numColumns     = layout.numColumns + 1;
			data.exclude		  = false;
		}
	}

	/**
	 * excludes a control from the GridLayout and adjusts the columns-number accordingly
	 * <b>Caution:</b> this is meant to be used in single-row layouts only
	 *
	 */
	public static void singleRowHide(final Control control) {

		GridData data     = (GridData) control.getLayoutData();
		GridLayout layout = (GridLayout) control.getParent().getLayout();

		if (! data.exclude) {
			layout.numColumns     = layout.numColumns - 1;
			data.exclude		  = true;
		}
	}

	public static void hide(final Control... controls) {
		for (final Control c : controls) {
			((GridData) c.getLayoutData()).exclude = true;
			c.setVisible(false);
		}
	}

	public static void show(final Control... controls) {
		for (final Control c : controls) {
			((GridData) c.getLayoutData()).exclude = false;
			c.setVisible(true);
		}
	}
}