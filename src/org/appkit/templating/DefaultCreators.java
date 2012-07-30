package org.appkit.templating;

import org.appkit.registry.Fonts;
import org.appkit.templating.event.ButtonEvent;
import org.appkit.templating.event.EventContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

final class DefaultCreators {

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	static final class SpacerCreator implements ControlCreator<Label> {
		@Override
		public Label initialize(final EventContext context, final Composite parent, final String name,
								final Options options) {
			return new Label(parent, SWT.NONE);
		}
	}

	static final class ButtonCreator implements ControlCreator<Button> {
		@Override
		public Button initialize(final EventContext context, final Composite parent, final String name,
								 final Options options) {

			final Button b = new Button(parent, SWT.PUSH);
			if (name != null) {
				b.addSelectionListener(
					new SelectionAdapter() {
							@Override
							public void widgetSelected(final SelectionEvent event) {
								context.postEvent(new ButtonEvent(b, name));
							}
						});
			}

			return b;
		}
	}

	static final class LabelCreator implements ControlCreator<Label> {
		@Override
		public Label initialize(final EventContext context, final Composite parent, final String name,
								final Options options) {

			Label label = new Label(parent, (options.get("border", false) ? SWT.BORDER : SWT.NONE));
			label.setText("< empty >");

			Fonts.set(label, options.get("font", ""));

			return label;
		}
	}

	static final class TextCreator implements ControlCreator<Text> {
		@Override
		public Text initialize(final EventContext context, final Composite parent, final String name,
							   final Options options) {

			int style = SWT.NONE;
			style |= (options.get("border", true) ? SWT.BORDER : SWT.NONE);
			style |= (options.get("search", false) ? SWT.SEARCH : SWT.NONE);
			style |= (options.get("search", false) ? SWT.CANCEL : SWT.NONE);
			style |= (options.get("password", false) ? SWT.PASSWORD : SWT.NONE);

			return new Text(parent, style);
		}
	}

	static final class TableCreator implements ControlCreator<Table> {
		@Override
		public Table initialize(final EventContext context, final Composite parent, final String name,
								final Options options) {

			int style = SWT.NONE;
			style |= (options.get("border", true) ? SWT.BORDER : SWT.NONE);
			style |= (options.get("virtual", true) ? SWT.VIRTUAL : SWT.NONE);
			style |= (options.get("fullselect", true) ? SWT.FULL_SELECTION : SWT.NONE);
			style |= (options.get("check", false) ? SWT.CHECK : SWT.NONE);
			style |= (options.get("multi", false) ? SWT.MULTI : SWT.NONE);

			Table table = new Table(parent, style);
			table.setLinesVisible((options.get("lines", true)));

			return table;
		}
	}
}