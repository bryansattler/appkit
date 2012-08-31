package org.appkit.util;

import com.google.common.base.Preconditions;
import com.google.common.io.InputSupplier;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ParamSupplier} which returns an InputStream by loading a file from resources.
 *
 */
public class ResourceStreamSupplier implements ParamInputSupplier<String, InputStream> {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L = LoggerFactory.getLogger(ResourceStreamSupplier.class);

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private ResourceStreamSupplier() {}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static ResourceStreamSupplier create() {
		return new ResourceStreamSupplier();
	}

	public InputSupplier<InputStream> getSupplier(final String resource) {
		return new InputSupplier<InputStream>() {
				@Override
				public InputStream getInput() throws IOException {
					return ResourceStreamSupplier.this.getInput(resource);
				}
			};
	}

	/** returns an InputStream for the resource */
	@Override
	public InputStream getInput(final String resource) throws IOException {

		String res			   = "/resources/" + resource;
		BufferedInputStream in = new BufferedInputStream(this.getClass().getResourceAsStream(res));

		try {
			in.available();
			return in;
		} catch (final IOException e) {
			Preconditions.checkArgument(false, "Resource '%s' not found", res);
			return null;
		}
	}
}