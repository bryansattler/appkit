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
 * Extend this class to add a naming-hierarchy to your class.
 * This serves as a generic dictionary which allows objects to be retrieved by combination of a string and a class (or just one of those).
 * If a class was specified, the results contain all objects that can be casted to that class. Test is done by
 * {@link Class#isAssignableFrom(Class)}.
 * If a String was specified the matcher needed for construction will be queried as well.
 *
 */
public class Naming<E> {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(Naming.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Set<E> data;
	private QueryMatcher<?super E> queryMatcher;
	private Map<Integer, ImmutableSet<?>> cache;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public Naming() {
		this.queryMatcher									 = QueryMatcher.ALL_MATCHER;
		this.data											 = Sets.newHashSet();
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	protected final void setQueryMatcher(final QueryMatcher<E> matcher) {
		Preconditions.checkState(! this.isSealed(), "naming-sealed, can't change query-matcher");
		Preconditions.checkNotNull(matcher);
		this.queryMatcher = matcher;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("Naming (" + (this.isSealed() ? "" : "NOT ") + "sealed)\n");
		sb.append("querymatcher: ");
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

	/**
	 * seals the naming, so queries are allowed to be cached
	 *
	 * @throws IllegalStateException if naming was already sealed
	 */
	public final void seal() {
		Preconditions.checkState(! this.isSealed(), "naming was already sealed");
		this.cache = Maps.newHashMap();
	}

	/** returns if naming was sealed */
	private final boolean isSealed() {
		return (this.cache != null);
	}

	/**
	 * registers a new object with the given name
	 *
	 * @throws IllegalStateException if naming was already sealed
	 * @throws NullPointerException if arguments were null
	 */
	public final void put(final E object) {
		Preconditions.checkNotNull(object, "parameters for put(object) must not be null");
		Preconditions.checkState(! this.isSealed(), "naming was already sealed");

		/* reference-able via exact name */
		this.data.add(object);
	}

	/**
	 * returns all matching objects casted to the given class
	 *
	 * @param str string/name-part of query
	 * @param clazz class-part of query
	 *
	 * @throws NullPointerException if arguments where null
	 */
	public <T extends E> ImmutableSet<T> find(final String str, final Class<T> clazz) {
		Preconditions.checkNotNull(str, "parameters for select(string, class) must not be null");
		Preconditions.checkNotNull(clazz, "parameters for select(string, class) must not be null");

		return impl_find(str, clazz);
	}

	@SuppressWarnings("unchecked")
	private <T extends E> ImmutableSet<T> impl_find(final String str, final Class<T> clazz) {

		int hashCode = Objects.hashCode(str, clazz);

		/* cache lookup */
		if ((this.cache != null) && this.cache.containsKey(hashCode)) {
			return (ImmutableSet<T>) this.cache.get(hashCode);
		}

		/* find results */
		ImmutableSet.Builder<T> builder = ImmutableSet.builder();
		for (final E object : this.data) {

			boolean clazzMatch  = (clazz == null) || clazz.isAssignableFrom(object.getClass());
			boolean stringMatch = (str == null) || this.queryMatcher.matches(object, str);

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

	/**
	 * returns the matching object
	 *
	 * @see #find(String, Class)
	 * @throws NullPointerException if arguments were null
	 * @throws IllegalStateException if not exactly 1 was found
	 */
	public final <T extends E> T select(final String str, final Class<T> clazz) {

		ImmutableSet<T> results = this.impl_find(str, clazz);

		Preconditions.checkState(
			results.size() == 1,
			"query %s / %s returned %s results instead of exactly 1",
			((str == null) ? "*" : ("'" + str + "'")),
			((clazz == null) ? "*" : clazz.getSimpleName()),
			results.size());

		return results.iterator().next();
	}

	/**
	 * returns the matching object
	 *
	 * @see #find(String, Class)
	 * @throws IllegalStateException if not exactly 1 was found
	 * @throws NullPointerException if arguments were null
	 */
	public final <C extends E> C select(final String str) {
		return this.select(str, null);
	}

	/**
	 * returns the matching object
	 *
	 * @see #find(String, Class)
	 * @throws IllegalStateException if not exactly 1 was found
	 * @throws NullPointerException if arguments were null
	 */
	public final <T extends E> T select(final Class<T> clazz) {
		return this.select(null, clazz);
	}

	/**
	 * returns the count of matching objects
	 *
	 * @see #find(String, Class)
	 * @throws NullPointerException if arguments were null
	 */
	public final <T extends E> int count(final String str) {
		return this.impl_find(str, null).size();
	}

	/**
	 * returns the count of matching objects
	 *
	 * @see #find(String, Class)
	 * @throws NullPointerException if arguments were null
	 */
	public final <T extends E> int count(final Class<T> clazz) {
		return this.impl_find(null, clazz).size();
	}

	/**
	 * returns the count of matching objects
	 *
	 * @see #find(String, Class)
	 * @throws NullPointerException if arguments were null
	 */
	public final <T extends E> int count(final String str, final Class<T> clazz) {
		return this.impl_find(str, clazz).size();
	}

	/**
	 * checks if a query returns exactly one match
	 *
	 * @see #find(String, Class)
	 * @throws NullPointerException if arguments were null
	 */
	public final boolean selectable(final String str) {
		return this.count(str) == 1;
	}

	/**
	 * checks if a query returns exactly one match
	 *
	 * @see #find(String, Class)
	 * @throws NullPointerException if arguments were null
	 */
	public final <T extends E> boolean selectable(final Class<T> clazz) {
		return this.count(clazz) == 1;
	}

	/**
	 * checks if a query returns exactly one match
	 *
	 * @see #find(String, Class)
	 * @throws NullPointerException if arguments were null
	 */
	public final <T extends E> boolean selectable(final String str, final Class<T> clazz) {
		return this.count(str, clazz) == 1;
	}

	/**
	 * returns all matching objects
	 *
	 * @see #find(String, Class)
	 * @throws NullPointerException if arguments were null
	 */
	public final <T extends E> ImmutableSet<T> find(final String str) {
		return this.impl_find(str, null);
	}

	/**
	 * returns all matching objects
	 *
	 * @see #find(String, Class)
	 * @throws NullPointerException if arguments were null
	 */
	public final <T extends E> ImmutableSet<T> find(final Class<T> clazz) {
		return this.impl_find(null, clazz);
	}

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

	public interface QueryMatcher<E> {

		public static final QueryMatcher<Object> ALL_MATCHER = new AllQueryMatcher();

		/* used for the toString method of Naming */
		String toStringPrimaryKey(final E object);

		/* checks if the string matches a certain object */
		boolean matches(final E object, final String query);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	public static class AllQueryMatcher implements QueryMatcher<Object> {
		@Override
		public String toStringPrimaryKey(final Object object) {
			return object.toString();
		}

		@Override
		public boolean matches(final Object object, final String query) {
			return true;
		}
	}
}