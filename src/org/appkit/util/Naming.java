package org.appkit.util;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This serves as a generic dictionary which allows objects to be retrieved using any
 * a combination of fully-customizable query syntax using a string and a specified class.
 * The object return by the retrieval-methods can be optionally cast to given class.
 *
 */
public final class Naming<E> {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(Naming.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Set<E> data								 = Sets.newHashSet();
	private final StringQueryMatcher<?super E> stringMatcher;
	private final ClassQueryMatcher<?super E> classMatcher;
	private Map<Integer, ImmutableSet<?>> cache;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private Naming(final StringQueryMatcher<?super E> stringMatcher, final ClassQueryMatcher<?super E> classMatcher) {
		this.stringMatcher									 = stringMatcher;
		this.classMatcher									 = classMatcher;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("Naming (" + (this.isSealed() ? "" : "NOT ") + "sealed)\n");
		sb.append("   string matcher: ");
		sb.append(this.stringMatcher.toString());
		sb.append("\n   class matcher: ");
		sb.append(this.classMatcher.toString());
		sb.append("\n\ndata (primary keys):\n");

		List<E> listData				  = Lists.newArrayList(this.data);
		Multimap<String, String> multimap = LinkedListMultimap.create();
		for (int i = 0; i < listData.size(); i++) {

			E o = listData.get(i);
			multimap.put(this.stringMatcher.toStringPrimaryKey(o), this.classMatcher.toStringPrimaryKey(o));
		}

		for (final String name : Ordering.natural().sortedCopy(multimap.keys())) {
			for (final String value : multimap.get(name)) {
				sb.append("   ");
				sb.append(name);
				sb.append(": ");
				sb.append(value);
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	/** creates a new Naming, using a {@link QueryMatchers.AssignableClassMatcher} */
	public static <E> Naming<E> create(final StringQueryMatcher<?super E> stringMatcher) {
		return new Naming<E>(stringMatcher, QueryMatchers.ASSIGNABLE_CLASS);
	}

	/** creates a new Naming */
	public static <E> Naming<E> create(final StringQueryMatcher<?super E> stringMatcher,
									   final ClassQueryMatcher<?super E> classQueryMatcher) {
		return new Naming<E>(stringMatcher, classQueryMatcher);
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
	public <T extends E> int count(final String str) {
		return this.find(str, null).size();
	}

	/**
	 * returns the count of matching objects
	 *
	 * @see #find(String, Class)
	 */
	public <T extends E> int count(final String str, final Class<T> clazz) {
		return this.find(str, null).size();
	}

	/**
	 * checks if a query returns exactly one match
	 *
	 * @see #find(String, Class)
	 */
	public <T extends E> boolean selectable(final Class<T> clazz) {
		return this.find(null, clazz).size() == 1;
	}

	/**
	 * checks if a query returns exactly one match
	 *
	 * @see #find(String, Class)
	 */
	public boolean selectable(final String str) {
		return this.find(str, null).size() == 1;
	}

	/**
	 * checks if a query returns exactly one match
	 *
	 * @see #find(String, Class)
	 */
	public <T extends E> boolean selectable(final String str, final Class<T> clazz) {
		return this.find(str, null).size() == 1;
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
	 * returns all matching objects, cast to the given class
	 *
	 * @param str string-part of query, can be null, but not empty
	 * @param clazz class-part of query, can be null
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends E> ImmutableSet<T> find(final String str, final Class<T> clazz) {
		Preconditions.checkArgument(
			(str == null) || ! str.isEmpty(),
			"name-query should not be '', omit it entirely instead");

		int hashCode = Objects.hashCode(str, clazz);

		/* cache lookup */
		if ((this.cache != null) && this.cache.containsKey(hashCode)) {
			return (ImmutableSet<T>) this.cache.get(hashCode);
		}

		/* find results */
		ImmutableSet.Builder<T> builder = ImmutableSet.builder();
		for (final E object : this.data) {

			boolean match = true;
			if ((clazz != null) && ! this.classMatcher.matches(object, clazz)) {
				match = false;
			}

			if (match && (str != null) && ! this.stringMatcher.matches(object, str)) {
				match = false;
			}

			if (match) {
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

	public interface StringQueryMatcher<E> {
		String toStringPrimaryKey(final E object);

		boolean matches(final E object, final String str);

		Set<String> precomputeMatches(final E object);
	}

	public interface ClassQueryMatcher<E> {
		String toStringPrimaryKey(final E object);

		boolean matches(final E object, final Class<?> clazz);

		Set<Class<?>> precomputeMatches(final E object);
	}
}