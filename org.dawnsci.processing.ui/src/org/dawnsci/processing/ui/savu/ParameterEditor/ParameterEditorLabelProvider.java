package org.dawnsci.processing.ui.savu.ParameterEditor;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Table viewer label provider
 *
 */
class ParameterEditorLabelProvider extends ColumnLabelProvider {

	private int column;

	public ParameterEditorLabelProvider(ParameterEditorTableViewModel viewModel, int column) {
		// TODO Auto-generated constructor stub
		this.column = column;
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		final ParameterEditorRowDataModel model = (ParameterEditorRowDataModel)element;
		DecimalFormat pointFormat = new DecimalFormat("##0.00###");
		switch (column) {
		case 0:

			return model.getKey(); 
		case 1:

			Object outVal = model.getValue(); 
				if (outVal instanceof Double) {
					return pointFormat.format(outVal);

				}
				if (outVal instanceof Integer) {
					return outVal.toString();

				}

				if (outVal instanceof Boolean) {
					return outVal.toString();
				}

				if (outVal instanceof String) {
					return outVal.toString();
				}

		case 2:

			return model.getDescription();
		default:
			return "No cheese";
		}
	}

	@Override
	public String getToolTipText(Object element) {
		return "";
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}