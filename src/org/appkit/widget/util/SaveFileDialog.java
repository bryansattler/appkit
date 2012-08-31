package org.appkit.widget.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.io.File;

import java.text.MessageFormat;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A FileDialog that asks for overwrite-confirmation if a file already exists.
 */
public class SaveFileDialog {

	//~ Instance fields ------------------------------------------------------------------------------------------------

	private final FileDialog dlg;
	private String replaceTitle    = "Replace File?";
	private String replaceFilename = "Replace '{}'?";
	private String replaceOK	   = "Replace";
	private String replaceAbort    = "Abort";

	//~ Constructors ---------------------------------------------------------------------------------------------------

	public SaveFileDialog(final Shell parentShell) {
		this.dlg = new FileDialog(parentShell, SWT.SAVE);
	}

	//~ Methods --------------------------------------------------------------------------------------------------------

	public String open() {

		// We store the selected file name in fileName
		String fileName = null;

		// The user has finished when one of the
		// following happens:
		// 1) The user dismisses the dialog by pressing Cancel
		// 2) The selected file name does not exist
		// 3) The user agrees to overwrite existing file
		boolean done = false;

		while (! done) {
			// Open the File Dialog
			fileName = dlg.open();
			if (fileName == null) {
				// User has cancelled, so quit and return
				done = true;
			} else {

				// User has selected a file; see if it already exists
				File file = new File(fileName);
				if (file.exists()) {

					// The file already exists; asks for confirmation
					MBox mb =
						new MBox(
							dlg.getParent(),
							MBox.Type.QUESTION,
							this.replaceTitle,
							MessageFormat.format(this.replaceFilename, fileName),
							1,
							this.replaceOK,
							this.replaceAbort);

					// If they click Yes, we're done and we drop out. If
					if (mb.showReturningInt() == 0) {
						done = true;
					}
				} else {
					// File does not exist, so drop out
					done = true;
				}
			}
		}
		return fileName;
	}

	public void translateMessage(final String i18nInfo) {

		List<String> texts = Lists.newArrayList(Splitter.on("/").split(i18nInfo));
		Preconditions.checkArgument(texts.size() == 4, "need four strings, separated by /");

		this.replaceTitle		 = texts.get(0);
		this.replaceFilename     = texts.get(1);
		this.replaceOK			 = texts.get(2);
		this.replaceAbort		 = texts.get(3);
	}

	public void setFileName(final String string) {
		dlg.setFileName(string);
	}

	public void setFilterExtensions(final String extensions[]) {
		dlg.setFilterExtensions(extensions);
	}

	public void setFilterNames(final String names[]) {
		dlg.setFilterNames(names);
	}

	public void setFilterPath(final String string) {
		dlg.setFilterPath(string);
	}

	public void setText(final String string) {
		dlg.setText(string);
	}
}