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
import org.appkit.util.Naming.QueryMatcher;

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
public class Component {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(Component.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final String componentName;
	private final Composite composite;
	private final Naming<Control> naming;
	private final Map<Control, WidgetDefinition> defMap;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	protected Component(final String componentName, final WidgetDefinition definition, final Composite parent,
						final EventContext context, final Map<String, ControlCreator<?>> customCreators,
						final Map<String, Class<?extends Control>> types) {
		Preconditions.checkArgument(definition.getFullName().equals(""), "don't give the top-composite a name");

		this.defMap				  = Maps.newHashMap();
		this.naming				  = Naming.create(new WidgetMatcher());

		/* recursive initialization */
		Control control = this.initRecursive(definition, parent, context, customCreators, types);

		this.componentName = componentName;
		if (control instanceof Composite) {
			this.composite = (Composite) control;
		} else {
			this.composite = null;
		}

		this.naming.seal();
		L.debug("{}", this.naming);
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
				throw new IllegalArgumentException(e.getMessage(), e);
			} catch (final NoSuchMethodException e) {
				L.error(e.getMessage(), e);
				throw new IllegalArgumentException(e.getMessage(), e);
			} catch (final InstantiationException e) {
				L.error(e.getMessage(), e);
				throw new IllegalArgumentException(e.getMessage(), e);
			} catch (final IllegalAccessException e) {
				L.error(e.getMessage(), e);
				throw new IllegalArgumentException(e.getMessage(), e);
			}
		}

		if (! definition.getChildren().isEmpty()) {
			Preconditions.checkState(
				c instanceof Composite,
				"control has children, but is not a composite, what is going on?");
			Preconditions.checkState(
				c instanceof LayoutUI,
				"control has children, but doesn't implement LayoutUI, what is going on?");

			for (final WidgetDefinition childDef : definition.getChildren()) {

				Control child = initRecursive(childDef, (Composite) c, context, customCreators, types);
				((LayoutUI) c).layoutChild(child, childDef.getOptions());
			}
		}

		this.naming.put(c);
		this.defMap.put(c, definition);

		return c;
	}

	/**
	 * Returns the Composite which is the root of the component.
	 *
	 * @throws IllegalStateException if the component doesn't have a composite as root.
	 */
	public Composite getComposite() {
		Preconditions.checkState(composite != null, "this component isn't a composite");
		return this.composite;
	}

	/**
	 * Returns the type/name of the component.
	 */
	public String getName() {
		return this.componentName;
	}

	/**
	 * Returns the control selected by the given name and class, cast to the class.
	 *
	 * @throws IllegalStateException if not exactly one was found
	 * @see Templating
	 *
	 */
	public final <T extends Control> T select(final String name, final Class<T> clazz) {
		return this.naming.select(name, clazz);
	}

	/**
	 * Returns the control selected by the given name
	 *
	 * @throws IllegalStateException if not exactly one was found
	 * @see Templating
	 *
	 */
	public final Control select(final String name) {
		return this.naming.select(name);
	}

	/**
	 * Returns the control selected by the given class, cast to the class.
	 *
	 * @throws IllegalStateException if not exactly one was found
	 * @see Templating
	 *
	 */
	public final <T extends Control> T select(final Class<T> clazz) {
		return this.naming.select(clazz);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private final class WidgetMatcher implements QueryMatcher<Control> {
		@Override
		public String toStringPrimaryKey(final Control c) {

			WidgetDefinition def = defMap.get(c);

			return def.getFullName() + "$" + def.getType() + ": " + c;
		}

		@Override
		public boolean matches(final Control c, final String str) {

			String namePortion = null;
			String typePortion = null;
			if (str.contains("$")) {

				List<String> list = Lists.newArrayList(Splitter.on("$").split(str));
				namePortion     = list.get(0).toLowerCase();
				typePortion     = list.get(1).toLowerCase();

			} else {
				namePortion = str.toLowerCase();
			}

			WidgetDefinition def = defMap.get(c);

			/* compare type */
			if ((typePortion != null) && ! typePortion.equals(def.getType())) {
				return false;
			}

			/* compare name */
			if (! def.getFullName().endsWith(namePortion) && ! def.getFullName().startsWith(namePortion)) {
				return false;
			}

			return true;
		}
	}
}