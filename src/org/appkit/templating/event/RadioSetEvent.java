package org.appkit.templating.event;

import org.appkit.templating.widget.RadioSet;

public final class RadioSetEvent {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final RadioSet radioSet;
	private final String selectedChoice;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public RadioSetEvent(final RadioSet radioSet, final String selectedChoice) {
		this.radioSet		    = radioSet;
		this.selectedChoice     = selectedChoice;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public final RadioSet getOrigin() {
		return this.radioSet;
	}

	public final String getSelectedChoice() {
		return this.selectedChoice;
	}
}