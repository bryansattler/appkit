package org.appkit.util;

import java.io.IOException;
import java.io.InputStream;

public interface ParamInputSupplier<K, V extends InputStream> {

	//~ Methods --------------------------------------------------------------------------------------------------------

	public V getInput(final K key) throws IOException;
}