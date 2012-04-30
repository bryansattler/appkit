package org.appkit.widget.util;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;

public final class GridLayoutUtils {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * includes a control in the GridLayout and adjusts the columns-number accordingly
	 * <b>Caution:</b> this is meant to be used in single-row layouts only
	 *
	 */
	public static void show(final Control control) {

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
	public static void hide(final Control control) {

		GridData data     = (GridData) control.getLayoutData();
		GridLayout layout = (GridLayout) control.getParent().getLayout();

		if (! data.exclude) {
			layout.numColumns     = layout.numColumns - 1;
			data.exclude		  = true;
		}
	}
}