package org.appkit.templating;

import com.google.common.base.Preconditions;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Component {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final String typeName;
	private final Composite composite;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public Component(final WidgetDefinition definition) {
		this.typeName	   = null;
		this.composite     = null;
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public Composite getComposite() {
		Preconditions.checkState(composite != null, "this component isn't a composite");
		return this.composite;
	}

	public String getTypeName() {
		return this.typeName;
	}

	/**
	 * returns the control of the component selected by the given name and class
	 *
	 * @throws IllegalStateException if not exactly 1 was found
	 *
	 */
	public final <T extends Control> T select(final String name, final Class<T> clazz) {
		return null;
	}

	/**
	 * returns the control of the component selected by the given name
	 *
	 * @throws IllegalStateException if not exactly 1 was found
	 *
	 */
	public final Control select(final String name) {
		return null;
	}
}