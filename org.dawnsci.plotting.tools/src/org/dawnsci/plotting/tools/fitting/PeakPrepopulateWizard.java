package org.dawnsci.plotting.tools.fitting;

import java.util.Collection;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.plotting.tools.finding.PeakFindingController;
import org.dawnsci.plotting.tools.finding.PeakFindingTool;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IPeak;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionFactory;

/**
 * TODO: adjust to spawn peak finder widget
 * 
 * @edits Dean P. Ottewell
 *
 */
public class PeakPrepopulateWizard extends WizardPage {
	
	public PeakPrepopulateWizard() {
		super("Intial peak searching tool");
	}

	private static final Logger logger = LoggerFactory.getLogger(FunctionFittingTool.class);
	
	//Tool common UI elements
	private Composite dialogContainer;
	
	//Peak finding UI elements
	private Combo peakTypeCombo;
	
	private Add pkCompFunction = null;
	private IFunction bkgFunction = null;
	private Add compFunction = null;
	
	private PeakFindingController controller;
	
	private IPlottingSystem<Composite> plotting;
	
	private Collection<ITrace> traces;
	
	private FunctionFittingTool parentFittingTool;
	
	public PeakPrepopulateWizard(FunctionFittingTool parentFittingTool, Dataset[] roiLimits) {
		super("Intial peak searching tool");
		this.setDescription("Search for peaks to then pass onto function fitting");
		this.setTitle("Peak Finding");
		
		this.setControl(parentFittingTool.getControl());
		//Setup the dialog and get the parent fitting tool as well as the ROI limits we're interested in.
	
		//Configure controller for peak tool
		
		this.parentFittingTool = parentFittingTool;
		this.traces = parentFittingTool.getPlottingSystem().getTraces();
		
		this.controller = new PeakFindingController();
		
		//Need to set plotting system in controller
		//this.controller = new PeakFindingController();
		//this.controller.setPlottingSystem(parentFittingTool.getPlottingSystem());
		//this.controller.setRegion((RectangularROI)parentFittingTool.getPlottingSystem().getRegion("fit_region").getROI());
	}
	
	@Override
	public void createControl(Composite parent) {
		
		//Create/get the base containers and set up the grid layout
		dialogContainer = new Composite(parent, SWT.NONE);
		dialogContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		dialogContainer.setLayout(new GridLayout(2, false));
		
		//TODO: create tool segments..
//		Composite tool = new Composite(dialogContainer, SWT.RIGHT);
//		tool.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
//		tool.setLayout(new GridLayout(1, false));
//		
//		
//		IToolBarManager toolBar = new ToolBarManager(new ToolBar(dialogContainer, SWT.FILL));
//		controller.getActions().createActions(toolBar);
		
		//controller.getWidget().createControl(dialogContainer);
		Composite left = new Composite(dialogContainer, SWT.FILL);
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		left.setLayout(new GridLayout());

		Composite right = new Composite(dialogContainer, SWT.FILL);
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		right.setLayout(new GridLayout());
		
		createPlottingSystem(right);
		
		this.controller.setPlottingSystem(plotting);
		//TODO: pass onlt controller
		PeakFindingTool tool = new PeakFindingTool(plotting,controller);
		
		tool.createControl(left);
	}
	
	private void createPlottingSystem(Composite pos){
		Composite plotComp = new Composite(pos, SWT.FILL);
		plotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plotComp.setLayout(new GridLayout());
		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(plotComp, null);
		Composite displayPlotComp  = new Composite(plotComp, SWT.BORDER);
		displayPlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		displayPlotComp.setLayout(new FillLayout());
		
		try {
			plotting = PlottingFactory.createPlottingSystem();
			plotting.createPlotPart(displayPlotComp, "Slice", actionBarWrapper, PlotType.IMAGE, null);
		} catch (Exception e) {
			logger.error("cannot create plotting system",e);
		}
		
		//Populate with last trace
		for(ITrace trace : traces)
			plotting.addTrace(trace);
	}	
	
	/**
	 * Gets the currently selected peak profile type in the combo box
	 * @return peak function class
	 */
	private Class<? extends IPeak> getProfileFunction(){
		String selectedProfileName = peakTypeCombo.getText();
		
		Class<? extends IPeak> selectedProfile = FunctionFactory.getPeakFunctionClass(selectedProfileName);
		
		return selectedProfile;
	}
	
	/**
	 * Update the composite function with either new peaks or new background.
	 * Uses existing background if none is given and does nothing if there are
	 * no peak or background functions.
	 * @param peaks - new peak Add function
	 * @param bkg - new background function
	 */
	private void updateCompFunction(Add peaks, IFunction bkg) {
		try {// Have to copy peak function info across, otherwise we're 
			 // updating the reference, not the object!
			if (peaks != null) {
				compFunction = (Add)peaks.copy();
			} else if (pkCompFunction != null) {
				compFunction = (Add)pkCompFunction.copy();
			} else {
				compFunction = new Add();
			}
		} catch (Exception e) {
			logger.error("Failed to update fit with peak functions",e);
		}
		if (bkg != null) {
			compFunction.addFunction(bkg);
		} else if (bkgFunction != null) {
			compFunction.addFunction(bkgFunction);
		}
	}
	
	/**
	 * Creates peak finding job and then sets parameters for the peak finding before scheduling job.
	 * JobChangeListener waits until peak finding finishes before calling back to parent tool
	 * to draw in located peaks.
	 */
	private void findInitialPeaks() {

		
//		if (findStartingPeaksJob == null) {
//			findStartingPeaksJob = new FindInitialPeaksJob("Find Initial Peaks");
//		}
//		
//		findStartingPeaksJob.setData(roiLimits);
//		findStartingPeaksJob.setNrPeaks(nrPeaks);
//		findStartingPeaksJob.setPeakFunction(getProfileFunction());
//		
//		findStartingPeaksJob.schedule();
//		
//		findStartingPeaksJob.addJobChangeListener(new JobChangeAdapter(){
//			@Override
//			public void done(IJobChangeEvent event) {
//				updateCompFunction(pkCompFunction, null);
//				// TODO this wants updating to use something more generic
//				parentFittingTool.setInitialPeaks(compFunction);
//			}
//		});
	
	
	
	}

}
