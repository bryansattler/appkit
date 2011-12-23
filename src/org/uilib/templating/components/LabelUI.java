package org.uilib.templating.components;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.uilib.templating.Component;
import org.uilib.templating.Options;
import org.uilib.util.Fonts;

public class LabelUI implements ComponentUI {

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public Control initialize(final Composite parent, final List<Component> children, final Options options) {

		Label label			  = new Label(parent, SWT.NONE);

		List<String> fontInfo = options.get("font");
		if (! fontInfo.isEmpty()) {
			if (fontInfo.contains("bold")) {
				// FIXME: this need disposal?
				label.setFont((new Fonts()).create(Fonts.Style.BOLD, 0));
			}
		}

		return label;
	}
}