package org.appkit.templating;

import org.appkit.templating.event.EventContext;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Implement this to enable {@link Control}s that don't have a ({@link EventContext}, {@link Composite}, {@link Options}) constructor
 * to be used with {@link Templating}
 */
public interface ControlCreator<E extends Control> {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/** Creates the widget */
	E initialize(final EventContext context, final Composite parent, final String name, final Options options);
}