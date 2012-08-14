package org.appkit.concurrent;

import com.google.common.collect.ImmutableList;

public final class Report {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	public final Enum<?> type;
	public final ImmutableList<Object> data;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	protected Report(final Enum<?> type, final ImmutableList<Object> data) {
		this.type     = type;
		this.data     = data;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public String toString() {
		return "{" + this.type + ": " + this.data + "}";
	}
}