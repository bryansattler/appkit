package org.appkit.templating;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.List;
import java.util.Map;

import org.appkit.templating.event.EventContext;
import org.appkit.util.Naming;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the handle on the Component you get after calling {@link Templating#create(String, Composite)}.
 * <br />
 * <br />
 * You can get to the individual widgets by selecting them via a custom query syntax. They can also be selected via
 * their Java-type, in which case they are cast to the type automatically.
 * <br />
 * The query format goes as follows: <code>name$type</code>. <code>type</code> matches the component-type,
 * <code>name</code> matches any prefix or suffix of the components name.
 * <br>
 * Examples:<br/>
 * <li> <code>$datepicker</code> selects the single datepicker found
 * <li> <code>sidebar$grid</code> selects the table found somewhere under "sidebar"
 * <li> <code>stores$table</code> selects the table found somewhere under "stores"
 */
public final class Component extends Naming<Control> {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(Component.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final String componentName;
	private final Composite composite;
	private final Map<Control, WidgetDefinition> defMap;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	protected Component(final String componentName, final WidgetDefinition definition, final Composite parent,
						final EventContext context, final Map<String, ControlCreator<?>> customCreators,
						final Map<String, Class<?extends Control>> types) {
		Preconditions.checkArgument(definition.getName().isEmpty(), "don't give the top-composite a name");

		this.componentName		  = componentName;
		this.defMap				  = Maps.newHashMap();
		this.setQueryMatcher(new ControlMatcher());

		/* recursive initialization */
		Control control = this.initRecursive(definition, parent, context, customCreators, types);
		Preconditions.checkState(control instanceof Composite, "top-control must be a composite");
		this.composite = (Composite) control;

		this.seal();
		L.debug("{}", this.namingEntriesToString());
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	private Control initRecursive(final WidgetDefinition definition, final Composite parent,
								  final EventContext context, final Map<String, ControlCreator<?>> customCreators,
								  final Map<String, Class<?extends Control>> types) {

		String type = definition.getType();

		Control c = null;
		if (customCreators.containsKey(type)) {
			c = customCreators.get(type).initialize(context, parent, definition.getName(), definition.getOptions());

		} else {
			Preconditions.checkArgument(types.containsKey(type), "no type '%s' registered", type);
			try {

				Constructor<?> con =
					types.get(type).getConstructor(EventContext.class, Composite.class, String.class, Options.class);
				c = (Control) con.newInstance(context, parent, definition.getName(), definition.getOptions());

			} catch (final InvocationTargetException e) {
				L.error(e.getMessage(), e);
				throw new RuntimeException(e);
			} catch (final NoSuchMethodException e) {
				L.error(e.getMessage(), e);
				throw new RuntimeException(e);
			} catch (final InstantiationException e) {
				L.error(e.getMessage(), e);
				throw new RuntimeException(e);
			} catch (final IllegalAccessException e) {
				L.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}

		if (! definition.getChildren().isEmpty()) {
			Preconditions.checkState(
				c instanceof Composite,
				"control '%s' has children, but is not a composite, what is going on?",
				definition.getName());
			Preconditions.checkState(
				c instanceof LayoutUI,
				"control '%s' has children, but doesn't implement LayoutUI, what is going on?",
				definition.getName());

			for (final WidgetDefinition childDef : definition.getChildren()) {

				Control child = initRecursive(childDef, (Composite) c, context, customCreators, types);
				((LayoutUI) c).layoutChild(child, childDef.getOptions());
			}
		}

		this.put(c);
		this.defMap.put(c, definition);

		return c;
	}

	/**
	 * Returns the Composite which is the root of the component.
	 */
	public Composite getComposite() {
		return this.composite;
	}

	/**
	 * Returns the name of the component.
	 */
	public String getName() {
		return this.componentName;
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private final class ControlMatcher implements QueryMatcher<Control> {
		@Override
		public String toStringPrimaryKey(final Control c) {

			WidgetDefinition def = defMap.get(c);
			return def.getNamePath() + "$" + def.getType() + ": " + c;
		}

		@Override
		public boolean matches(final Control c, final String str) {

			WidgetDefinition def = defMap.get(c);

			String namePortion = str.toLowerCase();
			if (str.contains("$")) {

				List<String> list = Lists.newArrayList(Splitter.on("$").split(str));
				namePortion = list.get(0).toLowerCase();

				/* compare type */
				String typePortion = list.get(1).toLowerCase();
				if (! typePortion.equalsIgnoreCase(def.getType())) {
					return false;
				}
			}
			if (namePortion.isEmpty()) {
				return true;
			}

			int index = def.getNamePath().indexOf(namePortion);
			if (index == -1) {
				return false;
			}

			if (index > 0) {
				if (def.getNamePath().charAt(index - 1) != '.') {
					return false;
				}
			}
			if ((index + namePortion.length()) < def.getNamePath().length()) {
				if (def.getNamePath().charAt(index + str.length()) != '.') {
					return false;
				}
			}

			return true;
		}
	}
}