package org.dawnsci.mapping.ui.datamodel;

import java.util.Arrays;

import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;

public class MapScanDimensions {

	
	private int xDim;
	private int yDim;
	private int[] nonXYScanDimensions;
	private int[] nonXYDimensionValues;
	private int scanRank;
	
	public MapScanDimensions(int xDim, int yDim, int scanRank) {
		this.xDim = xDim;
		this.yDim = yDim;
		this.scanRank = scanRank;
		
		//nonXYScanDimensions null when there are none
		if (xDim == yDim && scanRank != 1) {
			nonXYScanDimensions = new int[scanRank-1];
			nonXYDimensionValues = nonXYScanDimensions.clone();
		} else if (xDim != yDim && scanRank != 2) {
			nonXYScanDimensions = new int[scanRank-2];
			nonXYDimensionValues = nonXYScanDimensions.clone();
		}
		
		if (nonXYScanDimensions != null) {
			initialiseNonXScanValues();
		}
		
	}
	
	private void initialiseNonXScanValues(){
		
		for (int i = 0, j = 0; i < scanRank; i++) {
			if (i == xDim || i == yDim || j > nonXYDimensionValues.length) continue;
			nonXYScanDimensions[j++] = i;
		}
		
		Arrays.fill(nonXYDimensionValues, 0);
		
	}
	
	public int getxDim() {
		return xDim;
	}

	public int getyDim() {
		return yDim;
	}

	public int[] getNonXYScanDimensions() {
		return nonXYScanDimensions == null ? null : nonXYScanDimensions.clone();
	}

	public int getScanRank() {
		return scanRank;
	}

	public void updateNonXYScanSlice(int dim, int value) {
		
		for (int i = 0; i < nonXYScanDimensions.length; i++) {
			if (dim == nonXYScanDimensions[i]) {
				nonXYDimensionValues[i] = value;
			}
		}
	}
	
	public void updateNonXYScanSlice(int[] shape) {
		
		if (nonXYScanDimensions == null) return;
		
		for (int i = 0 ; i < shape.length; i++) {
			if (i == xDim || i == yDim) continue;
			updateNonXYScanSlice(i, shape[i]-1);
		}
		
	}
	
	
	public void changeXandYdims(int xDim, int yDim) {
		this.xDim = xDim;
		this.yDim = yDim;
		initialiseNonXScanValues();
		
	}
	
	public boolean isPointDetector(int[] shape){
		if (shape.length == scanRank) return true;
		
		if (scanRank < shape.length ) {
			boolean allOnes = true;
			for (int i = scanRank; i < shape.length; i++) {
				if (shape[i] != 1) {
					allOnes = false;
					break;
				}
			}
			
			return allOnes;
		}
		
		return false;
	}
	
	public boolean isRemappingRequired(){
		return xDim == yDim;
	}
	
	public boolean isTransposed(){
		return yDim > xDim;
	}
	
	public boolean isMapDimension(int i){
		return i == xDim || i == yDim;
	}
	
	private boolean isNonMapScanDimension(int dim) {
		if (nonXYScanDimensions == null) return false;
		boolean result = false;
		for (int i = 0; i < nonXYScanDimensions.length; i++) {
			if (dim == nonXYScanDimensions[i]) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	public SliceND getSlice(int mapXindex, int mapYindex, int[] shape) {
		SliceND slice = new SliceND(shape);
		slice.setSlice(xDim, mapXindex, mapXindex+1, 1);
		slice.setSlice(yDim, mapYindex, mapYindex+1, 1);
		
		if (shape.length == scanRank) return slice;
		
		int[] s = slice.getShape();
		
		if (ShapeUtils.squeezeShape(s, false).length == 1){
			//no point in reducing from 1D
			return slice;
		}
		
		if (nonXYScanDimensions != null) {
			
			int n = nonXYScanDimensions.length;
			
			if (shape.length == scanRank) {
				//always return at least 1D
				n--;
			}
			
			for (int i = 0; i < n ; i++) {
				slice.setSlice(nonXYScanDimensions[i], nonXYDimensionValues[i], nonXYDimensionValues[i]+1, 1);
			}
			
		}
			
		return slice;	
	}
	
	public SliceND getMapSlice(ILazyDataset map){
		
		if (nonXYScanDimensions == null) {
			if (map.getRank() != 2) {
				
				SliceND s = new SliceND(map.getShape());
				
				for (int i = 0; i < map.getRank(); i++){ 
					if (!isMapDimension(i)) s.setSlice(i, 0, 1, 1);
				}
				
				return s;
			}
			
			return new SliceND(map.getShape());
		}
			
		SliceND s = new SliceND(map.getShape());

		int n = nonXYScanDimensions.length;

		for (int i = 0; i < n ; i++) {
			s.setSlice(nonXYScanDimensions[i], nonXYDimensionValues[i], nonXYDimensionValues[i]+1, 1);
		}

		return s;
		
	}
	
	public void setMapAxes(ILazyDataset block, ILazyDataset map) {
		
		try {
			SliceND slice = new SliceND(block.getShape());
			
			for (int i = 0; i < block.getRank(); i++) {
				if (!(isMapDimension(i) || isNonMapScanDimension(i))) slice.setSlice(i, 0, 1, 1);
			}
			
			ILazyDataset sv = block.getSliceView(slice);
			sv.setShape(map.getShape());
			AxesMetadata svAx = sv.getFirstMetadata(AxesMetadata.class);
			
			map.setMetadata(svAx);
			
		} catch (Exception e) {
			//FIXME log
			e.printStackTrace();
		}
		
	}
	
	public int[] getDataDimensions(int dataRank) {
		int[] dataDims = new int[dataRank - scanRank];
		
		for (int i = 0,j = 0; i < dataRank; i++) {
			if (!isMapDimension(i) && !isNonMapScanDimension(i)){
				dataDims[j++] = i;
			}
		}
		
		return dataDims;
	}
}
