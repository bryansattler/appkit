package org.appkit.registry;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import java.util.Map;
import java.util.Map.Entry;

import org.appkit.osdependant.OSUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** <b>SWT Font cache/registry</b>
 * <br />
 * Creates, assigns and caches {@link Font}s. Fonts can be set on a {@link Control}.
 * Use of the font is deregistered when the widget is disposed, when a different Font is set via this registry
 * or manually via the {{@link #putBack(Control)}-method.
 * <br />
 * This uses a simple counter to keep of track of usage. If it drops to 0, the font
 * is disposed.
 */
public final class Fonts {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L	    = LoggerFactory.getLogger(Fonts.class);
	public static final Style BOLD	    = new Bold();
	public static final Style HUGEBOLD  = new HugeBold();
	public static final Style MONOSPACE = new Monospace();

	/* default font options */
	private static final String defaultFontName;
	private static final int defaultFontStyle;
	private static final int defaultFontHeight;

	/* cache / registry */
	private static final BiMap<Integer, Font> fontCache = HashBiMap.create();
	private static final Multiset<Font> usage		    = HashMultiset.create();

	/* currently installed disposeListeners */
	private static final Map<Widget, DisposeListener> disposeListeners = Maps.newHashMap();

	/* setters for images */
	private static final Map<Class<?extends Widget>, FontInterface> setters = Maps.newHashMap();

	static {
		Preconditions.checkArgument(Display.getCurrent() != null, "can't instantiate Fonts on a non-display thread");
		defaultFontName		  = Display.getCurrent().getSystemFont().getFontData()[0].getName();
		defaultFontStyle	  = Display.getCurrent().getSystemFont().getFontData()[0].getStyle();
		defaultFontHeight     = Display.getCurrent().getSystemFont().getFontData()[0].getHeight();

		addFontSetter(Control.class, new ControlFontInterface());
		addFontSetter(TableItem.class, new TableItemFontInterface());
		addFontSetter(TreeItem.class, new TreeItemFontInterface());
	}

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private Fonts() {}

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * Tells Fonts how to set a Font on a certain widget.
	 */
	public static <E extends Widget> void addFontSetter(final Class<E> clazz, final FontInterface setter) {
		setters.put(clazz, setter);
	}

	private static FontInterface getSetter(final Widget widget) {
		for (final Entry<Class<?extends Widget>, FontInterface> entry : setters.entrySet()) {
			if (entry.getKey().isAssignableFrom(widget.getClass())) {
				return entry.getValue();
			}
		}

		return null;
	}

	/**
	 * sets a Font, described by a fontStyle on a widget
	 *
	 * @throws IllegalStateException if called from a non-Display thread
	 */
	public static void set(final Widget widget, final Style fontStyle) {
		Preconditions.checkState(
			Display.getCurrent() != null,
			"Fonts is to be used from the display-thread exclusively!");
		Preconditions.checkArgument(
			getSetter(widget) != null,
			"don't know how to set font on {}, add a FontInterface first",
			widget);

		/* if we already set a font on this widget, remove it */
		if (disposeListeners.containsKey(widget)) {
			putBack(widget);
		}

		/* load / create font */
		String name = fontStyle.getName(defaultFontName);
		int height  = fontStyle.getHeight(defaultFontHeight);
		int style   = defaultFontStyle;
		if (fontStyle.bold()) {
			style = style | SWT.BOLD;
		}
		if (fontStyle.italic()) {
			style = style | SWT.ITALIC;
		}

		L.debug("setting font {} on {}", Joiner.on("-").join(name, height, fontStyle), widget);

		/* set font */
		int hash	    = Objects.hashCode(height, style, name);
		final Font font;
		if (fontCache.containsKey(hash)) {
			font	    = fontCache.get(hash);
		} else {
			font = new Font(Display.getCurrent(), name, height, style);
			L.debug("created font: {}", font);
			fontCache.put(hash, font);
		}

		/* increase usage-counter */
		usage.setCount(font, usage.count(font) + 1);
		L.debug("usage of {} now {}", font, usage.count(font));

		/* set font and add the disposer */
		getSetter(widget).setFont(widget, font);

		DisposeListener listener = new FontDisposeListener();
		disposeListeners.put(widget, listener);
		widget.addDisposeListener(listener);
	}

	/**
	 * deregisters use of the font of a widget
	 *
	 * @throws IllegalStateException if widget isn't registered
	 */
	public static void putBack(final Widget widget) {
		Preconditions.checkState(disposeListeners.containsKey(widget), "widget {} not registered", widget);

		/* remove widget out of registry and remove listener */
		widget.removeDisposeListener(disposeListeners.remove(widget));

		/* get the font */
		Font font = getSetter(widget).getFont(widget);

		/* decrease usage-counter */
		usage.setCount(font, usage.count(font) - 1);
		L.debug("usage of {} now {}", font, usage.count(font));

		/* if usage is 0 dispose it */
		if (! usage.contains(font)) {
			L.debug("disposing {}", font);
			fontCache.inverse().remove(font);
			font.dispose();
		}
	}

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

	/**
	 * styles for usage with the fonts-registry
	 */
	public interface Style {
		int getHeight(final int defaultHeight);

		String getName(final String defaultName);

		boolean bold();

		boolean italic();
	}

	/**
	 * Implement this to enable the use of Fonts for a custom widget.
	 *
	 */
	public static interface FontInterface {
		void setFont(final Widget widget, final Font font);

		Font getFont(final Widget widget);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private static final class FontDisposeListener implements DisposeListener {
		@Override
		public void widgetDisposed(final DisposeEvent event) {
			putBack(event.widget);
		}
	}

	/**
	 * default font, but bold
	 */
	public static class Bold implements Style {
		@Override
		public int getHeight(final int defaultHeight) {
			return defaultHeight;
		}

		@Override
		public String getName(final String defaultName) {
			return defaultName;
		}

		@Override
		public boolean bold() {
			return true;
		}

		@Override
		public boolean italic() {
			return false;
		}
	}

	/**
	 * default font, but bold and height increased by 4
	 */
	public static class HugeBold implements Style {
		@Override
		public int getHeight(final int defaultHeight) {
			return defaultHeight + 4;
		}

		@Override
		public String getName(final String defaultName) {
			return defaultName;
		}

		@Override
		public boolean bold() {
			return true;
		}

		@Override
		public boolean italic() {
			return false;
		}
	}

	/**
	 * monospace font, returns "Lucida Console" on Windows and "Monaco" on Mac.
	 */
	public static class Monospace implements Style {
		@Override
		public int getHeight(final int defaultHeight) {
			return defaultHeight;
		}

		@Override
		public String getName(final String defaultName) {
			if (OSUtils.isWindows()) {
				return "Lucida Console";
			} else if (OSUtils.isMac()) {
				return "Monaco";
			} else {
				return defaultName;
			}
		}

		@Override
		public boolean bold() {
			return false;
		}

		@Override
		public boolean italic() {
			return false;
		}
	}

	private static final class ControlFontInterface implements FontInterface {
		@Override
		public void setFont(final Widget o, final Font font) {
			((Control) o).setFont(font);
		}

		@Override
		public Font getFont(final Widget o) {
			return ((Control) o).getFont();
		}
	}

	private static final class TableItemFontInterface implements FontInterface {
		@Override
		public void setFont(final Widget o, final Font font) {
			((TableItem) o).setFont(font);
		}

		@Override
		public Font getFont(final Widget o) {
			return ((TableItem) o).getFont();
		}
	}

	private static final class TreeItemFontInterface implements FontInterface {
		@Override
		public void setFont(final Widget o, final Font font) {
			((TreeItem) o).setFont(font);
		}

		@Override
		public Font getFont(final Widget o) {
			return ((TreeItem) o).getFont();
		}
	}
}