package org.uilib.templating;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import org.uilib.templating.components.ButtonUI;
import org.uilib.templating.components.ComponentGridUI;
import org.uilib.templating.components.ComponentUI;
import org.uilib.templating.components.DatepickerUI;
import org.uilib.templating.components.LabelUI;
import org.uilib.templating.components.RadioSetUI;
import org.uilib.templating.components.SpacerUI;
import org.uilib.templating.components.StackUI;
import org.uilib.templating.components.TableUI;
import org.uilib.templating.components.TextUI;
import org.uilib.util.ResourceToStringSupplier;
import org.uilib.util.StringSupplier;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

// FIXME: Texts: getSystemDefault Lang
public final class Templating {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = Logger.getLogger(Templating.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final StringSupplier supplier;
	private final Gson gson;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public Templating(final StringSupplier supplier) {
		this.supplier			  = supplier;

		/* configure Gson */
		GsonBuilder gBuilder = new GsonBuilder();
		gBuilder.registerTypeAdapter(Component.class, new ComponentDeserializer());
		gBuilder.registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer());

		/* ...and construct it */
		this.gson = gBuilder.create();
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static Templating fromResources() {
		return new Templating(new ResourceToStringSupplier());
	}

	public Component create(final String componentType) {
		L.debug("creating component: " + componentType);

		String source = this.supplier.get("components/" + componentType + ".json");
		if (source == null) {
			L.debug("none found for: " + componentType);
			return null;
		}

		try {
			L.debug("deserializing component: " + componentType);

			Component component = this.gson.fromJson(source, Component.class);

			return component;
		} catch (final JsonParseException e) {
			L.error(e.getMessage(), e);
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private final class ComponentDeserializer implements JsonDeserializer<Component> {

		private final ImmutableMap<String, Class<?extends ComponentUI>> controllers;
		private final Type immutableListType = new TypeToken<ImmutableList<Component>>() {}
			.getType();

		public ComponentDeserializer() {

			ImmutableMap.Builder<String, Class<?extends ComponentUI>> map = ImmutableMap.builder();
			map.put("button", ButtonUI.class);
			map.put("datepicker", DatepickerUI.class);
			map.put("label", LabelUI.class);
			map.put("stack", StackUI.class);
			map.put("radioset", RadioSetUI.class);
			map.put("table", TableUI.class);
			map.put("componentgrid", ComponentGridUI.class);
			map.put("spacer", SpacerUI.class);
			map.put("text", TextUI.class);

			this.controllers = map.build();
		}

		private ComponentUI instantiateController(final String type) {
			Preconditions.checkArgument(this.controllers.containsKey(type), "no type '%s' registered", type);
			try {
				return this.controllers.get(type).newInstance();
			} catch (final InstantiationException e) {
				L.error(e.getMessage(), e);
				throw new IllegalArgumentException(e.getMessage(), e);
			} catch (final IllegalAccessException e) {
				L.error(e.getMessage(), e);
				throw new IllegalArgumentException(e.getMessage(), e);
			}
		}

		@Override
		public Component deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context) {

			JsonObject jsonObject = json.getAsJsonObject();

			/* 1. if component is empty, it's a spacer */
			if (jsonObject.entrySet().isEmpty()) {
				return new Component(
					null,
					"spacer",
					ImmutableList.<Component>of(),
					this.instantiateController("spacer"),
					Options.empty());
			}

			/* 2. read name (if existent) */
			JsonElement jsonName = jsonObject.get("name");
			String name			 = ((jsonName != null) ? jsonName.getAsString() : null);

			/* 3. read type, default to 'component' if non-existant */
			JsonElement jsonType = jsonObject.get("type");
			String componentType = ((jsonType != null) ? jsonType.getAsString() : "componentgrid");

			L.debug("deserializing: " + componentType);

			/* 4. read all other parameters (no name, children or type)  into options */
			Map<String, String> map = Maps.newHashMap();
			for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {

				String key = entry.getKey();
				if (key.equals("name") || key.equals("children") || key.equals("type")) {
					continue;
				}

				// FIXME: force that this is a string, boolean or integer
				map.put(key, entry.getValue().getAsString());
			}

			Options options = Options.of(map);

			// FIXME: preconditions, no subcomponent if type was found
			/* 5. try to load component which is called like this type (subcomponent) */
			Component subComp = Templating.this.create(componentType);

			/* 6. Check: if a sub-component was found this component isn't allowed to have children */
			Preconditions.checkState(
				(subComp == null) || (jsonObject.get("children") == null),
				"can either refer to a subcomponent or have children itself");

			ComponentUI compUI				  = null;
			ImmutableList<Component> children = null;

			/* 7. if a sub-component was found, we "inline" parts of it in this component by copying it's properties */
			if (subComp != null) {
				/* copy type, controller and children */
				compUI		 = subComp.getUI();
				children     = subComp.getChildren();

				options		 = options.withDefaults(subComp.getOptions());
			} else {
				/* 7b. if it wasn't a reference to a sub-component, we try the registered controllers */
				compUI = this.instantiateController(componentType);

				/* deserialize children */
				// FIXME: force this to be an array
				if (jsonObject.has("children")) {
					children = context.deserialize(jsonObject.get("children").getAsJsonArray(), this.immutableListType);
				} else {
					children = ImmutableList.of();
				}
			}

			return new Component(name, componentType, children, compUI, options);
		}
	}

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