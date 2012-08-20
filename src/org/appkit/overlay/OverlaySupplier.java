package org.appkit.overlay;

import org.eclipse.swt.graphics.Image;

public interface OverlaySupplier {

	//~ Methods --------------------------------------------------------------------------------------------------------

	void paintBuffer(final Image buffer);

	boolean copyBackground();

	void dispose();
}