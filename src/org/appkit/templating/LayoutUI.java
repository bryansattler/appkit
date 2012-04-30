package org.appkit.templating;

import org.eclipse.swt.widgets.Control;

/**
 * Implementing this to enable your {@link org.appkit.widget.Component} to have children.
 */
public interface LayoutUI {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/** layout a child with the given layout-options */
	public void layoutChild(final Control child, final Options options);

	/** set the visibility of a child */
	public void setVisible(final Control child, final boolean visible);
}