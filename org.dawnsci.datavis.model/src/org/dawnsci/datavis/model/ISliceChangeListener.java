package org.dawnsci.datavis.model;

import java.util.EventListener;

public interface ISliceChangeListener extends EventListener {

	public void sliceChanged(SliceChangeEvent event);
	
}
