package org.appkit.sample;

import com.google.common.eventbus.Subscribe;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.PropertyConfigurator;

import org.appkit.event.LocalEventContext;
import org.appkit.overlay.Overlay;
import org.appkit.overlay.SpinnerOverlay;
import org.appkit.preferences.PrefStore;
import org.appkit.templating.Component;
import org.appkit.templating.Templating;
import org.appkit.util.SWTSyncedRunnable;
import org.appkit.util.SmartExecutor;
import org.appkit.util.Texts;
import org.appkit.widget.Datepicker.DateRange;
import org.appkit.widget.util.TableScrollDetector.ScrollEvent;
import org.appkit.widget.util.TableScrollDetector.ScrollListener;
import org.appkit.widget.util.TableUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Sample {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(Sample.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Shell shell;
	private final Composite compOrders;
	private final Component orderview;
	private final SmartExecutor executor;
	private Overlay overlay;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public Sample() {
		/* Log4J Configuration */
		PropertyConfigurator.configure(log4jProperties());

		/* New Shell */
		shell					  = new Shell();
		shell.setLayout(new FillLayout());

		/* for saving and loading Preferences */
		PrefStore prefStore = PrefStore.createJavaPrefStore("org/appkit/sample");

		/* create a convenient Executor */
		executor = SmartExecutor.start();

		/* for catching all local events (see the methods tagged with @Subscribe) */
		LocalEventContext eventContext = new LocalEventContext(this);

		/* create templating */
		Templating templating = Templating.fromResources();

		/* create the orderview component with the given eventContext */
		this.orderview	    = templating.create("orderview", eventContext, shell);
		this.compOrders     = orderview.getComposite();

		/* translate component */
		Texts.translateComponent(orderview);

		final Button b = orderview.select("mark-ordered", Button.class);
		b.addSelectionListener(
			new SelectionListener() {
					@Override
					public void widgetSelected(final SelectionEvent arg0) {

						/*Shell subShell = new Shell(shell, SWT.NONE);
						   subShell.setSize(100,50);
						   subShell.open();
						   ShellUtils.smartAttachment(subShell, b);*/
					}

					@Override
					public void widgetDefaultSelected(final SelectionEvent arg0) {

						// TODO Auto-generated method stub
					}
				});

		/* selects the table */
		final Table t = orderview.select("orders", Table.class);
		t.setHeaderVisible(true);

		/* create columns */
		for (int i = 0; i <= 6; i++) {

			TableColumn c1 = new TableColumn(t, SWT.NONE);
			c1.setText("col " + i);
		}

		/* add items */
		for (int i = 0; i <= 15; i++) {

			TableItem i1 = new TableItem(t, SWT.NONE);
			i1.setText("item " + i);
		}

		/* divide table equally among columns */
		TableUtils.fillTableWidth(t);

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
			2,
			TimeUnit.SECONDS,
			new SWTSyncedRunnable(
				Display.getCurrent(),
				new Runnable() {
						@Override
						public void run() {

							TableItem i1 = new TableItem(t, SWT.NONE);
							i1.setText("item X");
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

		//MBox mbox = new MBox(shell, Type.INFO, "asdf", "xxxxx", 1, "ab", "b", "c", "d", "ac", "e");
		//L.debug("mbox {}", mbox.showReturningString());

		//final Table t = orderview.select("orders$table", Table.class);

		/* display a spinner: unfinished */
		this.overlay = Overlay.createAnimatedOverlay(this.compOrders, new SpinnerOverlay(), this.executor);
		Display.getCurrent().asyncExec(
			new Runnable() {
					@Override
					public void run() {
						overlay.show();
					}
				});
	}

	@Subscribe
	public void daterangeChange(final DateRange daterange) {
		L.debug("we got a date-range: " + daterange);
	}

	public static Properties log4jProperties() {

		Properties props = new Properties();

		props.setProperty("log4j.rootLogger", "DEBUG,console");
		props.setProperty("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
		props.setProperty("log4j.appender.console.Threshold", "DEBUG");
		props.setProperty("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
		props.setProperty("log4j.appender.console.layout.ConversionPattern", "%d [%t] %-5p %c - %m%n");

		return props;
	}
}