package org.appkit.widget.util;

import com.google.common.base.CharMatcher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utilities for working with {@link Text}s
 *
 */
public final class TextUtils {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L = LoggerFactory.getLogger(TextUtils.class);

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static void enableCopyShortcut(final Text text) {
		text.addKeyListener(
			new KeyAdapter() {
					@Override
					public void keyPressed(final KeyEvent event) {
						if ((event.stateMask == SWT.CTRL) && (event.keyCode == 'a')) {
							if (event.widget instanceof Text) {
								((Text) event.widget).selectAll();
							}
						}
					}
				});
	}

	public static void enableSelectAllOnFocus(final Text text) {
		text.addFocusListener(
			new FocusListener() {
					@Override
					public void focusLost(final FocusEvent event) {}

					@Override
					public void focusGained(final FocusEvent event) {
						if (event.widget instanceof Text) {
							((Text) event.widget).selectAll();
						}
					}
				});
	}

	public static void enableInputFilter(final Text text, final CharMatcher matcher) {
		text.addVerifyListener(
			new VerifyListener() {
					@Override
					public void verifyText(final VerifyEvent event) {
						if (! CharMatcher.INVISIBLE.matches(event.character) && ! matcher.matches(event.character)) {
							event.doit = false;
						}
					}
				});
	}

	public static void configureForNumber(final Text text, final long def, final int positions) {
		if (text.getText().isEmpty()) {
			text.setText(Long.toString(def));
		}
		enableInputFilter(text, CharMatcher.JAVA_DIGIT);
		text.setTextLimit(positions);
		text.addModifyListener(
			new ModifyListener() {
					@Override
					public void modifyText(final ModifyEvent event) {
						if (text.getText().isEmpty()) {
							text.setText(Long.toString(def));
							text.selectAll();
						}
					}
				});

		text.addFocusListener(
			new FocusListener() {
					@Override
					public void focusLost(final FocusEvent event) {

						long l = Long.parseLong(text.getText());
						text.setText(Long.toString(l));
					}

					@Override
					public void focusGained(final FocusEvent event) {}
				});
	}
}