package org.appkit.widget;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.Map;

import org.appkit.application.EventContext;
import org.appkit.registry.Texts.CustomTranlation;
import org.appkit.templating.Options;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** a component that uses multiple {@link Button}s to form a radio-set */
public class RadioSet extends Composite implements CustomTranlation {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(RadioSet.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Map<String, Button> choices = Maps.newHashMap();
	private String selection				  = "";

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public RadioSet(final EventContext app, final Composite parent, final Options options) {
		super(parent, options.get("border", false) ? SWT.BORDER : SWT.NONE);

		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight			 = 0;
		gl.marginWidth			 = 0;
		gl.horizontalSpacing     = 0;
		gl.verticalSpacing		 = 0;
		this.setLayout(gl);

		int i = 0;
		for (final String choice : options.get("choices")) {

			final Button btn = new Button(this, SWT.RADIO);
			this.choices.put(choice, btn);
			btn.addSelectionListener(
				new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent event) {
							if (btn.getSelection()) {
								app.postEvent(choice);
							}
						}
					});

			/* if it's the first, select it */
			if (i == 0) {
				btn.setSelection(true);
				this.selection = choice;
			}
			i++;
		}
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public String getSelection() {
		return this.selection;
	}

	@Override
	public void translate(final String i18nInfo) {
		for (final String choice : Splitter.on("/").split(i18nInfo)) {

			Iterator<String> i = Splitter.on(":").trimResults().split(choice).iterator();
			String code		   = i.next();
			String text		   = i.next();
			L.debug("i18n text for option '" + code + "' is '" + text + "'");
			this.choices.get(code).setText(text);
		}

		this.layout();
	}
}