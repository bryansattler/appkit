package org.appkit.widget;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.appkit.event.EventContext;
import org.appkit.util.Texts.CustomTranlation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** for creating a component that is a {@link DateTime} */
public final class Datepicker extends Composite implements CustomTranlation {

	//~ Static fields/initializers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final Logger L							 = LoggerFactory.getLogger(Datepicker.class);

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final DateTime dt;
	private final Label labelFrom;
	private final DateTime dtFrom;
	private final Button bEnableFrom;
	private final Label labelTo;
	private final DateTime dtTo;
	private final Button bEnableTo;

	/* currently selected DateRange */
	private DateRange daterange;

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public Datepicker(final EventContext context, final Composite parent, final Options options) {
		super(parent, (options.get("border", false) ? SWT.BORDER : SWT.NONE));

		GridLayout gl										 = new GridLayout(3, false);
		gl.marginHeight										 = 0;
		gl.marginWidth										 = 0;
		gl.verticalSpacing									 = 2;
		this.setLayout(gl);

		if (! options.get("range", true)) {
			this.dt = new DateTime(this, SWT.DATE | SWT.BORDER | SWT.DROP_DOWN);
			this.dt.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 3, 1));
			this.dt.addSelectionListener(
				new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent event) {
							setInternalDateRange();
							context.postEvent(new Event(Datepicker.this, daterange));
						}
					});
			this.labelFrom		 = null;
			this.dtFrom			 = null;
			this.bEnableFrom     = null;
			this.labelTo		 = null;
			this.dtTo			 = null;
			this.bEnableTo		 = null;
		} else {
			this.dt			   = null;
			this.labelFrom     = new Label(this, SWT.NONE);
			this.labelFrom.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, false, false));
			this.dtFrom = new DateTime(this, SWT.DATE | SWT.BORDER | SWT.DROP_DOWN);
			this.dtFrom.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, false, false));
			this.dtFrom.addSelectionListener(
				new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent event) {
							setInternalDateRange();
							context.postEvent(new Event(Datepicker.this, daterange));
						}
					});

			this.bEnableFrom = new Button(this, SWT.CHECK);
			this.bEnableFrom.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, false));
			this.bEnableFrom.setSelection(true);
			this.bEnableFrom.addSelectionListener(
				new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent event) {
							dtFrom.setEnabled(bEnableFrom.getSelection());
							setInternalDateRange();
							context.postEvent(new Event(Datepicker.this, daterange));
						}
					});

			this.labelTo = new Label(this, SWT.NONE);
			this.labelTo.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, false, false));
			this.dtTo = new DateTime(this, SWT.DATE | SWT.BORDER | SWT.DROP_DOWN);
			this.dtTo.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, false, false));
			this.dtTo.addSelectionListener(
				new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent event) {
							setInternalDateRange();
							context.postEvent(new Event(Datepicker.this, daterange));
						}
					});
			this.bEnableTo = new Button(this, SWT.CHECK);
			this.bEnableTo.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, false));
			this.bEnableTo.setSelection(true);
			this.bEnableTo.addSelectionListener(
				new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent event) {
							dtTo.setEnabled(bEnableTo.getSelection());
							setInternalDateRange();
							context.postEvent(new Event(Datepicker.this, daterange));
						}
					});
		}

		this.setInternalDateRange();
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public DateRange getDateRange() {
		return this.daterange;
	}

	@Override
	public void translate(final String i18nInfo) {

		List<String> texts = Lists.newArrayList(Splitter.on("/").split(i18nInfo));
		Preconditions.checkArgument(texts.size() == 2, "need two strings, separated by /");

		if (this.labelFrom != null) {
			this.labelFrom.setText(texts.get(0));
			this.labelTo.setText(texts.get(1));
		}
	}

	private void setInternalDateRange() {

		Date dateFrom = null;
		Date dateTo   = null;

		if (this.dt != null) {
			dateFrom     = this.constructDate(this.dt);
			dateTo		 = dateFrom;
		} else {
			if (this.dtFrom.getEnabled()) {
				dateFrom = this.constructDate(this.dtFrom);
			}

			if (this.dtTo.getEnabled()) {
				dateTo = this.constructDate(this.dtTo);
			}
		}

		this.daterange = new DateRange(dateFrom, dateTo);
	}

	private Date constructDate(final DateTime dt) {

		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.DAY_OF_MONTH, dt.getDay());
		cal.set(Calendar.MONTH, dt.getMonth());
		cal.set(Calendar.YEAR, dt.getYear());

		return cal.getTime();
	}

	//~ Inner Classes --------------------------------------------------------------------------------------------------

	public static final class DateRange {

		private final Date fromDate;
		private final Date toDate;

		public DateRange(final Date fromDate, final Date toDate) {

			Date tempFrom;
			Date tempTo;

			/* swap dates if necessary */
			if ((fromDate != null) && (toDate != null)) {
				if (fromDate.compareTo(toDate) <= 0) {
					tempFrom     = fromDate;
					tempTo		 = toDate;
				} else {
					tempFrom     = toDate;
					tempTo		 = fromDate;
				}
			} else {
				tempFrom     = fromDate;
				tempTo		 = toDate;
			}

			if (tempFrom != null) {
				this.fromDate = new Date(tempFrom.getTime());
			} else {
				this.fromDate = null;
			}

			if (tempTo != null) {
				this.toDate = new Date(tempTo.getTime());
			} else {
				this.toDate = null;
			}
		}

		public Date getFromDate() {
			if (fromDate != null) {
				return new Date(fromDate.getTime());
			} else {
				return null;
			}
		}

		public Date getToDate() {
			if (toDate != null) {
				return new Date(toDate.getTime());
			} else {
				return null;
			}
		}

		@Override
		public String toString() {
			return "[" + ((this.fromDate != null) ? this.fromDate : "") + ".."
				   + ((this.toDate != null) ? this.toDate : "") + "]";
		}
	}

	public static final class Event {

		private final Datepicker datePicker;
		private final DateRange range;

		public Event(final Datepicker datePicker, final DateRange range) {
			this.datePicker     = datePicker;
			this.range		    = range;
		}

		public final Datepicker getOrigin() {
			return this.datePicker;
		}

		public final DateRange getDateRange() {
			return this.range;
		}
	}
}