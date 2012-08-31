package org.appkit.widget.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.appkit.osdependant.OSUtils;
import org.appkit.util.Texts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utilities for working with SWT.
 *
 */
public final class SWTUtils {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(SWTUtils.class);

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static void checkStartup(final Texts texts) {
		try {
			Display.getDefault();

		} catch (final LinkageError e) {
			L.error(e.getMessage(), e);

			String error	  = texts.get("error_swt");
			String systemInfo = "\n\nSystem: ";
			String jvmInfo    = "\nJVM: ";
			if (OSUtils.isMac()) {
				systemInfo = systemInfo + " Mac";
			} else if (OSUtils.isWindows()) {
				systemInfo = systemInfo + " Windows";
			} else if (OSUtils.isLinux()) {
				systemInfo = systemInfo + " Linux";
			} else {
				systemInfo = systemInfo + " <unknown>";
			}

			String bitage = System.getProperty("sun.arch.data.model");
			switch (bitage) {
				case "32":
					jvmInfo = jvmInfo + "32 bit";
					break;
				case "64":
					jvmInfo = jvmInfo + "64 bit";
					break;
				default:
					jvmInfo = jvmInfo + "<unknown>";
			}
			JOptionPane.showMessageDialog(null, error + systemInfo + jvmInfo);
			System.exit(-1);
		}
	}

	public static void checkForBrowser(final int browserStyle, final Texts texts) {
		checkStartup(texts);
		try {

			Shell shell = new Shell();
			new Browser(shell, browserStyle);
			shell.dispose();

		} catch (final SWTError e) {
			if ((browserStyle & SWT.WEBKIT) != 0) {
				MBox.show(new Shell(), MBox.Type.ERROR, texts.get("error_nowebkit"));
				Program.launch(texts.get("error_nowebkit_url"));
			} else if ((browserStyle & SWT.MOZILLA) != 0) {
				MBox.show(new Shell(), MBox.Type.ERROR, texts.get("error_nomozilla"));
				Program.launch(texts.get("error_nomozilla_url"));
			}
			System.exit(-1);
		}
	}

	/** adjusts a rectangle, so that it stays on the client-ares of a given monitor */
	public static Point moveOntoMonitor(final Rectangle controlBounds, final Monitor monitor) {

		Rectangle monitorBounds = monitor.getClientArea();

		int x = Math.max(0, controlBounds.x);
		if (controlBounds.width > monitorBounds.width) {
			x = 0;
		} else {

			int diff = (controlBounds.x + controlBounds.width) - monitorBounds.width;
			if (diff > 0) {
				x = controlBounds.x - diff;
			}
		}

		int y = Math.max(0, controlBounds.y);
		if (controlBounds.height > monitorBounds.height) {
			y = 0;
		} else {

			int diff = (controlBounds.y + controlBounds.height) - monitorBounds.height;
			if (diff > 0) {
				y = controlBounds.y - diff;
			}
		}

		return new Point(x, y);
	}

	/** Returns the Point a control need to be located at to be in the center of the primary monitor (and an offset up or down).
	 *  The point is also adjusted that the composite completely stays on the primary monitor.
	 */
	public static Point getCenterPosition(final Control control, final int xOffset, final int yOffset) {

		/* Position in the middle of screen (and a little up) */
		Rectangle monitorBounds = control.getDisplay().getPrimaryMonitor().getClientArea();
		Rectangle controlBounds = control.getBounds();
		int x				    = monitorBounds.x + ((monitorBounds.width - controlBounds.width) / 2) + xOffset;
		int y				    = monitorBounds.y + ((monitorBounds.height - controlBounds.height) / 2) + yOffset;

		Rectangle bounds = new Rectangle(x, y, controlBounds.width, controlBounds.height);

		return moveOntoMonitor(bounds, control.getDisplay().getPrimaryMonitor());
	}

	/** Returns the Point a control need to be located at to be in the center of the reference control (and 150px up).
	 *  The point is also adjusted that the composite completely stays on the monitor the reference control is on.
	 */
	public static Point getRelativeCenterPosition(final Control control, final Control referenceControl,
												  final int xOffset, final int yOffset) {

		Rectangle referenceControlBounds = referenceControl.getBounds();
		Rectangle controlBounds			 = control.getBounds();
		int x							 =
			referenceControlBounds.x + ((referenceControlBounds.width - controlBounds.width) / 2) + xOffset;
		int y = referenceControlBounds.y + ((referenceControlBounds.height - controlBounds.height) / 2) + yOffset;

		Rectangle bounds = new Rectangle(x, y, controlBounds.width, controlBounds.height);

		return moveOntoMonitor(bounds, referenceControl.getMonitor());
	}

	@SuppressWarnings("unchecked")
	public static <E extends Control> ImmutableList<E> findAllChildren(final Composite parent, final Class<E> clazz) {

		List<E> results		   = Lists.newArrayList();
		List<Control> workList = Lists.newArrayList();
		workList.add(parent);
		while (! workList.isEmpty()) {

			Control c = workList.remove(0);

			if ((c instanceof Composite) && !(c instanceof Table)) {
				workList.addAll(Arrays.asList(((Composite) c).getChildren()));
			}

			if (clazz.isAssignableFrom(c.getClass())) {
				results.add((E) c);
			}
		}

		return ImmutableList.copyOf(results);
	}

	public static void fixCocoaAlignments(final Composite parent) {
		if (!OSUtils.isMac()) {
			return;
		}

		for (final ProgressBar bar : SWTUtils.findAllChildren(parent, ProgressBar.class)) {

			Rectangle bounds = bar.getBounds();
			if ((bounds.x + 5 + bounds.width) == bar.getParent().getClientArea().width) {
				bar.setBounds(bounds.x, bounds.y, bounds.width - 5, bounds.height);
			}
		}
		for (final Button btn : SWTUtils.findAllChildren(parent, Button.class)) {

			Point location = btn.getLocation();
			if (location.x == 5) {
				btn.setLocation(0, location.y);
			}
		}
		for (final Label label : SWTUtils.findAllChildren(parent, Label.class)) {

			Point location = label.getLocation();
			if (location.x == 5) {
				label.setLocation(4, location.y);
			}
		}
		for (final Table table : SWTUtils.findAllChildren(parent, Table.class)) {

			Point location = table.getLocation();
			if (location.x == 5) {
				table.setLocation(7, location.y);
			}

			Rectangle bounds = table.getBounds();
			if ((((bounds.x + 5) - 2) + bounds.width) == table.getParent().getClientArea().width) {
				table.setBounds(bounds.x, bounds.y, bounds.width - 7, bounds.height);
			}
		}
	}
}