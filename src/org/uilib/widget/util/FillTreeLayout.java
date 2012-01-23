package org.uilib.widget.util;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FillTreeLayout {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L = LoggerFactory.getLogger(FillTreeLayout.class);

	//~ Constructors ---------------------------------------------------------------------------------------------------

	protected FillTreeLayout(final Tree tree) {

		/* install layout into parentComposite of Table */
		TreeColumnLayout layout = new TreeColumnLayout();
		tree.getParent().setLayout(layout);

		/* size all the columns */
		int weight = 100 / tree.getColumnCount();
		for (final TreeColumn c : tree.getColumns()) {
			layout.setColumnData(c, new ColumnWeightData(weight));
		}
	}
}