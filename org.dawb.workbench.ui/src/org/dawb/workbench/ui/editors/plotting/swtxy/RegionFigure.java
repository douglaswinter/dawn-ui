package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * Shape used for ROIs which has bounds fixed to the graph area.
 * 
 * @author fcp94556
 *
 */
public abstract class RegionFigure extends Figure {
	
	protected String name;
	protected Trace  trace;
	protected XYGraph xyGraph;
	protected boolean free = true;

	public RegionFigure(String name, Trace trace) {
		super();
		this.name  = name;
		this.trace = trace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Trace getTrace() {
		return trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public void setxyGraph(XYGraph xyGraph) {
		this.xyGraph = xyGraph;
	}

	public boolean isFree() {
		return free;
	}

	public void setFree(boolean free) {
		this.free = free;
	}

	public Axis getXAxis() {
		return trace.getXAxis();
	}
	public Axis getYAxis() {
		return trace.getYAxis();
	}

	private Color regionColor;
	public void setRegionColor(Color color) {
		this.regionColor = color;
	}
	public Color getRegionColor(){
		return regionColor;
	}
  
	private boolean showPosition = true;

	public boolean isShowPosition() {
		return showPosition;
	}

	public void setShowPosition(boolean showPosition) {
		this.showPosition = showPosition;
	}
	
}
