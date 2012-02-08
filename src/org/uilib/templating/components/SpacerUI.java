package org.uilib.templating.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.uilib.application.EventContext;
import org.uilib.templating.Options;

/** no-op component for use as a spacer in GridLayouts*/
public class SpacerUI implements ComponentUI {

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public Control initialize(final EventContext app, final Composite parent, final String name, final String type,
							  final Options options) {
		return new Label(parent, SWT.NONE);
	}
}