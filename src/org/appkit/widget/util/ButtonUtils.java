package org.appkit.widget.util;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Menu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utilities for working with {@link Button}s
 *
 */
public final class ButtonUtils {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L = LoggerFactory.getLogger(ButtonUtils.class);

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * Adds a dropdown menu to the button.
	 */
	public static void setDropDownMenu(final Button button, final Menu menu) {
		/* show menu on button click */
		button.addSelectionListener(
			new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent event) {

						Rectangle buttonBounds = button.getBounds();
						Point abs			   =
							button.getParent().toDisplay(new Point(buttonBounds.x, buttonBounds.y));

						menu.setLocation(abs.x, abs.y + buttonBounds.height);
						menu.setVisible(true);
					}
				});
	}
}