package org.appkit.overlay;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Region;

public interface OverlaySupplier {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * renders the image given the size and returns it
	 *
	 * @param overlayWidth width of overlay
	 * @param overlayHeight
	 */
	void paintImage(final Image buffer);

	int getAlpha();

	/**
	 * disposes this Supplier
	 */
	void dispose();

	Region getRegion();
}