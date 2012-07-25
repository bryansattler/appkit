package org.appkit.templating.event;

import org.appkit.templating.widget.DatePicker;
import org.appkit.templating.widget.DatePicker.DateRange;

public final class DatePickerEvent {

	private final DatePicker datePicker;
	private final DateRange range;

	public DatePickerEvent(final DatePicker datePicker, final DateRange range) {
		this.datePicker     = datePicker;
		this.range		    = range;
	}

	public final DatePicker getOrigin() {
		return this.datePicker;
	}

	public final DateRange getDateRange() {
		return this.range;
	}
}