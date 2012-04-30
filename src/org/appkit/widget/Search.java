package org.appkit.widget;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;

import org.appkit.application.EventContext;
import org.appkit.osdependant.OSUtils;
import org.appkit.registry.Texts.CustomTranlation;
import org.appkit.templating.Options;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** for creating a component that aims to be a search-field */
public final class Search extends Composite implements CustomTranlation {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(Search.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Text text;
	private final Button bDelete;
	private final Label label;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public Search(final EventContext app, final Composite parent, final Options options) {
		super(parent, SWT.NONE);

		boolean delButtonNeeded								 = true;
		if (OSUtils.isMac()) {
			delButtonNeeded = false;
		}

		this.setLayout(new GridLayout((delButtonNeeded ? 3 : 2), false));
		((GridLayout) this.getLayout()).horizontalSpacing     = -1;

		this.label											  = new Label(this, SWT.NONE);
		this.label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		if (delButtonNeeded) {
			this.bDelete = new Button(this, SWT.PUSH);
			this.bDelete.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			this.bDelete.addSelectionListener(new BDeleteClicked());
		} else {
			this.bDelete = null;
		}

		this.text = new Text(this, SWT.SEARCH | SWT.CANCEL);
		this.text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.text.addFocusListener(new FocusChanged());
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public void translate(final String i18nInfo) {

		List<String> texts = Lists.newArrayList(Splitter.on("/").split(i18nInfo));
		Preconditions.checkArgument(texts.size() == 2, "need two strings, separated by /");

		label.setText(texts.get(0));
		bDelete.setText(texts.get(1));
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