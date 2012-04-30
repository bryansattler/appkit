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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.List;
import java.util.Map;

import org.appkit.application.EventContext;
import org.appkit.util.ParamSupplier;
import org.appkit.util.ResourceStringSupplier;
import org.appkit.widget.Datepicker;
import org.appkit.widget.GridComposite;
import org.appkit.widget.RadioSet;
import org.appkit.widget.Search;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * Templating enables you to load Interface-description out of JSON. Other/different formats may follow.
 *
 * It creates a {@link Component} which can be used to work with the Interface in an easy-to-use way.
 *
 */
public final class Templating {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(Templating.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final ParamSupplier<String, String> templateSupplier;
	private final Gson gson;
	private final Map<String, Class<?extends ControlCreator<?>>> customCreators = Maps.newHashMap();
	private final Map<String, Class<?extends Control>> types = Maps.newHashMap();

	//~ Constructors ---------------------------------------------------------------------------------------------------

	/** instantiates Templating which loads json via the given <code>templateSupplier</code> */
	public Templating(final ParamSupplier<String, String> templateSupplier) {
		this.templateSupplier = templateSupplier;

		/* built in types */
		this.addCustomCreator(ControlCreator.ButtonCreator.class, "button");
		this.addCustomCreator(ControlCreator.LabelCreator.class, "label");
		this.addCustomCreator(ControlCreator.TableCreator.class, "table");
		this.addCustomCreator(ControlCreator.TextCreator.class, "text");
		this.addType(Search.class, "search");
		this.addType(Datepicker.class, "datepicker");
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
	 * instantiates Templating which loads from resources.
	 * Example: the type "orderview" will be loaded from the file 'components/orderview.json' in resources
	 */
	public static Templating fromResources() {
		return new Templating(ResourceStringSupplier.instance());
	}

	/** registers a new control type
	 *
	 * @throws IllegalStateException if type was already registered
	 */
	public void addType(final Class<?extends Control> type, final String typeName) {
		Preconditions.checkState(! this.types.containsKey(typeName), "type %s already registered", typeName);

		this.types.put(typeName, type);
	}

	/** registers a custom {@link ControlCreator} for types that don't have a ({@link EventContext}, {@link Composite}, {@link Options}) constructor
	 *
	 * @throws IllegalStateException if creator was already registered
	 */
	public void addCustomCreator(final Class<?extends ControlCreator<?>> creator, final String typeName) {
		Preconditions.checkState(
			! this.customCreators.containsKey(typeName),
			"custom creator for type %s already registered",
			typeName);

		this.customCreators.put(typeName, creator);
	}

	/** load and creates a component of the specified type
	 *
	 * @throws IllegalStateException when JSON parsing failed or there other errors
	 */
	public Component create(final String componentType, final EventContext context, final Composite parent) {
		/* get file */
		L.debug("loading component: {}", componentType);

		String file   = "components/" + componentType + ".json";
		String source = this.templateSupplier.get(file);
		Preconditions.checkArgument(source != null, "file '%s' not found", file);

		/* parse json */
		WidgetDefinition definition = null;
		try {
			L.debug("deserializing component: " + componentType);
			definition = this.gson.fromJson(source, WidgetDefinition.class);
		} catch (final JsonParseException e) {
			L.error(e.getMessage(), e);
			throw new IllegalStateException(e.getMessage(), e);
		}

		/* initialize controls */
		L.debug("creating component: {}", componentType);

		return new Component(definition);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private static final class ImmutableListDeserializer implements JsonDeserializer<ImmutableList<?>> {
		@Override
		public ImmutableList<?> deserialize(final JsonElement json, final Type type,
											final JsonDeserializationContext context)
									 throws JsonParseException
		{

			Type type2   =
				ParameterizedTypeImpl.make(List.class, ((ParameterizedType) type).getActualTypeArguments(), null);
			List<?> list = context.deserialize(json, type2);

			return ImmutableList.copyOf(list);
		}
	}
}