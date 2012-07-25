package org.appkit.templating.event;

import org.eclipse.swt.widgets.Button;

public final class ButtonEvent {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Button button;
	private final String origin;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public ButtonEvent(final Button button, final String origin) {
		this.button     = button;
		this.origin     = origin;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public Button getButton() {
		return this.button;
	}

	public String getOrigin() {
		return this.origin;
	}
}