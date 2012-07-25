package org.appkit.templating.event;

import org.eclipse.swt.widgets.Button;

public final class ButtonEvent {

	private final Button button;
	private final String origin;

	public ButtonEvent(final Button button, final String origin) {
		this.button = button;
		this.origin = origin;
	}

	public Button getButton() {
		return this.button;
	}

	public String getOrigin() {
		return this.origin;
	}
}