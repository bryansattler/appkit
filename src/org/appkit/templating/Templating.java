package org.appkit.templating;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.List;
import java.util.Map;

import org.appkit.templating.event.EventContext;
import org.appkit.templating.event.EventContexts;
import org.appkit.templating.widget.DatePicker;
import org.appkit.templating.widget.GridComposite;
import org.appkit.templating.widget.RadioSet;
import org.appkit.templating.widget.Search;
import org.appkit.util.ParamSupplier;
import org.appkit.util.ResourceStringSupplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * Templating enables loading Interface-Descriptions out of JSON. More formats may follow. <br />
 *
 * The {@link #create(String, Composite)} method creates a {@link Component} which can be used to work with the Interface in an easy-to-use way:
 *
 */
public final class Templating {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(Templating.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final ParamSupplier<String, String> templateSupplier;
	private final Gson gson;
	private final Map<String, ControlCreator<?>> customCreators = Maps.newHashMap();
	private final Map<String, Class<?extends Control>> types = Maps.newHashMap();

	//~ Constructors ---------------------------------------------------------------------------------------------------

	/** Instantiates Templating with a <code>templateSupplier</code> to load the JSON-files. */
	public Templating(final ParamSupplier<String, String> templateSupplier) {
		this.templateSupplier = templateSupplier;

		/* built in types */
		this.addCustomCreator(new DefaultCreators.ButtonCreator(), "button");
		this.addCustomCreator(new DefaultCreators.LabelCreator(), "label");
		this.addCustomCreator(new DefaultCreators.TableCreator(), "table");
		this.addCustomCreator(new DefaultCreators.SpacerCreator(), "spacer");
		this.addCustomCreator(new DefaultCreators.TextCreator(), "text");
		this.addType(Search.class, "search");
		this.addType(DatePicker.class, "datepicker");
		this.addType(RadioSet.class, "radioset");
		this.addType(GridComposite.class, "grid");

		/* configure Gson */
		GsonBuilder gBuilder = new GsonBuilder();
		gBuilder.registerTypeAdapter(WidgetDefinition.class, new WidgetDefinition.Deserializer());
		gBuilder.registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer());

		/* ...and construct it */
		this.gson = gBuilder.create();
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * Instantiates Templating which loads templates from resources.
	 * <br />
	 * Example: the type "orderview" will be loaded from the file <code>components/orderview.json</code> in resources
	 */
	public static Templating fromResources() {
		return new Templating(ResourceStringSupplier.instance());
	}

	/**
	 * Registers a new type of control.
	 *
	 * @throws IllegalStateException if type was already registered
	 */
	public void addType(final Class<?extends Control> type, final String typeName) {
		Preconditions.checkState(! this.types.containsKey(typeName), "type %s already registered", typeName);

		this.types.put(typeName, type);
	}

	/**
	 * Registers a custom {@link ControlCreator} for types that don't have a ({@link EventContext}, {@link Composite}, {@link Options}) constructor
	 *
	 * @throws IllegalStateException if creator was already registered
	 */
	public void addCustomCreator(final ControlCreator<?> creator, final String typeName) {
		Preconditions.checkState(
			! this.customCreators.containsKey(typeName),
			"custom creator for type %s already registered",
			typeName);

		this.customCreators.put(typeName, creator);
	}

	/**
	 * Creates a component of the specified type.
	 *
	 * @throws IllegalStateException when JSON loading or parsing failed
	 */
	public Component create(final String componentName, final Composite parent) {
		return this.create(componentName, EventContexts.NOOP, parent);
	}

	/**
	 * Creates a component of the specified type with a given {@link EventContext}
	 *
	 * @throws IllegalStateException when JSON loading or parsing failed
	 */
	public Component create(final String componentName, final EventContext context, final Composite parent) {
		/* get file */
		L.debug("loading component: '{}'", componentName);

		String file   = "components/" + componentName + ".json";
		String source = this.templateSupplier.get(file);
		Preconditions.checkArgument(source != null, "file '%s' not found", file);

		/* parse json */
		L.debug("deserializing component: '{}'", componentName);

		WidgetDefinition definition = null;

		try {
			definition = this.gson.fromJson(source, WidgetDefinition.class);
		} catch (final JsonSyntaxException e) {
			L.error(e.getMessage());
			return null;
		}

		/* initialize controls */
		L.debug("creating component: '{}'", componentName);

		return new Component(componentName, definition, parent, context, customCreators, types);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private static final class ImmutableListDeserializer implements JsonDeserializer<ImmutableList<?>> {
		@Override
		public ImmutableList<?> deserialize(final JsonElement json, final Type type,
											final JsonDeserializationContext context)
									 throws JsonParseException
		{

			Type type2 =
				ParameterizedTypeImpl.make(List.class, ((ParameterizedType) type).getActualTypeArguments(), null);
			List<?> list = context.deserialize(json, type2);

			return ImmutableList.copyOf(list);
		}
	}
}