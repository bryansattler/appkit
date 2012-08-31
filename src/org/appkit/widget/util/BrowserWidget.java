package org.appkit.widget.util;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.InputStream;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BrowserWidget extends Composite {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	private static final Logger L = LoggerFactory.getLogger(BrowserWidget.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final List<String> outstandingExecutes = Lists.newArrayList();
	private final Browser browser;
	private boolean isLoaded					   = false;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public BrowserWidget(final Composite parent, final int style) {
		super(parent, SWT.NONE);
		this.setLayout(new FillLayout());

		this.browser = new Browser(this, style);
		this.browser.addProgressListener(
			new ProgressListener() {
					@Override
					public void changed(final ProgressEvent event) {}

					@Override
					public void completed(final ProgressEvent event) {
						L.debug("browser loaded");
						isLoaded = true;
						for (final String cmd : outstandingExecutes) {
							L.debug("executing outstanding command {}", cmd);
							browser.execute(cmd);
						}
					}
				});
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public abstract ImmutableList<String> getStyleSheets();

	public abstract ImmutableList<String> getJavaScripts();

	public abstract String getBody();

	public abstract InputStream getImage(final String image);

	public abstract Object callback(final String jsFnName, final Object args[]);

	protected final void enableCallback(final String jsFnName) {
		new BrowserFunction(this.browser, jsFnName) {
				@Override
				public Object function(final Object args[]) {
					return BrowserWidget.this.callback(jsFnName, args);
				}
			};
	}

	protected final void executeCmd(final String cmd) {
		if (! isLoaded) {
			L.debug("adding command '{}' to outstanding-list", cmd);
			this.outstandingExecutes.add(cmd);
		} else {
			L.debug("executing command: '{}'", cmd);
			this.browser.execute(cmd);
		}
	}

	protected final void reloadWidget() {
		this.isLoaded = false;
		this.outstandingExecutes.clear();
		this.browser.setText(this.loadPage());
	}

	private final String loadPage() {

		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html><html><head>");
		for (final String styleSheet : this.getStyleSheets()) {
			sb.append("<style type=\"text/css\" media=\"all\">");
			sb.append(styleSheet);
			sb.append("</style>");
		}
		for (final String javascript : this.getJavaScripts()) {
			sb.append("<script type=\"text/javascript\"");
			sb.append(javascript);
			sb.append("</script>");
		}
		sb.append("</head>");
		sb.append("<body>");
		sb.append(this.getBody());
		sb.append("</body>");
		sb.append("</html>");

		return sb.toString();
	}

	private final String jsFunction(final String name, final Object... args) {

		StringBuilder str = new StringBuilder();
		str.append(name);
		str.append("(");

		Collection<String> arguments =
			Collections2.transform(
				Arrays.asList(args),
				new Function<Object, String>() {
						@Override
						public String apply(final Object arg0) {
							if (arg0 instanceof String) {
								return "'" + ((String) arg0).replace("'", "&#39;") + "'";
							}

							return arg0.toString();
						}
					});
		str.append(Joiner.on(",").join(arguments));
		str.append(")");

		return str.toString();
	}
}