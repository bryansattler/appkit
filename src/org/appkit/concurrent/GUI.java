package org.appkit.concurrent;

import com.google.common.base.Preconditions;

import org.eclipse.swt.widgets.Display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GUI<E extends Enum<E>> {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(GUI.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	protected final ReportQueue queue;
	private E currentState;
	private GUIState currentGUIState;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public GUI() {
		this.queue											 = ReportQueue.create();
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public abstract GUIState getGUIState(final E state);

	protected abstract void closeGUI();

	public ReportQueue getReports() {
		return this.queue;
	}

	public final void close() {
		Display.getDefault().syncExec(
			new Runnable() {
					@Override
					public void run() {
						closeGUI();
					}
				});
	}

	public final void showState(final E state, final Object... data) {
		Preconditions.checkNotNull(state);
		Display.getDefault().syncExec(
			new Runnable() {
					@Override
					public void run() {
						if (currentState == state) {
							currentGUIState.update(data);
						} else {
							currentState	    = state;
							currentGUIState = getGUIState(state);
							currentGUIState.enter(data);
						}
					}
				});
	}

	//~ Inner Interfaces -----------------------------------------------------------------------------------------------

	public interface GUIState {
		public void enter(Object... data);

		public void update(Object... data);
	}
}