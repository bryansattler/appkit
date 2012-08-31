package org.appkit.registry;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.Map.Entry;

import org.appkit.util.ParamInputSupplier;
import org.appkit.util.ResourceStreamSupplier;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** <b>SWT Image cache/registry</b>
 * <br />
 * Creates, assigns and caches {@link Image}s. Images can be set on a {@link Button}s, {@link Label}s, {@link Shell}s or
 * an {@link Control} for which an {@link ImageInterface} is added.
 * Use of an image is deregistered when the widget is disposed, if a different Images is set via this registry
 * or manually via the {@link #putBack(Widget)} method.
 * <br />
 * This uses a simple counter to keep of track of usage. If it drops to 0, the image
 * is disposed.
 * <br />
 * The methods expect {@link Supplier}s for keys. These can be easily implemented by an Enum for example.
 */
public final class Images {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(Images.class);

	/* cache / registry */
	private static final BiMap<Integer, Image> imageCache = HashBiMap.create();
	private static final Multiset<Image> usage			  = HashMultiset.create();
	private static boolean keepCached					  = false;

	/* currently installed disposeListeners */
	private static final Map<Widget, DisposeListener> disposeListeners = Maps.newHashMap();

	/* setters for images */
	private static final Map<Class<?extends Widget>, ImageInterface> setters = Maps.newHashMap();

	static {
		Preconditions.checkArgument(Display.getCurrent() != null, "can't instantiate Images on a non-display thread");
		addImageSetter(Button.class, new ButtonImageInterface());
		addImageSetter(Label.class, new LabelImageInterface());
		addImageSetter(Shell.class, new ShellImageInterface());
	}

	//~ Constructors ---------------------------------------------------------------------------------------------------

	private Images() {}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public static void keepCache(final boolean keep) {
		keepCached = keep;
	}

	private static ImageInterface getSetter(final Widget widget) {
		for (final Entry<Class<?extends Widget>, ImageInterface> entry : setters.entrySet()) {
			if (entry.getKey().isAssignableFrom(widget.getClass())) {
				return entry.getValue();
			}
		}

		return null;
	}

	/**
	 * Tells Images how to set an Image on a certain type.
	 */
	public static <E extends Widget> void addImageSetter(final Class<E> clazz, final ImageInterface setter) {
		setters.put(clazz, setter);
	}

	/**
	 * Sets an image on the widget. The InputStream for loading the image
	 * is retrieved by passing the key received from the
	 * <code>keySupplier</code> into a {@link ResourceStreamSupplier}.
	 *
	 * @throws IllegalStateException if called from a non-Display thread
	 * @throws IllegalArgumentException if image couldn't be set
	 */
	public static void set(final Widget widget, final Supplier<String> keySupplier) {
		set(widget, keySupplier.get(), ResourceStreamSupplier.create());
	}

	/**
	 * Sets an image on the widget. The InputStream for loading the image
	 * is retrieved by passing the key into a {@link ResourceStreamSupplier}.
	 *
	 * @throws IllegalStateException if called from a non-Display thread
	 * @throws IllegalArgumentException if image couldn't be set
	 */
	public static void set(final Widget widget, final String key) {
		set(widget, key, ResourceStreamSupplier.create());
	}

	/**
	 * Sets an image on the widget. The InputStream for loading the image
	 * is retrieved by passing the key received from the
	 * <code>keySupplier</code> into the <code>dataSupplier</code>.
	 *
	 * @throws IllegalStateException if called from a non-Display thread
	 * @throws IllegalArgumentException if image couldn't be set
	 */
	public static <E> void set(final Widget widget, final E key, final ParamInputSupplier<E, InputStream> streamSupplier) {
		Preconditions.checkState(
			Display.getCurrent() != null,
			"Images is to be used from the display-thread exclusively!");
		Preconditions.checkArgument(
			getSetter(widget) != null,
			"don't know how to set image on {}, add an ImageInterface first",
			widget);

		/* if we already set an image on this widget, remove it */
		if (disposeListeners.containsKey(widget)) {
			putBack(widget);
		}

		/* get image out of cache or load it */
		int hash    = Objects.hashCode(key);
		Image image = null;

		L.debug("setting image {} on {}", key, widget);
		if (imageCache.containsKey(hash)) {
			image = imageCache.get(hash);

		} else {
			try {

				InputStream in = streamSupplier.getInput(key);
				if (in == null) {
					L.error("data supplier returned no InputStream for '{}'", key);
					return;
				}

				image = new Image(Display.getCurrent(), in);
				L.debug("created image: {}", image);

				in.close();
			} catch (final IOException e) {
				L.error(e.getMessage(), e);
			}

			if (image != null) {
				imageCache.put(hash, image);
			}
		}

		/* increase usage-counter */
		usage.setCount(image, usage.count(image) + 1);
		L.debug("usage of {} now {}", image, usage.count(image));

		/* set image */
		setters.get(widget.getClass()).setImage(widget, image);

		/* and add the disposer */
		DisposeListener listener = new ImageDisposeListener();
		disposeListeners.put(widget, listener);
		widget.addDisposeListener(listener);
	}

	/**
	 * Manually deregisters use of an image of a widget
	 *
	 * @throws IllegalStateException if called from a non-Display thread
	 * @throws IllegalStateException if widget isn't registered
	 */
	public static void putBack(final Widget widget) {
		/* check for UI-thread */
		Preconditions.checkState(
			Display.getCurrent() != null,
			"Images is to be used from the display-thread exclusively!");
		Preconditions.checkState(disposeListeners.containsKey(widget), "widget {} not registered", widget);

		/* remove widget out of registry and remove listener */
		widget.removeDisposeListener(disposeListeners.remove(widget));

		/* get the image */
		Image image = getSetter(widget).getImage(widget);

		/* decrease usage-counter */
		usage.setCount(image, usage.count(image) - 1);
		L.debug("usage of {} now {}", image, usage.count(image));

		/* if usage is 0 dispose it */
		if (! usage.contains(image)) {
			if (keepCached) {
				L.debug("keeping {} in cache", image);
			} else {
				L.debug("disposing {}", image);
				imageCache.inverse().remove(image);
				image.dispose();
			}
		}
	}

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

	/**
	 * Implement this to enable the use of Images for a custom-widget.
	 *
	 */
	public static interface ImageInterface {
		void setImage(final Widget widget, final Image image);

		Image getImage(final Widget widget);
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	private static final class ImageDisposeListener implements DisposeListener {
		@Override
		public void widgetDisposed(final DisposeEvent event) {
			putBack(event.widget);
		}
	}

	private static final class LabelImageInterface implements ImageInterface {
		@Override
		public void setImage(final Widget o, final Image image) {
			((Label) o).setImage(image);
		}

		@Override
		public Image getImage(final Widget o) {
			return ((Label) o).getImage();
		}
	}

	private static final class ButtonImageInterface implements ImageInterface {
		@Override
		public void setImage(final Widget o, final Image image) {
			((Button) o).setImage(image);
		}

		@Override
		public Image getImage(final Widget o) {
			return ((Button) o).getImage();
		}
	}

	private static final class ShellImageInterface implements ImageInterface {
		@Override
		public void setImage(final Widget o, final Image image) {
			((Shell) o).setImage(image);
		}

		@Override
		public Image getImage(final Widget o) {
			return ((Shell) o).getImage();
		}
	}
}