package org.appkit.templating.components;

import org.eclipse.swt.widgets.Control;

import org.appkit.templating.Options;

/**
 * Implementing this to enable your {@link org.appkit.templating.Component} to have children.
 */
public interface LayoutUI extends ComponentUI {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/** layout a child with the given layout-options */
	public void layoutChild(final Control child, final Options options);

	/** set the visibility of a child */
	public void setVisible(final Control child, final boolean visible);
}