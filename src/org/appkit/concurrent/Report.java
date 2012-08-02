package org.appkit.concurrent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public final class Report<E extends Enum<E>> implements Delayed {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	public final E type;
	public final ImmutableList<Object> data;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	protected Report(final E type, final ImmutableList<Object> data) {
		this.type     = type;
		this.data     = data;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public int compareTo(final Delayed o) {
		return Ordering.arbitrary().compare(this, o);
	}

	@Override
	public long getDelay(final TimeUnit unit) {
		return 0;
	}

	@Override
	public String toString() {
		return "{" + this.type + ": " + this.data + "}";
	}
}