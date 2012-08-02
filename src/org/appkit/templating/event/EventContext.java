package org.appkit.templating.event;


/**
 * The context in which an objects fires events. The events will be fired in this
 * context, which means that the objects registered to the context will receive it. Registration
 * is specified by the implementation.
 *
 * @see LocalEventContext
 *
 */
public interface EventContext {

	//~ Methods --------------------------------------------------------------------------------------------------------

	/**
	 * post any kind of object as event
	 *
	 * @param event the event-object, doesn't have to be, but should be thought of a Serializable
	 */
	public void postEvent(final Object event);
}