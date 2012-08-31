package org.appkit.templating.widget;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;

import org.appkit.osdependant.OSUtils;
import org.appkit.templating.Options;
import org.appkit.templating.event.EventContext;
import org.appkit.util.Texts.Translateable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** for creating a component that aims to be a search-field */
public final class Search extends Composite implements Translateable {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(Search.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Text text;
	private final Button bDelete;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public Search(final EventContext context, final Composite parent, final String name, final Options options) {
		super(parent, SWT.NONE);

		boolean delButtonNeeded								 = true;
		if (OSUtils.isMac()) {
			delButtonNeeded = false;
		}

		this.setLayout(new GridLayout((delButtonNeeded ? 2 : 1), false));
		((GridLayout) this.getLayout()).horizontalSpacing = -1;

		if (delButtonNeeded) {
			this.bDelete = new Button(this, SWT.PUSH);
			this.bDelete.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			this.bDelete.addSelectionListener(new BDeleteClicked());
		} else {
			this.bDelete = null;
		}

		this.text = new Text(this, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
		this.text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.text.addFocusListener(new FocusChanged());
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public void translate(final String i18nInfo) {

		List<String> texts = Lists.newArrayList(Splitter.on("/").split(i18nInfo));
		Preconditions.checkArgument(texts.size() == 2, "need two strings, separated by /");

		text.setMessage(texts.get(0));
		if (bDelete != null) {
			bDelete.setText(texts.get(1));
		}
	}

	public Text getTextWidget() {
		return this.text;
	}

	public void clear() {
		this.text.setText("");
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private final class FocusChanged implements FocusListener {
		@Override
		public void focusGained(final FocusEvent e) {
			text.selectAll();
		}

		@Override
		public void focusLost(final FocusEvent e) {}
	}

	private class BDeleteClicked extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent event) {
			clear();
			text.setFocus();
		}
	}
}