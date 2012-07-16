package org.appkit.widget.util.impl;

import org.appkit.widget.util.TableUtils;
import org.appkit.widget.util.TableUtils.ScrollEvent;
import org.appkit.widget.util.TableUtils.ScrollListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TableScrollDetector implements PaintListener {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(TableScrollDetector.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final Table table;
	private final ScrollListener listener;
	private int firstVisible								 = 0;
	private int lastVisible									 = 0;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public TableScrollDetector(final Table table, final ScrollListener listener) {
		this.table		  = table;
		this.listener     = listener;
		this.table.addPaintListener(this);
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	@Override
	public void paintControl(final PaintEvent event) {

		int totalRows   = table.getItemCount();
		int newFirstVis = table.getTopIndex();
		int newLastVis  = TableUtils.getBottomIndex(table);

		/* -1 if no data in table */
		if (totalRows == 0) {
			newFirstVis     = -1;
			newLastVis	    = -1;
		}

		if ((newFirstVis != firstVisible) || (newLastVis != lastVisible)) {
			this.listener.scrolled(new ScrollEvent(totalRows, newFirstVis, newLastVis));
		}

		this.firstVisible     = newFirstVis;
		this.lastVisible	  = newLastVis;
	}
}