/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.api;

import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * Class used to fool plotting system that there is a
 * part which is active. This provides then the IAdaptable
 * for tools to work.
 * 
 * @author fcp94556
 *
 */
public class EmptyWorkbenchPart extends WorkbenchPart {

	private IPlottingSystem system;

	public EmptyWorkbenchPart(IPlottingSystem system) {
		this.system = system;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
    public Object getAdapter(final Class clazz) {
		
		 if (clazz == IToolPageSystem.class) {
			return system;
		}
		
		return null;
	}

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
}
