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
		sb.append("matcher criteria:\n");
		sb.append(this.stringMatcher.toString());
		sb.append("\n");
		sb.append(this.classMatcher.toString());
		sb.append("\ndata:\n\n");

		List<String> primKeys = Lists.newArrayList();
		for (final E o : this.data) {
			primKeys.add(this.stringMatcher.toStringPrimaryKey(o));
		}
		Collections.sort(primKeys, Ordering.natural());
		for (final String k : primKeys) {
			sb.append(k);
			sb.append("\n");
		}

		return sb.toString();
	}

	/** creates a new Naming */
	public static <E> Naming<E> create(final StringQueryMatcher<?super E> stringMatcher) {
		return new Naming<E>(stringMatcher, QueryMatchers.ASSIGNABLE_CLASS);
	}

	/** creates a new Naming */
	public static <E> Naming<E> create(final StringQueryMatcher<?super E> stringMatcher,
									   final ClassQueryMatcher<?super E> classQueryMatcher) {
		return new Naming<E>(stringMatcher, classQueryMatcher);
	}

	/** seals the naming, so queries are allowed to be cached
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

	/** registers a new object with the given name
	 *
	 * @throws IllegalStateException if naming was already sealed
	 * @throws IllegalArgumentException if name or object is null
	 */
	public void register(final E object) {
		Preconditions.checkState(! this.isSealed(), "naming was already sealed");
		Preconditions.checkArgument((object != null), "nulls are not allowedl");

		/* reference-able via exact name */
		this.data.add(object);
	}

	/**
	 * returns the matching object
	 *
	 * @throws IllegalStateException if not exactly 1 was found
	 */
	public <C extends E> C select(final String str) {
		return this.select(str, null);
	}

	/**
	 * returns the matching object, cast to the given class
	 *
	 * @throws IllegalStateException if not exactly 1 was found
	 *
	 */
	public <T extends E> T select(final Class<T> clazz) {
		return this.select(null, clazz);
	}

	/**
	 * returns the matching object, cast to the given class
	 *
	 * @throws IllegalStateException if not exactly 1 was found
	 */
	public <T extends E> T select(final String str, final Class<T> clazz) {

		ImmutableSet<T> results = this.find(str, clazz);

		Preconditions.checkState(
			results.size() == 1,
			"query '%s' / %s returned %s results instead of exactly 1",
			str,
			clazz.getSimpleName(),
			results.size());

		return results.iterator().next();
	}

	/** returns the count of matching objects */
	public <T extends E> int count(final Class<T> clazz) {
		return this.find(null, clazz).size();
	}

	/** returns the count of matching objects */
	public <T extends E> int count(final String str) {
		return this.find(str, null).size();
	}

	/** returns the count of matching objects */
	public <T extends E> int count(final String str, final Class<T> clazz) {
		return this.find(str, null).size();
	}

	/** returns all matching objects */
	public <T extends E> ImmutableSet<T> find(final String str) {
		return this.find(str, null);
	}

	/** returns all matching objects, cast to the given class */
	public <T extends E> ImmutableSet<T> find(final Class<T> clazz) {
		return this.find(null, clazz);
	}

	/** returns all matching objects, cast to the given class */
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
			if (this.classMatcher.matches(object, clazz) && this.stringMatcher.matches(object, str)) {
				;
			}
			builder.add((T) object);
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