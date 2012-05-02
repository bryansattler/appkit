package org.appkit.templating;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
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

import org.appkit.widget.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WidgetDefinition {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final CharMatcher NAMEFILTER =
		CharMatcher.inRange('a', 'z').or(CharMatcher.inRange('0', '9')).or(CharMatcher.anyOf("?!-"));

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private WidgetDefinition parentDef;
	private final String name;
	private final String type;
	private final ImmutableList<WidgetDefinition> children;
	private final Options options;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public WidgetDefinition(final String name, final String type, final List<WidgetDefinition> children,
							final Options options) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(children);
		Preconditions.checkNotNull(options);
		Preconditions.checkArgument(
			(name == null) || ! name.isEmpty(),
			"name can be non-existant, but it most not be empty");
		Preconditions.checkArgument(
			(name == null) || NAMEFILTER.matchesAllOf(name),
			"'%s' didn't satisfy name-filter (%s)",
			name,
			NAMEFILTER);

		this.parentDef						    = null;
		this.name							    = name;
		this.type							    = type;
		this.children						    = ImmutableList.copyOf(children);
		this.options						    = options;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public void setParent(final WidgetDefinition parentDef) {
		Preconditions.checkState(this.parentDef == null, "parent is not null");
		this.parentDef = parentDef;
	}

	public String getFullName() {
		if ((this.parentDef != null) && ! this.parentDef.getFullName().equals("")) {
			if (this.name == null) {
				return this.parentDef.getFullName();
			} else {
				return this.parentDef.getFullName() + "." + this.name;
			}
		} else {
			if (this.name == null) {
				return "";
			} else {
				return this.name;
			}
		}
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

		private static final Logger L		 = LoggerFactory.getLogger(WidgetDefinition.Deserializer.class);
		private final Type immutableListType = new TypeToken<ImmutableList<WidgetDefinition>>() {}
			.getType();

		@Override
		public WidgetDefinition deserialize(final JsonElement json, final Type type,
											final JsonDeserializationContext context) {

			JsonObject jsonObject = json.getAsJsonObject();

			/* 1. if component is empty, it's a spacer */
			if (jsonObject.entrySet().isEmpty()) {
				return new WidgetDefinition(null, "spacer", ImmutableList.<WidgetDefinition>of(), Options.empty());
			}

			/* 2. name */
			JsonElement jsonName = jsonObject.get("name");
			String name			 = ((jsonName != null) ? jsonName.getAsString().toLowerCase() : null);

			/* 3. type, defaults to 'grid' if non-existent */
			JsonElement jsonType    = jsonObject.get("type");
			String componentType    = ((jsonType != null) ? jsonType.getAsString().toLowerCase() : "grid");

			/* 4. options = all other parameters (no name, children or type) */
			Map<String, String> map = Maps.newHashMap();
			for (final Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				Preconditions.checkState(
					entry.getValue().isJsonPrimitive(),
					"option %s is no json-primitive",
					entry.getValue());

				String key   = entry.getKey().toLowerCase();
				String value = entry.getValue().getAsString().toLowerCase();

				/* don't put name, children or type in options */
				if (key.equals("name") || key.equals("children") || key.equals("type")) {
					continue;
				}

				if (! key.equals("options")) {
					Preconditions.checkState(! map.containsKey(key), "option key '%s' specified more than once!", key);
					map.put(key, value);
				} else {
					L.debug("splitting up bool-list (options: '{}')", value);
					for (final String subOpt : Splitter.on(' ').trimResults().split(value)) {
						Preconditions.checkState(
							! map.containsKey(subOpt),
							"option key '%s' (found in bool-list (options: '%s') specified more than once!",
							subOpt,
							value);
						map.put(subOpt, "true");
					}
				}
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

			WidgetDefinition def = new WidgetDefinition(name, componentType, children, options);
			for (final WidgetDefinition childDef : def.getChildren()) {
				childDef.setParent(def);
			}

			return def;
		}
	}
}