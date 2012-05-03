package org.appkit.templating;

import java.util.List;

import org.appkit.event.EventContext;
import org.appkit.registry.Fonts;
import org.appkit.widget.Options;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class DefaultCreators {

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	static class SpacerCreator implements ControlCreator<Label> {
		@Override
		public Label initialize(final EventContext context, final Composite parent, final Options options) {
			return new Label(parent, SWT.NONE);
		}
	}

	static class ButtonCreator implements ControlCreator<Button> {
		@Override
		public Button initialize(final EventContext context, final Composite parent, final Options options) {
			return new Button(parent, SWT.PUSH);
		}
	}

	static class LabelCreator implements ControlCreator<Label> {
		@Override
		public Label initialize(final EventContext context, final Composite parent, final Options options) {

			Label label = new Label(parent, SWT.NONE);
			label.setText("< empty >");

			List<String> fontInfo = options.getList("font");
			if (! fontInfo.isEmpty()) {
				if (fontInfo.contains("bold")) {
					Fonts.set(label, Fonts.BOLD);
				}
			}

			return label;
		}
	}

	static class TextCreator implements ControlCreator<Text> {
		@Override
		public Text initialize(final EventContext context, final Composite parent, final Options options) {

			int style = SWT.NONE;
			style |= (options.get("border", true) ? SWT.BORDER : SWT.NONE);
			style |= (options.get("search", false) ? SWT.SEARCH : SWT.NONE);
			style |= (options.get("search", false) ? SWT.CANCEL : SWT.NONE);
			style |= (options.get("password", false) ? SWT.PASSWORD : SWT.NONE);

			return new Text(parent, style);
		}
	}

	static class TableCreator implements ControlCreator<Table> {
		@Override
		public Table initialize(final EventContext context, final Composite parent, final Options options) {

			int style = SWT.NONE;
			style |= (options.get("border", true) ? SWT.BORDER : SWT.NONE);
			style |= (options.get("virtual", true) ? SWT.VIRTUAL : SWT.NONE);
			style |= (options.get("fullselect", true) ? SWT.FULL_SELECTION : SWT.NONE);
			style |= (options.get("check", false) ? SWT.CHECK : SWT.NONE);

			Table table = new Table(parent, style);
			table.setLinesVisible((options.get("lines", true)));

			return table;
		}
	}
}