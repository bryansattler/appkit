package org.uilib.registry;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** <b>SWT Color cache/registry</b>
 * <br />
 * <br />
 * Creates, assigns and caches {@link Color}s. Colors can be set to the foreground or background of a {@link Control}.
 * Use of the color is deregistered when the control is disposed or manually via the <code>putBack</code> methods.
 * <br />
 * <br />
 * This uses a simple counter to keep of track of usage of certain Colors. If the usage drops to 0, the color
 * is disposed.
 * <br />
 * <br />
 * <b>TODO:</b> "ColorSetable" interface to set Color on arbitrary things and more controls<br />
 * <b>TODO:</b> Direct creation of colors?
 */
public final class Colors {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(Colors.class);

	/* cache / registry */
	private static final BiMap<Integer, Color> colorCache				  = HashBiMap.create();
	private static final Multiset<Color> usage							  = HashMultiset.create();
	private static final Map<Control, DisposeListener> fgDisposeListeners = Maps.newHashMap();
	private static final Map<Control, DisposeListener> bgDisposeListeners = Maps.newHashMap();

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private Colors() {}

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * sets the foreground color of the given control to an RGB-value
	 *
	 * <b>TODO</b>This works only for {@link Text} at the moment
	 * @param control control on which color should be set
	 * @param r red value
	 * @param g green value
	 * @param b blue value
	 */
	public static void setForeground(final Control control, final int r, final int g, final int b) {
		setColor(control, r, g, b, true);
	}

	/**
	 * sets the background color of the given control to an RGB-value
	 *
	 * <b>TODO</b>This works only for {@link Text} at the moment
	 * @param control control on which color should be set
	 * @param r red value
	 * @param g green value
	 * @param b blue value
	 * @throws IllegalStateException if called from a non-Display thread
	 * @throws IllegalArgumentException if image couldn't be set
	 */
	public static void setBackground(final Control control, final int r, final int g, final int b) {
		setColor(control, r, g, b, false);
	}

	private static void setColor(final Control control, final int r, final int g, final int b, final boolean foreground) {
		/* check for UI-thread and if control is colorable */
		Preconditions.checkState(
			Display.getCurrent() != null,
			"Colors is to be used from the display-thread exclusively!");
		Preconditions.checkArgument(control instanceof Text, "don't know how to set color on {}", control);

		L.debug(
			"setting " + (foreground ? "fore" : "back") + "ground-color {} for {}",
			Joiner.on(".").join(r, g, b),
			control);

		/* if we already set an color on this control, remove it */
		if (foreground) {
			if (fgDisposeListeners.containsKey(control)) {
				putBackForeground(control);
			}
		} else {
			if (bgDisposeListeners.containsKey(control)) {
				putBackBackground(control);
			}
		}

		/* search for color in cache or create it */
		int hash		  = Objects.hashCode(r, g, b);
		final Color color;
		if (colorCache.containsKey(hash)) {
			color		  = colorCache.get(hash);
		} else {
			color = new Color(Display.getCurrent(), r, g, b);
			L.debug("created color: {}", color);
			colorCache.put(hash, color);
		}

		/* register usage */
		usage.setCount(color, usage.count(color) + 1);
		L.debug("usage of {} now {}", color, usage.count(color));

		/* set the color and add the disposer */
		if (control instanceof Text) {
			if (foreground) {
				control.setForeground(color);

				DisposeListener listener = new ColorDisposeListener(true);
				fgDisposeListeners.put(control, listener);
				control.addDisposeListener(listener);
			} else {
				control.setBackground(color);

				DisposeListener listener = new ColorDisposeListener(false);
				bgDisposeListeners.put(control, listener);
				control.addDisposeListener(listener);
			}
		} else {
			throw new IllegalStateException();
		}
	}

	/**
	 * deregister use of foreground-color of control
	 *
	 * @throws IllegalStateException if control isn't registered
	 */
	public static void putBackForeground(final Control control) {
		putBack(control, true);
	}

	/**
	 * deregisters use of background-color of control
	 *
	 * @throws IllegalStateException if control isn't registered
	 */
	public static void putBackBackground(final Control control) {
		putBack(control, false);
	}

	private static void putBack(final Control control, final boolean foreground) {
		if (foreground) {
			Preconditions.checkState(fgDisposeListeners.containsKey(control), "control {} not registered", control);
		} else {
			Preconditions.checkState(bgDisposeListeners.containsKey(control), "control {} not registered", control);
		}

		/* remove control out of registry and remove listener */
		if (foreground) {
			control.removeDisposeListener(fgDisposeListeners.remove(control));
		} else {
			control.removeDisposeListener(bgDisposeListeners.remove(control));
		}

		/* get the color */
		Color color;
		if (control instanceof Text) {
			if (foreground) {
				color = ((Text) control).getForeground();
			} else {
				color = ((Text) control).getBackground();
			}
		} else {
			throw new IllegalStateException();
		}

		/* decrease usage-counter */
		usage.setCount(color, usage.count(color) - 1);
		L.debug("usage of {} now {}", color, usage.count(color));

		/* if usage is 0 dispose it */
		if (! usage.contains(color)) {
			L.debug("disposing {}", color);
			colorCache.inverse().remove(color);
			color.dispose();
		}
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private static final class ColorDisposeListener implements DisposeListener {

		private final boolean foreground;

		public ColorDisposeListener(final boolean foreground) {
			this.foreground = foreground;
		}

		@Override
		public void widgetDisposed(final DisposeEvent event) {
			if (foreground) {
				putBackForeground((Control) event.widget);
			} else {
				putBackBackground((Control) event.widget);
			}
		}
	}
}