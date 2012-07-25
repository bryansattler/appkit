package org.appkit.templating;


import org.eclipse.swt.widgets.Control;

/**
 * Implement this to enable a {@link Control} to have children.
 */
public interface LayoutUI {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/** Layout a child with the given layout-options */
	public void layoutChild(final Control child, final Options options);
}