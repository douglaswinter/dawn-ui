package org.dawnsci.datavis.live;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.datavis.model.ILiveFileListener;
import org.dawnsci.datavis.model.ILiveFileService;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.processing.IOperationBean;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPropertyFilter.FilterAction;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.CommandConstants;

public class LiveFileServiceImpl implements ILiveFileService {

	private Set<ILiveFileListener> listeners = new HashSet<>();
	private ISubscriber<EventListener> scanSubscriber;
	private ISubscriber<EventListener> procSubscriber;
	
	private boolean attached = false;
	private IScanListener scanListener;
	private IBeanListener<StatusBean> beanListener;
	
	private static long MIN_REFRESH_TIME = 2000;
	
	private UpdateJob job;
	
	@Override
	public void addLiveFileListener(ILiveFileListener l) {
		if (!attached) attach();
		listeners.add(l);
	}

	@Override
	public void removeLiveFileListener(ILiveFileListener l) {
		listeners.remove(l);
		if (listeners.isEmpty()) {
			detach();
		}
		
	}
	
	@Override
	public void runUpdate(Runnable runnable) {
		if (job == null) {
			job = new UpdateJob("Update Live Data");
			job.setPriority(Job.INTERACTIVE);
		}
		job.setRunnable(runnable);
		job.schedule();
	}

	private void attach() {
		IEventService eService = ServiceManager.getIEventService();
		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri==null) return; // Nothing to start, standard DAWN.

		
		try {
			final URI uri = new URI(suri);
			scanSubscriber = eService.createSubscriber(uri, EventConstants.STATUS_TOPIC);
			
			// We don't care about the scan request, removing it means that
			// all the points models and detector models to not have to resolve in
			// order to get the event.
			scanSubscriber.addProperty("scanRequest", FilterAction.DELETE); 
			scanSubscriber.addProperty("position", FilterAction.DELETE); 	
			
			if (scanListener == null) scanListener = new ScanListener();
			
			scanSubscriber.addListener(scanListener);
			
			procSubscriber = eService.createSubscriber(uri, "scisoft.operation.STATUS_TOPIC");
			
			if (beanListener == null) beanListener = new BeanListener();
			
			procSubscriber.addListener(beanListener);
			
//			logger.info("Created subscriber");
			
		} catch (URISyntaxException | EventException e) {
//			logger.error("Could not subscribe to the event service", e);
		}
	}
	
	private void detach() {
		
		if (scanSubscriber != null && scanListener != null) scanSubscriber.removeListener(scanListener);
		if (procSubscriber != null && beanListener != null) procSubscriber.removeListener(beanListener);
		
		attached = false;
	}
	
	private void fireListeners(LoadedFile f) {
		for (ILiveFileListener l : listeners) l.fileLoaded(f);
	}
	
	public static String getDataServerHost() {
		String name = System.getProperty("org.eclipse.dawnsci.data.server.host");
		if (name==null) name = System.getProperty("GDA/gda.dataserver.host");
		if (name==null) name = System.getProperty("gda.dataserver.host");
		return name;
	}

	// TODO put this in global place?
	public static int getDataServerPort() {
		int port = Integer.getInteger("org.eclipse.dawnsci.data.server.port", -1);
		if (port<=0) port = Integer.getInteger("GDA/gda.dataserver.port", -1);
		if (port<=0) port = Integer.getInteger("gda.dataserver.port", -1);
		return port;
	}
	
	private class UpdateJob extends Job {

		private final AtomicReference<Runnable> task =new AtomicReference<Runnable>();
		
		public UpdateJob(String name) {
			super(name);
		}
		
		public void setRunnable(Runnable runnable) {
			this.task.set(runnable);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Runnable local = task.getAndSet(null);
			if (local == null) return org.eclipse.core.runtime.Status.OK_STATUS;
			local.run();


			try {
				Thread.sleep(MIN_REFRESH_TIME);
			} catch (InterruptedException e) {
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}


			return org.eclipse.core.runtime.Status.OK_STATUS;
		}
		
	}
	
	private class ScanListener implements IScanListener {
		
		public void scanEventPerformed(ScanEvent evt) {
			for (ILiveFileListener l : listeners) {
				l.refreshRequest();
			}
		}
		
		@Override
		public void scanStateChanged(ScanEvent event) {
			
			ScanBean beanNoScanReq = event.getBean();
			
			if (beanNoScanReq.getStatus().equals(Status.RUNNING)) {
				for (ILiveFileListener l : listeners) {
					l.refreshRequest();
				}
			}
			
			final String filePath = beanNoScanReq.getFilePath();
			// Scan started
			if (beanNoScanReq.scanStart() == true) {

				// Recent change to GDA means that its configuration may be read without
				// making a dependency on it.
				String host = getDataServerHost();
				int port    = getDataServerPort();
				

				LiveLoadedFile f = new LiveLoadedFile(filePath, host, port);
				
				fireListeners(f);

			}
			
			if (beanNoScanReq.scanEnd() == true) {

				for (ILiveFileListener l : listeners) l.localReload(filePath);
			}
		}
		
	}
	
	private class BeanListener implements IBeanListener<StatusBean> {
		
		@Override
		public void beanChangePerformed(BeanEvent<StatusBean> evt) {
			
			if (!(evt.getBean() instanceof IOperationBean)) return;
			
			StatusBean bean = evt.getBean();
			
			if (Status.RUNNING.equals(bean.getStatus()) && !Status.RUNNING.equals(bean.getPreviousStatus())) {
				String host = getDataServerHost();
				int port    = getDataServerPort();
				

				LiveLoadedFile f = new LiveLoadedFile(((IOperationBean)evt.getBean()).getOutputFilePath(), host, port);
				
				fireListeners(f);
				
				return;
				
			}
			
			if (Status.RUNNING.equals(bean.getStatus())) {
				for (ILiveFileListener l : listeners) {
					l.refreshRequest();
				}
			}
			
			
			if (!evt.getBean().getStatus().isFinal()) return;
			if (evt.getBean() instanceof IOperationBean) {

				for (ILiveFileListener l : listeners) l.localReload(((IOperationBean)evt.getBean()).getOutputFilePath());

			}
		}
		
		public Class<StatusBean> getBeanClass() {
			return StatusBean.class;
		}
	}

}
