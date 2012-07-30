package org.appkit.util;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ParamSupplier} which returns a String by loading a file from resources.
 *
 */
public class ResourceStringSupplier implements ParamSupplier<String, String> {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(ResourceStringSupplier.class);
	private static final ResourceStringSupplier INSTANCE     = new ResourceStringSupplier();

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Map<String, String> cache = Maps.newHashMap();

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private ResourceStringSupplier() {}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static ResourceStringSupplier instance() {
		return INSTANCE;
	}

	@Override
	public String get(final String resource) {

		String fullName = "/resources/" + resource;

		if (this.cache.containsKey(fullName)) {
			return this.cache.get(fullName);
		}

		/* read string from stream */
		String s = null;
		try {

			InputStream in  = ResourceStreamSupplier.create().getInput(resource);
			Scanner scanner = new Scanner(in, "UTF8");
			s			    = scanner.useDelimiter("\\A").next();

			scanner.close();
			in.close();
		} catch (final IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		/* save in cache */
		this.cache.put(fullName, s);

		return s;
	}
}