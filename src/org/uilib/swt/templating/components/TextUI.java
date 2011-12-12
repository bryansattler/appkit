package org.uilib.swt.templating.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.uilib.swt.templating.Options;

public class TextUI implements UIController {

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public Control initialize(final Composite parent, final Options options) {

		int style = SWT.NONE;
		if (options.get("border", false)) {
			style = SWT.BORDER;
		}

		return new Text(parent, style);
	}
}