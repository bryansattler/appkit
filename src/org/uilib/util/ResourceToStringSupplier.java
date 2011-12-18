package org.uilib.util;

import com.google.common.collect.Maps;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

public class ResourceToStringSupplier implements StringSupplier {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = Logger.getLogger(ResourceToStringSupplier.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Map<String, String> cache = Maps.newHashMap();

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static ResourceToStringSupplier create() {
		return new ResourceToStringSupplier();
	}

	@Override
	public String get(final String resource) {

		String fullName = "/resources/" + resource;

		if (this.cache.containsKey(fullName)) {
			return this.cache.get(fullName);
		}

		InputStream in = null;
		try {

			URL url = this.getClass().getResource(fullName);
			if (url == null) {
				return null;
			}
			in = new BufferedInputStream(url.openStream());

			/* convert to string */
			String s = new Scanner(in, "UTF8").useDelimiter("\\A").next();

			/* save in cache */
			this.cache.put(fullName, s);

			return s;

		} catch (final IOException e) {}
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {}
		}

		return null;
	}
}