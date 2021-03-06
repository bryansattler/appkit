package org.appkit.preferences;

import com.google.common.collect.ImmutableMap;

/**
 * A simple preferences store.
 * All retrieval methods require a default to be specified, which will
 * be returned when the key isn't found or type-conversion from String fails.
 *
 */
public final class PrefStore {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final PrefStoreBackend backend;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public PrefStore(final PrefStoreBackend backend) {
		this.backend = backend;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * create a new PrefStore using the JavaPreferences back-end
	 *
	 * @param node preferred JavaPreferences node
	 *
	 */
	public static PrefStore createJavaPrefStore(final String node) {
		return new PrefStore(new JavaPreferencesBackend(node));
	}

	/**
	 * store a String
	 */
	public void store(final String key, final String value) {
		this.backend.store(key, value);
	}

	/**
	 * stores a long
	 */
	public void store(final String key, final long value) {
		this.backend.store(key, String.valueOf(value));
	}

	/**
	 * store a boolean
	 */
	public void store(final String key, final boolean value) {
		this.backend.store(key, String.valueOf(value));
	}

	/**
	 * retrieve a String
	 *
	 * @param def default to be returned if key wasn't found
	 */
	public String get(final String key, final String def) {

		String pref = this.backend.get(key);
		if (pref == null) {
			return def;
		}

		return pref;
	}

	/**
	 * retrieves a long
	 *
	 * @param def default to be returned if key wasn't found or {@link Long#valueOf(long_string)} failed.
	 */
	public long get(final String key, final long def) {

		String pref = this.backend.get(key);
		if (pref == null) {
			return def;
		}

		try {
			return Long.valueOf(pref);
		} catch (final NumberFormatException e) {
			return def;
		}
	}

	/**
	 * check if a key-value mapping exists
	 */
	public boolean exists(final String key) {
		return (this.backend.get(key) != null);
	}

	/**
	 * retrieve a boolean
	 *
	 * @param def default to be returned if key wasn't found or stored property is no boolean ("true" or "false")
	 */
	public boolean get(final String key, final boolean def) {

		String pref = this.backend.get(key);
		if (pref == null) {
			return def;
		}

		if (pref.equals("true")) {
			return Boolean.TRUE;
		} else if (pref.equals("false")) {
			return Boolean.FALSE;
		} else {
			return def;
		}
	}

	/**
	 * remove a property
	 */
	public void remove(final String property) {
		this.backend.remove(property);
	}

	/**
	 * returns all stored properties as a map
	 */
	public ImmutableMap<String, String> asMap() {

		ImmutableMap.Builder<String, String> hm = ImmutableMap.builder();
		for (final String key : this.backend.getKeys()) {
			hm.put(key, this.backend.get(key));
		}

		return hm.build();
	}

	/**
	 * returns all stored properties, starting with a prefix as a map of strings
	 */
	public ImmutableMap<String, String> getPrefixMap(final String prefix) {

		ImmutableMap.Builder<String, String> hm = ImmutableMap.builder();
		for (final String key : this.backend.getKeys()) {
			if (key.startsWith(prefix)) {
				hm.put(key, this.get(key, null));
			}
		}

		return hm.build();
	}

	/**
	 * returns all stored properties, starting with a prefix as a map of longs
	 *
	 * @param def value if matched property couldn't be converted to a long
	 */
	public ImmutableMap<String, Long> getPrefixMap(final String prefix, final long def) {

		ImmutableMap.Builder<String, Long> hm = ImmutableMap.builder();
		for (final String key : this.backend.getKeys()) {
			if (key.startsWith(prefix)) {
				hm.put(key, this.get(key, def));
			}
		}

		return hm.build();
	}

	/**
	 * returns all stored properties starting with a prefix as a map of boolean
	 *
	 * @param def value if matched property couldn't be converted to a boolean
	 */
	public ImmutableMap<String, Boolean> getPrefixMap(final String prefix, final boolean def) {

		ImmutableMap.Builder<String, Boolean> hm = ImmutableMap.builder();
		for (final String key : this.backend.getKeys()) {
			if (key.startsWith(prefix)) {
				hm.put(key, this.get(key, def));
			}
		}

		return hm.build();
	}
}