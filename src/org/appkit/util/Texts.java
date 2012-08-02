package org.appkit.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;

import java.text.MessageFormat;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

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
	public Texts(final ParamInputSupplier<String, InputStream> dataSupplier, final String resourceName) {
		try {
			L.debug("loading language-properties out of resource: '{}'", resourceName);

			Properties i18n		  = new Properties();
			InputStream in		  = dataSupplier.getInput(resourceName);

			i18n.load(in);
			in.close();

			ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
			for (final String property : i18n.stringPropertyNames()) {

				String msgIdentifier = property;
				String msg			 = i18n.getProperty(property);
				L.debug("'{}' -> '{}'", msgIdentifier, msg);
				map.put(msgIdentifier, msg);
			}
			this.texts = map.build();

		} catch (final IOException e) {
			L.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * Loads global i18n-texts for a specified locale.
	 *
	 * <br />
	 * Example: Passing Locale.ENGLISH would load <code>i18n/en.properties</code> from resources.
	 */
	public static Texts fromResources(final Locale locale) {
		return new Texts(ResourceStreamSupplier.create(), "i18n/" + locale.getLanguage() + ".properties");
	}

	/**
	 * Loads i18n-texts for a specified locale and a name.
	 *
	 * <br />
	 * Example: Passing Locale.ENGLISH and 'test2/myTexts' would load <code>i18n/test2/myTexts.en.properties</code> from resources.
	 */
	public static Texts fromResources(final String name, final Locale locale) {
		return new Texts(ResourceStreamSupplier.create(), "i18n/" + name + "." + locale.getLanguage() + ".properties");
	}

	/**
	 * Translates a {@link Component} by loading language files from resources and using all keys of the files to
	 * find containing controls. I18n-texts are set by calling setText() on {@link Button}s, {@link Text}s and {@link Label}s.
	 * Other controls are ignored until they implement {@link Translateable}.
	 * <br />
	 * The language-files will be used:
	 * <br />
	 * <br />
	 * <li> common defaults: e.g. <code>i18n/common.en.properties</code>
	 * <li> component-defaults: e.g. <code>i18n/components/table.en.properties</code>
	 *
	 * @see Component
	 * @see Templating
	 * @see Translateable
	 */
	public static void translateComponent(final Component component, final Locale locale) {

		Map<String, String> map = Maps.newHashMap();
		map.putAll(Texts.fromResources("common", locale).asMap());
		map.putAll(Texts.fromResources("components/" + component.getName(), locale).asMap());

		for (final Entry<String, String> entry : map.entrySet()) {

			String query	   = entry.getKey();
			String translation = entry.getValue();

			int count = component.count(query);
			if (count == 0) {
				L.debug("skipping '{}', not needed", query);
			} else if (count > 1) {
				L.error("multiple results for '{}'", query);
			} else {

				Control c = component.select(query);
				if (Translateable.class.isAssignableFrom(c.getClass())) {
					((Translateable) c).translate(translation);
				} else if (c instanceof Button) {
					((Button) c).setText(translation);
					c.getParent().layout();
				} else if (c instanceof Label) {
					((Label) c).setText(translation);
					c.getParent().layout();
				} else if (c instanceof Text) {
					((Text) c).setText(translation);
					c.getParent().layout();
				} else {
					L.error("don't know how to translate widget: {}", c);
				}
			}
		}

		component.getComposite().layout(true);
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
	public static interface Translateable {
		public void translate(final String i18nInfo);
	}
}