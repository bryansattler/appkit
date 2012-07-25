package org.appkit.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.InputStream;

import java.text.MessageFormat;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.appkit.templating.Component;
import org.appkit.templating.Templating;
import org.appkit.templating.widget.RadioSet;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves as a store for i18n-texts and provides methods for working with them.
 *
 */
public final class Texts {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(Texts.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final ImmutableMap<String, String> texts;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	/**
	 * loads Text by passing the <code>resourceName</code> into the <code>dataSupplier</data>
	 */
	public Texts(final ParamSupplier<String, InputStream> dataSupplier, final String resourceName) {

		ImmutableMap.Builder<String, String> map = ImmutableMap.builder();

		try {
			L.debug("loading language out of ressource: " + resourceName);

			Properties i18n = new Properties();
			InputStream in  = dataSupplier.get(resourceName);

			i18n.load(in);
			in.close();

			for (final String property : i18n.stringPropertyNames()) {

				String msgIdentifier = property;
				String msg			 = i18n.getProperty(property);
				L.debug(msgIdentifier + " -> " + msg);
				map.put(msgIdentifier, msg);
			}
		} catch (final IOException e) {
			L.error(e.getMessage(), e);
		}

		this.texts = map.build();
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * Loads i18n-texts using the default locale.
	 *
	 * @see #fromResources(Locale)
	 */
	public static Texts fromResources() {
		return fromResources(Locale.getDefault());
	}

	/**
	 * Loads i18n-texts using a file from resources and a specified locale.
	 * <br />
	 * Example: Passing Locale.ENGLISH would load <code>i18n/en.properties</code> from resources.
	 */
	public static Texts fromResources(final Locale locale) {
		return new Texts(new ResourceStreamSupplier(), "i18n/" + locale.getLanguage() + ".properties");
	}

	/**
	 * Translates a component using the default locale.
	 *
	 * @see #translateComponent(Component, Locale, String)
	 */
	public static void translateComponent(final Component component) {
		translateComponent(component, Locale.getDefault(), null);
	}

	/**
	 * Translates a component using the default locale.
	 *
	 * @see #translateComponent(Component, Locale, String)
	 */
	public static void translateComponent(final Component component, final String customI18NFile) {
		translateComponent(component, Locale.getDefault(), customI18NFile);
	}

	/**
	 * Translates a component using the specified locale.
	 *
	 * @see #translateComponent(Component, Locale, String)
	 */
	public static void translateComponent(final Component component, final Locale locale) {
		translateComponent(component, locale, null);
	}

	/**
	 * Translates a {@link Component} by loading language files from resources and using all keys of the files to
	 * find containing controls. I18n-texts are set by calling setText() on {@link Button}s, {@link Text}s and {@link Label}s.
	 * Other controls are ignored until they implement {@link CustomTranlation}.
	 * <br />
	 * The files are loaded in the following order:
	 * <br />
	 * <br />
	 * <li> if customI18NFile was specified it is loaded, the filename used is <code>i18n/customI18NFile.language.properties</code>
	 * <li> the file for the component is loaded, the filename used is <code>i18n/components/type.language.properties</code>
	 * <li> the appkit-defaults are loaded, the filename used is <code>i18n/appkit-default.language.properties</code>
	 *
	 * @see Component
	 * @see Templating
	 * @see CustomTranlation
	 */
	public static void translateComponent(final Component type, final Locale locale, final String customI18NFile) {

		ResourceStreamSupplier supplier = new ResourceStreamSupplier();

		List<String> files = Lists.newArrayList();
		if (customI18NFile != null) {
			files.add("i18n/" + customI18NFile + "." + locale.getLanguage() + ".properties");
		}
		files.add("i18n/components/" + type.getName() + "." + locale.getLanguage() + ".properties");
		files.add("i18n/appkit-default." + locale.getLanguage() + ".properties");

		Set<Control> translated = Sets.newHashSet();
		for (final String file : files) {

			Texts texts = new Texts(supplier, file);

			for (final Entry<String, String> msg : texts.asMap().entrySet()) {

				/* get control */
				Control c = type.select(msg.getKey());

				/* skip already translated */
				if (translated.contains(c)) {
					continue;
				}

				if (c instanceof CustomTranlation) {
					((CustomTranlation) c).translate(msg.getValue());
					translated.add(c);
				} else if (c instanceof Button) {
					((Button) c).setText(msg.getValue());
					c.getParent().layout();
					translated.add(c);
				} else if (c instanceof Label) {
					((Label) c).setText(msg.getValue());
					c.getParent().layout();
					translated.add(c);
				} else if (c instanceof Text) {
					((Text) c).setText(msg.getValue());
					c.getParent().layout();
					translated.add(c);
				} else {
					L.error("don't know how to translate widget: {}", c);
				}
			}
		}

		type.getComposite().layout();
	}

	/**
	 * Return the loaded texts as a Map.
	 */
	public ImmutableMap<String, String> asMap() {
		return this.texts;
	}

	/**
	 * Returns the i18n-text for the given identifier, formatted with the values
	 *
	 * @return "missing identifier" if no string was found
	 */
	public String get(final String identifier, final Object... values) {

		String text = this.texts.get(identifier);

		if (text == null) {
			text = "<missing identifier>";
			L.error("missing identifier: " + identifier);
		}

		return MessageFormat.format(text, values);
	}

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

	/**
	 * Implement this to make a control translatable. The content of i18nInfo doesn't have to follow a particular format.
	 * It usually is just a string but for {@link RadioSet} for example it contains multiple key-value-pairs to
	 * translate all of it's options.
	 */
	public static interface CustomTranlation {
		public void translate(final String i18nInfo);
	}
}