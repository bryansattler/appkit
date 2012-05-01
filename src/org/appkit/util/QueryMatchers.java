package org.appkit.util;

import java.util.Set;

import org.appkit.util.Naming.ClassQueryMatcher;
import org.appkit.util.Naming.StringQueryMatcher;

/**
 * various useful query-matchers
 */
public final class QueryMatchers {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	public static ClassQueryMatcher<Object> ASSIGNABLE_CLASS = new AssignableClassMatcher();

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	public abstract static class AbstractPrefixMatcher<E> implements StringQueryMatcher<E> {
		public abstract String getName(final E object);

		@Override
		public final boolean matches(final E object, final String query) {
			if (this.getName(object).startsWith(query)) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public final Set<String> precomputeMatches(final E object) {
			return null;
		}

		@Override
		public final String toString() {
			return "matching all prefixes";
		}

		@Override
		public final String toStringPrimaryKey(final E object) {
			return this.getName(object);
		}
	}

	private static final class AssignableClassMatcher implements ClassQueryMatcher<Object> {
		@Override
		public final boolean matches(final Object object, final Class<?> queryClass) {
			if (queryClass.isAssignableFrom(object.getClass())) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public final Set<Class<?>> precomputeMatches(final Object object) {
			return null;
		}

		@Override
		public final String toString() {
			return "matching assignable classes";
		}

		@Override
		public final String toStringPrimaryKey(final Object object) {
			return object.toString();
		}
	}
}