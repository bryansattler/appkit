package org.appkit.registry;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import java.util.Map;
import java.util.Map.Entry;

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
 * or manually via the {{@link #putBack(Widget)}-method.
 * <br />
 * This uses a simple counter to keep of track of usage. If it drops to 0, the font
 * is disposed.
 */
public final class Fonts {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(Fonts.class);

	/* default font options */
	private static final String defaultFontName;
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
	public static void set(final Widget widget, final String styleString) {
		set(widget, styleString, null);
	}

	/**
	 * sets a Font, described by a fontStyle on a widget
	 *
	 * @throws IllegalStateException if called from a non-Display thread
	 */
	public static void set(final Widget widget, final String styleString, final String fontName) {
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
		FontStyle fontStyle = FontStyle.parse(styleString);
		String name		    = (fontName != null) ? fontName : defaultFontName;
		int height		    = defaultFontHeight + fontStyle.getHeightDiff();
		int style		    = SWT.NONE;
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

	private static final class FontStyle {

		private boolean italic = false;
		private boolean bold   = false;
		private int heightDiff = 0;

		private FontStyle() {}

		public static FontStyle parse(final String fontString) {

			FontStyle style = new FontStyle();
			for (final String option : Splitter.on(' ').trimResults().split(fontString)) {
				if (option.equalsIgnoreCase("bold")) {
					style.bold = true;
				} else if (option.equalsIgnoreCase("italic")) {
					style.italic = true;
				} else if (option.length() > 1) {
					if (option.charAt(0) == '+') {
						try {
							style.heightDiff = Integer.parseInt(option.substring(1));
						} catch (final NumberFormatException e) {}
					} else if (option.charAt(0) == '-') {
						try {
							style.heightDiff = -Integer.parseInt(option.substring(1));
						} catch (final NumberFormatException e) {}
					}
				}
			}
			return style;
		}

		public int getHeightDiff() {
			return this.heightDiff;
		}

		public boolean bold() {
			return this.bold;
		}

		public boolean italic() {
			return this.italic;
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