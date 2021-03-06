package org.dawnsci.slicing.tools.hyper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.swt.widgets.Composite;

public class HyperPlotViewer extends IPlottingSystemViewer.Stub<Composite> {

	private HyperComponent hyper;
	
	public void createControl(final Composite parent) {
		hyper = new HyperComponent();
		hyper.createControl(parent);
		
		
	}
	
	@Override
	public boolean addTrace(ITrace trace){
		if (trace instanceof IHyperTrace) {
			IHyperTrace h = (IHyperTrace)trace;
			ILazyDataset lazyDataset = h.getLazyDataset();
			int[] order = h.getOrder();
			
			IDataset[] daxes = new IDataset[3];
			
			AxesMetadata md = lazyDataset.getFirstMetadata(AxesMetadata.class);
			
			if (md == null) {
				
				for (int i = 0; i < 3;i++) {
					IDataset d = DatasetFactory.createRange(lazyDataset.getShape()[order[i]]);
					daxes[i] = d;
				}
				
			} else {

				for (int i = 0; i < 3;i++) {
					ILazyDataset[] axis = md.getAxis(order[i]);
					IDataset d = null;
					if (axis == null || axis[0] == null) {
						d = DatasetFactory.createRange(lazyDataset.getShape()[order[i]]);
					} else {
						try {
							//TODO ndAxes
							d = axis[0].getSlice();
							d.squeeze();
							String name = MetadataPlotUtils.removeSquareBrackets(d.getName());
							d.setName(name);
						} catch (DatasetException e) {
							//TODO log
							d = DatasetFactory.createRange(lazyDataset.getShape()[order[i]]);
						}
					}
					
					daxes[i] = d;
				}
				
			}
			
			hyper.setData(lazyDataset, Arrays.asList(daxes), h.getSlice().convertToSlice(), order);
			return true;
		}
		return false;
	}
	
	public Composite getControl() {
		if (hyper == null) return null;
		return (Composite)hyper.getControl();
	}
	
	
	@Override
	public  <U extends ITrace> U createTrace(String name, Class<? extends ITrace> clazz) {
		if (clazz == IHyperTrace.class) {
			HyperTrace hyperTrace = new HyperTrace();
			hyperTrace.setName(name);
			return (U)hyperTrace;
		}
		return null;
	}
	
	@Override
	public boolean isTraceTypeSupported(Class<? extends ITrace> trace) {
		if (IHyperTrace.class.isAssignableFrom(trace)) {
			return true;
		}
		return false;
	}
	
	@Override
	public Collection<Class<? extends ITrace>> getSupportTraceTypes() {
		List<Class<? extends ITrace>> l = new ArrayList<>();
		l.add(IHyperTrace.class);
		return l;
	}
}
