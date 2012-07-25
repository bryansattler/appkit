package org.appkit.widget.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.List;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A more sophisticated MessageBox. Returns -1 if just disposed without clicking any button.
 */
public final class MBox {

	//~ Enumerations ---------------------------------------------------------------------------------------------------

	public enum Type {ERROR, INFO, WARNING, QUESTION;
	}

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(MBox.class);
	private static final int DISPOSEANSWER_INT				 = -1;
	private static final String DISPOSEANSWER_STR			 = "?";

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Shell shell;
	private final ImmutableList<String> options;
	private final ArrayListMultimap<Character, Button> hotKeys = ArrayListMultimap.create();
	private Button defBtn;
	private int answer										   = 0;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public MBox(final Shell parentShell, final Type type, final String title, final String message, final int def,
				final String... optionArray) {

		List<String> optionList = Arrays.asList(optionArray);
		Preconditions.checkArgument(
			Iterables.all(optionList, Predicates.and(Predicates.notNull(), Predicates.not(Predicates.equalTo("")))),
			"at least one option was null or the empty string");

		Preconditions.checkArgument(optionList.size() > 0, "empty options");
		Preconditions.checkArgument(
			(def >= 0) && (def < optionList.size()),
			"%s options but default %s specified",
			optionList.size(),
			def);

		this.options     = ImmutableList.copyOf(optionList);

		/* answer when the messagebox is just disposed = clicked away */
		this.answer		 = DISPOSEANSWER_INT;

		/* create a shell */
		this.shell		 = new Shell(parentShell, SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM | SWT.SHEET);
		this.shell.setLayout(new GridLayout(2, false));

		/* icon */
		Composite compIcon = new Composite(this.shell, SWT.NONE);
		compIcon.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));

		GridLayout gl = new GridLayout();
		gl.marginHeight     = 10;
		gl.marginWidth	    = 10;
		compIcon.setLayout(gl);

		Label label = new Label(compIcon, SWT.NONE);

		int systemImage;
		switch (type) {
			case ERROR:
				systemImage = SWT.ICON_ERROR;
				break;
			case WARNING:
				systemImage = SWT.ICON_WARNING;
				break;
			case QUESTION:
				systemImage = SWT.ICON_QUESTION;
				break;
			default:
				systemImage = SWT.ICON_INFORMATION;
		}

		Image image = this.shell.getDisplay().getSystemImage(systemImage);
		label.setImage(image);

		this.shell.setText((title != null) ? title : "");

		if (message.length() < 1000) {

			Label lSecMessage = new Label(this.shell, SWT.NONE);
			lSecMessage.setText(message);
			lSecMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
		} else {

			Text tSecMessage = new Text(this.shell, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
			tSecMessage.setText(message);
			tSecMessage.setEditable(false);
			tSecMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
			tSecMessage.getVerticalBar().setEnabled(true);
		}

		Composite compButtons = new Composite(this.shell, SWT.NONE);
		compButtons.setLayout(new GridLayout(options.size() + 1, false));
		compButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		if (options.size() != 1) {

			Label spacer = new Label(compButtons, SWT.NONE);
			spacer.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		}

		int i	   = 0;
		Button btn = null;
		for (final String option : options) {
			btn = new Button(compButtons, SWT.PUSH);
			btn.setText("&" + option);
			btn.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));
			btn.addSelectionListener(new BClicked(i));
			btn.addTraverseListener(new Traversed());
			btn.addKeyListener(new KeyPressed());

			/* put Hotkey -> Button Mapping into Map */
			hotKeys.put(Character.toLowerCase(option.charAt(0)), btn);

			if (i == def) {
				defBtn = btn;
			}

			i++;
		}
		this.shell.setDefaultButton(this.defBtn);
		this.shell.addKeyListener(new KeyPressed());

		/* Center Button if there is just one */
		if (options.size() == 1) {
			((GridData) btn.getLayoutData()).horizontalAlignment		   = SWT.CENTER;
			((GridData) btn.getLayoutData()).grabExcessHorizontalSpace     = true;
		}

		if (message.length() < 1000) {
			this.shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		} else {
			this.shell.setSize(shell.computeSize(800, 400));
		}

		/* Position in the middle of of parent shell */
		SWTUtils.moveToCenterOf(this.shell, this.shell.getParent());
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static void show(final Shell parentShell, final Type type, final String message) {
		show(parentShell, type, null, message);
	}

	public static void show(final Shell parentShell, final Type type, final String title, final String message) {

		MBox mbox = new MBox(parentShell, type, null, message, 0, "OK");
		mbox.show();
	}

	public void show() {
		this.showReturningInt();
	}

	public int showReturningInt() {
		this.shell.open();
		this.defBtn.setFocus();

		while (! this.shell.isDisposed()) {
			if (! this.shell.getDisplay().readAndDispatch()) {
				this.shell.getDisplay().sleep();
			}
		}

		return this.answer;
	}

	public String showReturningString() {

		int answer = this.showReturningInt();
		if (answer == DISPOSEANSWER_INT) {
			return DISPOSEANSWER_STR;
		} else {
			return this.options.get(answer);
		}
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	public class KeyPressed extends KeyAdapter {
		@Override
		public void keyPressed(final KeyEvent event) {

			Button selected = null;
			if (event.widget instanceof Button) {
				selected = (Button) event.widget;
			}

			Character c = Character.toLowerCase(event.character);

			if (event.character == SWT.CR) {
				if (selected != null) {
					selected.notifyListeners(SWT.Selection, null);
				} else {
					shell.getDefaultButton().notifyListeners(SWT.Selection, null);
				}
			} else if (hotKeys.containsKey(c)) {

				List<Button> matchingButtons = hotKeys.get(c);
				if (matchingButtons.size() == 1) {
					matchingButtons.get(0).notifyListeners(SWT.Selection, null);

				} else if (matchingButtons.size() > 1) {

					/* look which of the matching buttons was selected */
					int i = 0;
					for (i = 0; i < matchingButtons.size(); i++) {
						if (matchingButtons.get(i) == selected) {
							break;
						}
					}

					/* select the next */
					if ((i + 1) < matchingButtons.size()) {
						i = i + 1;
					} else {
						i = 0;
					}

					matchingButtons.get(i).setFocus();
				}
			}
		}
	}

	public class Traversed implements TraverseListener {
		@Override
		public void keyTraversed(final TraverseEvent event) {

			Character c = Character.toLowerCase(event.character);
			if (hotKeys.containsKey(c)) {
				event.doit = false;
			}
		}
	}

	private final class BClicked extends SelectionAdapter {

		private final int answer;

		public BClicked(final int answer) {
			this.answer = answer;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			MBox.this.answer = this.answer;
			shell.dispose();
		}
	}
}