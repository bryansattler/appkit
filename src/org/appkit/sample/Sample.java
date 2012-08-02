package org.appkit.sample;

import com.google.common.eventbus.Subscribe;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.appkit.concurrent.SWTSyncedRunnable;
import org.appkit.concurrent.SmartExecutor;
import org.appkit.overlay.Overlay;
import org.appkit.overlay.SpinnerOverlay;
import org.appkit.preferences.PrefStore;
import org.appkit.templating.Component;
import org.appkit.templating.Templating;
import org.appkit.templating.event.ButtonEvent;
import org.appkit.templating.event.DatePickerEvent;
import org.appkit.templating.event.EventContext;
import org.appkit.templating.event.EventContexts;
import org.appkit.templating.event.RadioSetEvent;
import org.appkit.templating.widget.RadioSet;
import org.appkit.util.Texts;
import org.appkit.widget.util.ButtonUtils;
import org.appkit.widget.util.MBox;
import org.appkit.widget.util.MBox.Type;
import org.appkit.widget.util.SWTUtils;
import org.appkit.widget.util.ShellUtils;
import org.appkit.widget.util.TableUtils;
import org.appkit.widget.util.TableUtils.ScrollEvent;
import org.appkit.widget.util.TableUtils.ScrollListener;
import org.appkit.widget.util.TextUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Sample {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(Sample.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Shell shell;
	private final Component sample;
	private final SmartExecutor executor;
	private Overlay overlay;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public Sample() {
		/* SLF4J Configuration */
		System.setProperty("org.slf4j.simplelogger.defaultlog", "debug");

		/* check swt-startup */
		Texts texts = Texts.fromResources(Locale.ENGLISH);
		SWTUtils.checkStartup(texts);
		//SWTUtils.checkForBrowser(SWT.MOZILLA, texts);

		/* New Shell */
		shell = new Shell();
		shell.setLayout(new FillLayout());

		/* for saving and loading Preferences */
		PrefStore prefStore = PrefStore.createJavaPrefStore("org/appkit/sample");

		/* create a convenient Executor */
		executor = SmartExecutor.start();

		/* for catching all local events (see the methods tagged with @Subscribe) */
		EventContext eventContext = EventContexts.forSendingTo(this);

		/* create templating */
		Templating templating = Templating.fromResources();

		/* create the orderview component with the given eventContext */
		this.sample = templating.create("sample", eventContext, shell);

		/* translate component */
		Texts.translateComponent(sample, Locale.ENGLISH);

		/* gets all texts with the SWT method */
		for (final Text text : SWTUtils.findAllChildren(shell, Text.class)) {
			TextUtils.enableCopyShortcut(text);

			/* default: 0, maxDigits: 5 */
			TextUtils.configureForNumber(text, 0, 5);
		}

		Menu menu     = new Menu(this.shell);
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText("An Option");
		item = new MenuItem(menu, SWT.NONE);
		item.setText("Another Option");

		ButtonUtils.setDropDownMenu(sample.select("dropdown", Button.class), menu);

		/* select the second item in the RadioSet */
		sample.select(RadioSet.class).selectChoice("hideoverlay");

		/* selects the table */
		final Table t = sample.select("data", Table.class);
		L.debug("t {}", t.getTopIndex());
		t.setHeaderVisible(true);

		/* create columns */
		for (int i = 0; i < 3; i++) {

			TableColumn c1 = new TableColumn(t, SWT.NONE);
			c1.setText("col " + i);
		}

		/* restore and save column-sizes and order */
		TableUtils.rememberColumnSizes(t, "sample", prefStore, executor);
		TableUtils.rememberColumnOrder(t, "sample", prefStore, executor);

		/* install a ScrollDetector */
		TableUtils.installScrollListener(
			t,
			new ScrollListener() {
					@Override
					public void scrolled(final ScrollEvent event) {
						L.debug("first vis: {}", event.getFirstVisibleRow());
						L.debug("last vis: {}", event.getLastVisibleRow());
					}
				});

		this.executor.scheduleAtFixedRate(
			1,
			TimeUnit.SECONDS,
			new SWTSyncedRunnable(
				Display.getCurrent(),
				new Runnable() {

						private int i = 0;

						@Override
						public void run() {

							TableItem i1 = new TableItem(t, SWT.NONE);
							i1.setText("item " + i);
							this.i = i + 1;
						}
					}));

		shell.open();
		while (! shell.isDisposed()) {
			if (! shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}
		}

		executor.shutdownNow();
		System.exit(0);
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static void main(final String args[]) {
		new Sample();
	}

	@Subscribe
	public void localEvent(final Object object) {
		L.debug("event: " + object);
	}

	@Subscribe
	public void daterangeChange(final DatePickerEvent event) {
		L.debug("we got a date-range: {}", event.getDateRange());
	}

	@Subscribe
	public void buttonClick(final ButtonEvent event) {
		L.debug("buttonclick: {}", event.getOrigin());
		if (event.getOrigin().equals("shellattach")) {

			/* opens an attached subshell */
			Shell subShell = new Shell(shell, SWT.NONE);
			subShell.setSize(400, 150);
			subShell.open();

			ShellUtils.smartAttachment(subShell, event.getButton());
		} else if (event.getOrigin().equals("dummy")) {

			MBox mbox =
				new MBox(
					shell,
					Type.QUESTION,
					"A question",
					"What is it going to be?",
					1,
					"ab",
					"b",
					"c",
					"d",
					"ac",
					"e");
			MBox.show(shell, Type.INFO, "Answer: " + mbox.showReturningString());

		}
	}

	@Subscribe
	public void radioBtnSelected(final RadioSetEvent event) {
		L.debug("we got a radio-choice: {}", event.getSelectedChoice());
		if (event.getSelectedChoice().equals("showoverlay")) {
			if (this.overlay == null) {

				Table table = sample.select("data$table", Table.class);

				/* display a spinner */
				this.overlay = Overlay.createAnimatedOverlay(table, new SpinnerOverlay(), this.executor);
				Display.getCurrent().asyncExec(
					new Runnable() {
							@Override
							public void run() {
								overlay.show();
							}
						});
			}
		} else if (event.getSelectedChoice().equals("hideoverlay")) {
			if (this.overlay != null) {
				this.overlay.dispose();
				this.overlay = null;
			}
		}
	}
}