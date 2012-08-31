package org.appkit.templating;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds options for component and provides methods for working with. For retrieval operations
 * defaults have to be specified, similar to {@link org.appkit.preferences.PrefStore}.
 *
 * <li>For keys of options only the following characters are valid: a-z,A-Z, '?', '_' and '-'.
 * <li>boolean options values can be "true","false","yes" or "no"
 * <li>an options called "options" will be split by space into a list of boolean options.
 *     Specifying <code>options="border bold"</code> equals specifying <code>border=yes</code> and <code>bold=yes</code>.
 * Trying to construct invalid options or options that contradict itself will throw an IllegalArgumentException.
 *
 */
public class Options {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L								  = LoggerFactory.getLogger(Options.class);
	private static final ImmutableMap<String, Boolean> boolTransl =
		ImmutableMap.of("true", Boolean.TRUE, "yes", Boolean.TRUE, "false", Boolean.FALSE, "no", Boolean.FALSE);
	private static final CharMatcher nameFilter =
		CharMatcher.inRange('a', 'z').or(CharMatcher.inRange('0', '9')).or(CharMatcher.anyOf("?!-_"));

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final ImmutableMap<String, String> options;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private Options(final Map<String, String> options) {
		for (final String key : options.keySet()) {
			Preconditions.checkArgument(nameFilter.matchesAllOf(key), "only a-z, '-' and '?' allowed in name");
		}
		this.options						    = ImmutableMap.copyOf(options);
		L.debug("created options: {}", this.options);
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	/** creates empty Options */
	public static Options empty() {
		return new Options(ImmutableMap.<String, String>of());
	}

	/** creates Options from the given map*/
	public static Options of(final Map<String, String> options) {
		return new Options(options);
	}

	/** creates Options with multiple boolean keys set to "true" */
	public static Options set(final String... booleanKeys) {

		Map<String, String> map = Maps.newHashMap();
		for (final String key : booleanKeys) {
			map.put(key, "true");
		}

		return new Options(map);
	}

	/**
	 * returns new options where this options are combined with the specified defaults,
	 * which are inserted if they weren't specified.
	 */
	public Options withDefaults(final Options defaults) {

		Map<String, String> newOptions = Maps.newHashMap();
		newOptions.putAll(this.options);

		for (final Entry<String, String> entry : defaults.asMap().entrySet()) {
			if (! newOptions.containsKey(entry.getKey())) {
				newOptions.put(entry.getKey(), entry.getValue());
			}
		}

		return new Options(newOptions);
	}

	/**
	 * returns an option as a boolean
	 *
	 * @return def is returns if option wasn't found or didn't match "true","false","yes" or "no"
	 */
	public boolean get(final String key, final boolean def) {

		String option = this.options.get(key);
		if (option == null) {
			return def;
		} else {
			Preconditions.checkArgument(
				boolTransl.containsKey(option.toLowerCase(Locale.ENGLISH)),
				option + " no a valid boolean option");

			return boolTransl.get(option.toLowerCase(Locale.ENGLISH));
		}
	}

	/**
	 * returns an option as an int
	 *
	 * @return def is returns if option wasn't found or couldn't be converted to an Integer
	 */
	public int get(final String key, final int def) {

		String option = this.options.get(key);
		if (option == null) {
			return def;
		}

		try {
			return Integer.valueOf(option);
		} catch (final NumberFormatException e) {
			return def;
		}
	}

	/**
	 * returns an option as a string
	 *
	 * @return def is returns if option wasn't found
	 */
	public String get(final String key, final String def) {

		String option = this.options.get(key);
		if (option == null) {
			return def;
		} else {
			return option;
		}
	}

	/**
	 * returns an option as a List of Strings.
	 * The option's value is splitted by space, the inividual options are trimmed.
	 *
	 * @throws IllegalArgumentException if the key was "options" since this is special key that will be split into booleans
	 */
	public ImmutableList<String> getList(final String key) {
		Preconditions.checkArgument(! key.equals("options"), "'options' is translated into boolean options");

		String option = this.options.get(key);
		if (option == null) {
			return ImmutableList.of();
		} else {
			return ImmutableList.copyOf(Splitter.on(' ').trimResults().split(option));
		}
	}

	/**
	 * returns this options as a map
	 */
	public ImmutableMap<String, String> asMap() {
		return this.options;
	}
}