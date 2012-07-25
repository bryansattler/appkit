package org.appkit.templating.widget;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.Map;

import org.appkit.templating.Options;
import org.appkit.templating.event.EventContext;
import org.appkit.templating.event.RadioSetEvent;
import org.appkit.util.Texts.CustomTranlation;

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

	public RadioSet(final EventContext context, final Composite parent, final String name, final Options options) {
		super(parent, options.get("border", false) ? SWT.BORDER : SWT.NONE);

		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight			 = 0;
		gl.marginWidth			 = 0;
		gl.horizontalSpacing     = 0;
		gl.verticalSpacing		 = 0;
		this.setLayout(gl);

		int i = 0;
		for (final String choice : options.getList("choices")) {

			final Button btn = new Button(this, SWT.RADIO);
			this.choices.put(choice, btn);
			btn.addSelectionListener(
				new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent event) {
							if (btn.getSelection()) {
								selection = choice;
								context.postEvent(new RadioSetEvent(RadioSet.this, choice));
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

	public void selectChoice(String choice) {
		Preconditions.checkArgument(this.choices.containsKey(choice), "choices of this radioset %s don't contain '%s'", this.choices.keySet(), choice);

		for (Map.Entry<String,Button> btn : this.choices.entrySet()) {
			if (btn.getKey().equals(choice)) {
				btn.getValue().setSelection(true);
			} else {
				btn.getValue().setSelection(false);
			}
		}
		this.selection = choice;
	}

	public String getSelectedChoice() {
		return this.selection;
	}

	@Override
	public void translate(final String i18nInfo) {
		for (final String choice : Splitter.on("/").split(i18nInfo)) {

			Iterator<String> i = Splitter.on(":").trimResults().split(choice).iterator();
			String code		   = i.next();
			String text		   = i.next();

			Preconditions.checkArgument(this.choices.containsKey(code), "choices of this radioset %s don't contain '%s'", this.choices.keySet(), code);

			L.debug("i18n text for option '{}' is '{}'", code, text);
			this.choices.get(code).setText(text);
		}

		this.layout();
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

}