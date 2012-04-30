package org.appkit.templating;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WidgetDefinition {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private String name;
	private String type;
	private ImmutableList<WidgetDefinition> children;
	private Options options;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public WidgetDefinition(final String name, final String type, final List<WidgetDefinition> children,
							final Options options) {
		this.name		  = name;
		this.type		  = type;
		this.children     = ImmutableList.copyOf(children);
		this.options	  = options;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public ImmutableList<WidgetDefinition> getChildren() {
		return children;
	}

	public Options getOptions() {
		return options;
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	public static final class Deserializer implements JsonDeserializer<WidgetDefinition> {

		@SuppressWarnings("unused")
		private static final Logger L							 =
			LoggerFactory.getLogger(WidgetDefinition.Deserializer.class);
		private final Type immutableListType					 =
			new TypeToken<ImmutableList<WidgetDefinition>>() {}
			.getType();

		@Override
		public WidgetDefinition deserialize(final JsonElement json, final Type type,
											final JsonDeserializationContext context) {

			JsonObject jsonObject = json.getAsJsonObject();

			/* 1. if component is empty, it's a spacer */
			if (jsonObject.entrySet().isEmpty()) {
				return new WidgetDefinition("no-name", "spacer", ImmutableList.<WidgetDefinition>of(), Options.empty());
			}

			/* 2. name, defaults to 'no-name' if non-existent */
			JsonElement jsonName = jsonObject.get("name");
			String name			 = ((jsonName != null) ? jsonName.getAsString().toLowerCase() : "no-name");

			/* 3. type, defaults to 'grid' if non-existent */
			JsonElement jsonType    = jsonObject.get("type");
			String componentType    = ((jsonType != null) ? jsonType.getAsString().toLowerCase() : "grid");

			/* 4. options = all other parameters (no name, children or type) */
			Map<String, String> map = Maps.newHashMap();
			for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {

				String key		   = entry.getKey().toLowerCase();
				JsonElement option = entry.getValue();

				if (key.equals("name") || key.equals("children") || key.equals("type")) {
					continue;
				}

				Preconditions.checkState(option.isJsonPrimitive(), "option %s is no json-primitive", option);
				map.put(key, option.getAsString().toLowerCase());
			}

			Options options							 = Options.of(map);

			/* 5. children */
			ImmutableList<WidgetDefinition> children = null;
			if (jsonObject.has("children")) {
				Preconditions.checkState(jsonObject.get("children").isJsonArray(), "children is not an array");
				children = context.deserialize(jsonObject.get("children").getAsJsonArray(), this.immutableListType);
			} else {
				children = ImmutableList.of();
			}

			return new WidgetDefinition(name, componentType, children, options);
		}
	}
}