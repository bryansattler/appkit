package org.appkit.measure;


/**
 * Data of a single measurement. Contains a name, a start-time, a duration and
 * optionally associated data.
 *
 */
public final class MeasureData {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final String name;
	private final Object data;
	private final long start;
	private final long duration;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public MeasureData(final String name, final Object data, final long start) {
		this(name, data, start, 0);
	}

	public MeasureData(final String name, final Object data, final long start, final long duration) {
		this.name		  = name;
		this.data		  = data;
		this.start		  = start;
		this.duration     = duration;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	public long getStart() {
		return start;
	}

	public long getDuration() {
		return duration;
	}

	public Object getData() {
		return data;
	}
}