package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.VerticalHorizontalSlices;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class SuperSashPlotSystem3Composite extends Composite{

	private IPlottingSystem<Composite> plotSystem1;/////top plot
    private IPlottingSystem<Composite> plotSystem2;/////main image
    private IPlottingSystem<Composite> plotSystem3;/////side image
    private IRegion verticalSlice;
    private IRegion horizontalSlice;
    private IDataset image2;
    private SashForm right; 
    private SashForm left;
//    private Button backgroundButton;
	private MultipleOutputCurvesTableView outputCurves;
	private SurfaceScatterPresenter ssp;
    private IRegion region;
	
	public SurfaceScatterPresenter getSsp() {
		return ssp;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}


	private SurfaceScatterViewStart ssvs;

	public SurfaceScatterViewStart getSsvs() {
		return ssvs;
	}

	public void setSsvs(SurfaceScatterViewStart ssvs) {
		this.ssvs = ssvs;
	}

	public SuperSashPlotSystem3Composite(Composite parent, int style,
			SurfaceScatterViewStart ssvs) throws Exception {
        super(parent, style);
        
        
        
//        new Label(this, SWT.NONE).setText("Region of Interest");
        this.ssvs = ssvs;
        
        
        if(image2 == null){
        	image2 = (DatasetFactory.ones(new int[] {4,4}));
        }
        
        
        try {
        	plotSystem1 = PlottingFactory.createPlottingSystem();
			plotSystem2 = PlottingFactory.createPlottingSystem();
			plotSystem3 = PlottingFactory.createPlottingSystem();
        } 
        catch (Exception e2) {
			e2.printStackTrace();
		}

        this.createContents(); 
        
    }
	
	 public void createContents() throws Exception {
		 	
		 
		
		 
		SashForm sashForm= new SashForm(this, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
		left = new SashForm(sashForm, SWT.VERTICAL);
		
		right = new SashForm(sashForm, SWT.VERTICAL);
		
		
		sashForm.setWeights(new int[]{50,60});
		

	/////////////////Left SashForm///////////////////////////////////////////////////
//		backgroundButton = new Button(left, SWT.CHECK);
//		backgroundButton.setText("Background Display");
//	    backgroundButton.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, false));
		
		
		Group topImage = new Group(left, SWT.NONE);
        topImage.setText("Top Image");
        GridLayout topImageLayout = new GridLayout();
        topImage.setLayout(topImageLayout);
		GridData topImageData= new GridData(SWT.FILL, SWT.FILL, true, true);
		topImage.setLayoutData(topImageData);
	
		GridData ld1 = new GridData(SWT.FILL, SWT.FILL, true, true);

		ActionBarWrapper actionBarCompositeTop = ActionBarWrapper.createActionBars(topImage, null);;
        
		plotSystem1.createPlotPart(topImage, "Top Image", actionBarCompositeTop, PlotType.IMAGE, null);
		plotSystem1.getPlotComposite().setLayoutData(ld1);
		
		plotSystem1.getAxis("X-Axis").setTitle("");
        
        Group mainImage = new Group(left, SWT.NONE);
        mainImage.setText("Main Image");
        GridLayout mainImageLayout = new GridLayout();
        mainImage.setLayout(mainImageLayout);
		GridData mainImageData= new GridData(SWT.FILL, SWT.FILL, true, true);
		mainImage.setLayoutData(mainImageData);
		
		GridData ld2 = new GridData(SWT.FILL, SWT.FILL, true, true);

		ActionBarWrapper actionBarCompositeMain = ActionBarWrapper.createActionBars(mainImage, null);;
        
		plotSystem2.createPlotPart(mainImage, "Top Image", actionBarCompositeMain, PlotType.IMAGE, null);
		plotSystem2.getPlotComposite().setLayoutData(ld2);
		plotSystem2.createPlot2D(image2, null, null);
//		plotSystem2.getAxis("X-Axis").setTitle("");
		
		
	////////////////////////////////////////////////////////////////////////////////////
	/////////////////Right sashform//////////////////////////////////////////////////////////
//	       
//	        
        
        
        try {
			outputCurves = new MultipleOutputCurvesTableView(right, 
															SWT.FILL, 
															0);
			
			outputCurves.setLayout(new GridLayout());
			outputCurves.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    outputCurves.getOverlapZoom().addSelectionListener(new SelectionListener() {
				    	
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ArrayList<ArrayList<IDataset>> xyArrays = ssp.xyArrayPreparer();
				
				GeneralOverlapHandlerView goh = new GeneralOverlapHandlerView(
						ssp.getParentShell(), 
						SWT.OPEN, 
						xyArrays.get(0),
						xyArrays.get(1),
						xyArrays.get(2),
						xyArrays.get(3),
						xyArrays.get(4), 
						outputCurves.getPlotSystem(),
						ssp);
				
				goh.open();
				
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
        
        Group sideImage = new Group(right, SWT.NONE);
        sideImage.setText("Side Image");
        GridLayout sideImageLayout = new GridLayout();
        sideImage.setLayout(sideImageLayout);
		GridData sideImageData= new GridData(SWT.FILL, SWT.FILL, true, true);
		sideImage.setLayoutData(sideImageData);
		
		GridData ld3 = new GridData(SWT.FILL, SWT.FILL, true, true);
		
		ActionBarWrapper actionBarCompositeSide= ActionBarWrapper.createActionBars(sideImage, null);;
        
		plotSystem3.createPlotPart(sideImage, "Side Image", actionBarCompositeSide, PlotType.IMAGE, null);
		plotSystem3.getPlotComposite().setLayoutData(ld3);
		
		plotSystem3.getAxis("X-Axis").setTitle("");
	

	//////////////////////////////////////////////////////////////////////////////////////////////

        left.setWeights(new int[]{50,50});         
		right.setWeights(new int[]{50,50});  
        
		
//		left.addControlListener(new ControlListener() {
//			
//			@Override
//			public void controlResized(ControlEvent e) {
//				right.setWeights(left.getWeights());  
//				
//			}
//			
//			@Override
//			public void controlMoved(ControlEvent e) {
//				right.setWeights(left.getWeights());  
//				
//			}
//		});
		
		try {
			verticalSlice = plotSystem2.createRegion("Vertical Slice", RegionType.XAXIS);
			
			
			int[] ad = image2.getShape();
			
			horizontalSlice = plotSystem2.createRegion("Horizontal Slice", RegionType.YAXIS);
			
			RectangularROI horizROI = new RectangularROI(0,(int) Math.round(ad[1]/2),ad[0],(int) Math.round(ad[1]*0.5),0);
//			RectangularROI horizROI = new RectangularROI(0,(int) Math.round(ad[1]/2),50,10,0);
			horizontalSlice.setROI(horizROI);
			horizontalSlice.setActive(true);
			horizontalSlice.setMobile(true);
			
			RectangularROI vertROI = new RectangularROI((int) Math.round(ad[0]/2),0,(int) Math.round(ad[0]*0.5),ad[1],0);
			verticalSlice.setROI(vertROI);
			
			plotSystem2.addRegion(horizontalSlice);
			plotSystem2.addRegion(verticalSlice);
			plotSystem2.addRegion(region);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		///////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////
		/////////////////////// Background slice
		/////////////////////////////////////////////////////////////////////////////////// viewer//////////////////////////////////////
		///////////////// Keywords: cross hairs viewer, image
		/////////////////////////////////////////////////////////////////////////////////// examiner/////////////////////////////
		////////////////////////////////////////////////////////////////////////
		
//		IRegion[] regionArray = new IRegion[2];
//		
//		
//		plotSystem2.getRegions().toArray(regionArray);
		
		horizontalSlice.addROIListener(new IROIListener() {

			@Override
			public void roiSelected(ROIEvent evt) {
				generalUpdate();
			}

			@Override
			public void roiDragged(ROIEvent evt) {
				generalUpdate();
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				generalUpdate();

			}

		});
				
		///////////////////////////////////////////////////////////////////////////////
		verticalSlice.addROIListener(new IROIListener() {

			@Override
			public void roiSelected(ROIEvent evt) {
				generalUpdate();
			}

			@Override
			public void roiDragged(ROIEvent evt) {
				generalUpdate();
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				generalUpdate();

			}

		});

		/////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////// Background Curves in cross sections
		///////////////////////////////////////////////////////////////////////////////////////// Plotsystem2////////
		/////////////////////////////////////////// keywords: background
		///////////////////////////////////////////////////////////////////////////////////////// display//////////////////////////////////////////////

//		backgroundButton.addSelectionListener(new SelectionListener() {
//
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				generalUpdate();
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//
//			}
//		});

		///////////////////////////////////////////////////////////////////////////////////////
		
//		generalUpdate();
	   }
	   
	   public Composite getComposite(){   	
	   	return this;
	   }
	   
	   public IPlottingSystem<Composite> getPlotSystem1(){
		   return plotSystem1;
	   }
	   
	   
	   public IPlottingSystem<Composite> getPlotSystem2(){
		   return plotSystem2;
	   }
	   
	   public IPlottingSystem<Composite> getPlotSystem3(){
		   return plotSystem3;
	   }

	   public IDataset getImage(){
		   return image2;
	   }
	   
	   public void setData(IDataset input){
		   this.image2 = input;
	   }
	   
	   
	   public void setImage(IDataset input){
		   plotSystem2.updatePlot2D(input,  null, null);
		   setData(input);
	   }
	   
	   public void plotSystem2Redraw(){
		   plotSystem2.repaint();
	   }
	   
		public void updateImage(IDataset image){
			plotSystem2.updatePlot2D(image, null, null);
			setImage(image);
		}
		
		
		public IRegion[] getRegions(){
			IRegion[] hv = new IRegion[] {horizontalSlice, verticalSlice};
			return hv;
		}
		
		public SashForm getRight(){
			return right;
			
		}
		
		public SashForm getLeft(){
			return left;
			
		}
		
//		public Button getBackgroundButton(){
//			return backgroundButton;
//		}

		public MultipleOutputCurvesTableView getOutputCurves(){
			return outputCurves;
		}
		
		
		public void generalUpdate(){
		
			
				plotSystem1.clear();
				plotSystem3.clear();
				
				int[][] lenPt = ssp.getLenPt();
				
				
				IRectangularROI greenRectangle = new RectangularROI(lenPt[1][0], lenPt[1][1],
																	 lenPt[0][0], lenPt[0][1], 0);
					
				
				int selection = ssp.getSliderPos();
						
				Dataset subImage =ssp.subImage(selection,greenRectangle);
				
				plotSystem2.updatePlot2D(subImage, null, null);
//				plotSystem2.createPlot2D(subImage, null, null);
				
				ILineTrace lt1 = VerticalHorizontalSlices.horizontalslice(
								horizontalSlice.getROI().getBounds(),
								plotSystem1, 
								subImage, 
								greenRectangle);
				
				plotSystem1.addTrace(lt1);
				
				ILineTrace lt2 = VerticalHorizontalSlices.verticalslice(
								verticalSlice.getROI().getBounds(), 
								subImage,
								plotSystem3, 
								greenRectangle);
				
				plotSystem3.addTrace(lt2);	
				
				
				
				
					@SuppressWarnings("unchecked")
					IDataset output = ssp.presenterDummyProcess(selection,
							ssp.getImage(selection),
							ssvs.getPlotSystemCompositeView().getPlotSystem(),
							3);
			
					ILineTrace lt3 = VerticalHorizontalSlices.horizontalsliceBackgroundSubtracted(
							horizontalSlice.getROI().getBounds(),
							plotSystem1, 
							ssp.getTemporaryBackground(),
							greenRectangle);
//					
					ILineTrace lt4 = VerticalHorizontalSlices.verticalsliceBackgroundSubtracted(
							verticalSlice.getROI().getBounds(),
							getPlotSystem3(),
							ssp.getTemporaryBackground(),
							greenRectangle);
//					
//					
					ILineTrace lt5 = VerticalHorizontalSlices.horizontalsliceBackgroundSubtracted(
							horizontalSlice.getROI().getBounds(),
							plotSystem1, 
							output,
							greenRectangle);
//					
					ILineTrace lt6 = VerticalHorizontalSlices.verticalsliceBackgroundSubtracted(
							verticalSlice.getROI().getBounds(),
							plotSystem3,
							output,
							greenRectangle);
//						
					lt3.setName("background Slice");
					lt4.setName("background Slice");
//					
					plotSystem1.addTrace(lt3);
					plotSystem3.addTrace(lt4);
					plotSystem1.addTrace(lt5);
					plotSystem3.addTrace(lt6);
//					
				
//				
				plotSystem1.clearAnnotations();
				plotSystem3.clearAnnotations();
				
				plotSystem1.autoscaleAxes();
				plotSystem3.autoscaleAxes();
//		
				plotSystem1.repaint();
				plotSystem3.repaint();
				
				return;
		}
		
		
		public void generalUpdate(int[][] lenPt){
			
			
			plotSystem1.clear();
			plotSystem3.clear();
			
//			int[][] lenPt = ssp.getLenPt();
//			
			
			IRectangularROI greenRectangle = new RectangularROI(lenPt[1][0], lenPt[1][1],
																 lenPt[0][0], lenPt[0][1], 0);
				
			
			int selection = ssp.getSliderPos();
					
			Dataset subImage =ssp.subImage(selection,greenRectangle);
			
			plotSystem2.updatePlot2D(subImage, null, null);
//			plotSystem2.createPlot2D(subImage, null, null);
			
			ILineTrace lt1 = VerticalHorizontalSlices.horizontalslice(
							horizontalSlice.getROI().getBounds(),
							plotSystem1, 
							subImage, 
							greenRectangle);
			
			plotSystem1.addTrace(lt1);
			
			ILineTrace lt2 = VerticalHorizontalSlices.verticalslice(
							verticalSlice.getROI().getBounds(), 
							subImage,
							plotSystem3, 
							greenRectangle);
			
			plotSystem3.addTrace(lt2);	
			
			
			
			
				@SuppressWarnings("unchecked")
				IDataset output = ssp.presenterDummyProcess(selection,
						ssp.getImage(selection),
						ssvs.getPlotSystemCompositeView().getPlotSystem(),
						3);
		
				ILineTrace lt3 = VerticalHorizontalSlices.horizontalsliceBackgroundSubtracted(
						horizontalSlice.getROI().getBounds(),
						plotSystem1, 
						ssp.getTemporaryBackground(),
						greenRectangle);
//				
				ILineTrace lt4 = VerticalHorizontalSlices.verticalsliceBackgroundSubtracted(
						verticalSlice.getROI().getBounds(),
						getPlotSystem3(),
						ssp.getTemporaryBackground(),
						greenRectangle);
//				
//				
				ILineTrace lt5 = VerticalHorizontalSlices.horizontalsliceBackgroundSubtracted(
						horizontalSlice.getROI().getBounds(),
						plotSystem1, 
						output,
						greenRectangle);
//				
				ILineTrace lt6 = VerticalHorizontalSlices.verticalsliceBackgroundSubtracted(
						verticalSlice.getROI().getBounds(),
						plotSystem3,
						output,
						greenRectangle);
//					
				lt3.setName("background Slice");
				lt4.setName("background Slice");
//				
				plotSystem1.addTrace(lt3);
				plotSystem3.addTrace(lt4);
				plotSystem1.addTrace(lt5);
				plotSystem3.addTrace(lt6);
//				
			
//			
			plotSystem1.clearAnnotations();
			plotSystem3.clearAnnotations();
			
			plotSystem1.autoscaleAxes();
			plotSystem3.autoscaleAxes();
//	
			plotSystem1.repaint();
			plotSystem3.repaint();
			
			return;
	}
}
		
		
		
		

