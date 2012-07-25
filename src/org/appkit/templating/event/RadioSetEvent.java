package org.appkit.templating.event;

import org.appkit.templating.widget.RadioSet;

public final class RadioSetEvent {

	private final RadioSet radioSet;
	private final String selectedChoice;

	public RadioSetEvent(final RadioSet radioSet, final String selectedChoice) {
		this.radioSet		    = radioSet;
		this.selectedChoice     = selectedChoice;
	}

	public final RadioSet getOrigin() {
		return this.radioSet;
	}

	public final String getSelectedChoice() {
		return this.selectedChoice;
	}
}