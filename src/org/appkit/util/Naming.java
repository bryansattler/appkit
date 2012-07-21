package org.appkit.util;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This serves as a generic dictionary which allows objects to be retrieved by combination of a string and a class (or just one of those).
 * If a class was specified, the results contain all objects that can be casted to that class. Test is done by
 * {@link Class#isAssignableFrom(Class)}.
 * If a String was specified the matcher needed for construction will be queried as well.
 *
 */
public final class Naming<E> {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(Naming.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Set<E> data						  = Sets.newHashSet();
	private final QueryMatcher<?super E> queryMatcher;
	private Map<Integer, ImmutableSet<?>> cache;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private Naming(final QueryMatcher<?super E> queryMatcher) {
		this.queryMatcher							  = queryMatcher;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("Naming (" + (this.isSealed() ? "" : "NOT ") + "sealed)\n");
		sb.append("   querymatcher: ");
		sb.append(this.queryMatcher.toString());
		sb.append("\n\ndata (primary keys):\n");

		List<String> keyList = Lists.newArrayList();
		for (final E o : this.data) {
			keyList.add(this.queryMatcher.toStringPrimaryKey(o));
		}
		Collections.sort(keyList, Ordering.natural());
		for (final String name : keyList) {
			sb.append("   ");
			sb.append(name);
			sb.append("\n");
		}

		return sb.toString();
	}

	/** creates a new Naming */
	public static <E> Naming<E> create(final QueryMatcher<?super E> queryMatcher) {
		return new Naming<E>(queryMatcher);
	}

	/**
	 * seals the naming, so queries are allowed to be cached
	 *
	 * @throws IllegalStateException if naming was already sealed
	 */
	public void seal() {
		Preconditions.checkState(! this.isSealed(), "naming was already sealed");
		this.cache = Maps.newHashMap();
	}

	/** returns if naming was sealed */
	public boolean isSealed() {
		return (this.cache != null);
	}

	/**
	 * registers a new object with the given name
	 *
	 * @throws IllegalStateException if naming was already sealed
	 * @throws IllegalArgumentException if object is null
	 */
	public void put(final E object) {
		Preconditions.checkState(! this.isSealed(), "naming was already sealed");
		Preconditions.checkArgument((object != null), "nulls are not allowed");

		/* reference-able via exact name */
		this.data.add(object);
	}

	/**
	 * returns the matching object
	 *
	 * @see #select(String, Class)
	 * @throws IllegalStateException if not exactly 1 was found
	 */
	public <C extends E> C select(final String str) {
		return this.select(str, null);
	}

	/**
	 * returns the matching object
	 *
	 * @see #select(String, Class)
	 * @throws IllegalStateException if not exactly 1 was found
	 */
	public <T extends E> T select(final Class<T> clazz) {
		return this.select(null, clazz);
	}

	/**
	 * returns the matching object
	 *
	 * @see #find(String, Class)
	 * @throws IllegalStateException if not exactly 1 was found
	 */
	public <T extends E> T select(final String str, final Class<T> clazz) {

		ImmutableSet<T> results = this.find(str, clazz);

		Preconditions.checkState(
			results.size() == 1,
			"query %s / %s returned %s results instead of exactly 1",
			((str == null) ? "*" : ("'" + str + "'")),
			((clazz == null) ? "*" : clazz.getSimpleName()),
			results.size());

		return results.iterator().next();
	}

	/**
	 * returns the count of matching objects
	 *
	 * @see #find(String, Class)
	 */
	public <T extends E> int count(final String str) {
		return this.find(str, null).size();
	}

	/**
	 * returns the count of matching objects
	 *
	 * @see #find(String, Class)
	 */
	public <T extends E> int count(final Class<T> clazz) {
		return this.find(null, clazz).size();
	}

	/**
	 * returns the count of matching objects
	 *
	 * @see #find(String, Class)
	 */
	public <T extends E> int count(final String str, final Class<T> clazz) {
		return this.find(str, clazz).size();
	}

	/**
	 * checks if a query returns exactly one match
	 *
	 * @see #find(String, Class)
	 */
	public boolean selectable(final String str) {
		return this.count(str) == 1;
	}

	/**
	 * checks if a query returns exactly one match
	 *
	 * @see #find(String, Class)
	 */
	public <T extends E> boolean selectable(final Class<T> clazz) {
		return this.count(clazz) == 1;
	}

	/**
	 * checks if a query returns exactly one match
	 *
	 * @see #find(String, Class)
	 */
	public <T extends E> boolean selectable(final String str, final Class<T> clazz) {
		return this.count(str, clazz) == 1;
	}

	/**
	 * returns all matching objects
	 *
	 * @see #find(String, Class)
	 */
	public <T extends E> ImmutableSet<T> find(final String str) {
		return this.find(str, null);
	}

	/**
	 * returns all matching objects
	 *
	 * @see #find(String, Class)
	 */
	public <T extends E> ImmutableSet<T> find(final Class<T> clazz) {
		return this.find(null, clazz);
	}

	/**
	 * returns all matching objects, cast to the given class
	 *
	 * @param str string-part of query, can be null
	 * @param clazz class-part of query, can be null
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends E> ImmutableSet<T> find(final String str, final Class<T> clazz) {

		int hashCode = Objects.hashCode(str, clazz);

		/* cache lookup */
		if ((this.cache != null) && this.cache.containsKey(hashCode)) {
			return (ImmutableSet<T>) this.cache.get(hashCode);
		}

		/* find results */
		ImmutableSet.Builder<T> builder = ImmutableSet.builder();
		for (final E object : this.data) {
			boolean clazzMatch = clazz == null || clazz.isAssignableFrom(object.getClass());
			boolean stringMatch = str == null || this.queryMatcher.matches(object, str);

			if (clazzMatch && stringMatch) {
				builder.add((T) object);
			}
		}
		ImmutableSet<T> results = builder.build();

		/* cache save */
		if (this.cache != null) {
			this.cache.put(hashCode, results);
		}

		return results;
	}

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

	public interface QueryMatcher<E> {
		/* used for the toString method of Naming */
		String toStringPrimaryKey(final E object);

		/* checks if the string matches a certain object */
		boolean matches(final E object, final String query);
	}
}