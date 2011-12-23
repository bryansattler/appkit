package org.uilib.templating.components;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

import org.uilib.templating.Component;
import org.uilib.templating.Options;

public class TableUI implements ComponentUI {

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public Control initialize(final Composite parent, final List<Component> children, final Options options) {

		/* table is only child so that ColumnWeightLayout works */
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new FillLayout());

		int style = SWT.NONE;
		style |= (options.get("border", true) ? SWT.BORDER : SWT.NONE);
		style |= (options.get("virtual", true) ? SWT.VIRTUAL : SWT.NONE);
		style |= (options.get("fullselect", true) ? SWT.FULL_SELECTION : SWT.NONE);
		style |= (options.get("check", false) ? SWT.CHECK : SWT.NONE);

		Table table = new Table(comp, style);
		table.setLinesVisible((options.get("lines", true)));

		return comp;
	}
}