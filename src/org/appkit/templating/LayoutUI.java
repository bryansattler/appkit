package org.appkit.templating;

import org.appkit.widget.Options;

import org.eclipse.swt.widgets.Control;

/**
 * Implementing this to enable your {@link org.appkit.widget.Component} to have children.
 */
public interface LayoutUI {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/** layout a child with the given layout-options */
	public void layoutChild(final Control child, final Options options);
}