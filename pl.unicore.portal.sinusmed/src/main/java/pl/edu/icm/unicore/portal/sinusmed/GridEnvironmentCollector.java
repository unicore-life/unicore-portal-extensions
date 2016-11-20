/*
 * Copyright (c) 2015 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed;

import eu.unicore.griddisco.core.api.ChangeEvent;
import eu.unicore.griddisco.core.api.ChangeType;
import eu.unicore.griddisco.core.api.ServiceListener;
import eu.unicore.griddisco.core.api.UserGridDiscovery;
import eu.unicore.griddisco.core.api.filter.TrueFilter;
import eu.unicore.griddisco.core.model.BrokerService;
import eu.unicore.griddisco.core.model.GridResource;
import eu.unicore.griddisco.core.model.StorageFactoryService;
import eu.unicore.griddisco.core.model.TargetSystemFactoryService;
import org.apache.log4j.Logger;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType;
import org.unigrids.x2006.x04.services.tss.ApplicationResourceType;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects information about required resources in the Grid to submit SinusMed jobs.
 * @author K. Benedyczak
 */
public class GridEnvironmentCollector
{
	private static final Logger log = Logger.getLogger(GridEnvironmentCollector.class);

	private Set<BrokerService> brokers = new LinkedHashSet<>();
	private Set<StorageFactoryService> sfses = new LinkedHashSet<>();
	private Set<TargetSystemFactoryService> tsfs = new LinkedHashSet<>();
	private int maxCpus = -1;
	private ServiceListener listener;
	private UserGridDiscovery discovery;
	private SinusMedEnvironmentListener externalListener;
	
	public GridEnvironmentCollector(UserGridDiscovery discovery, SinusMedEnvironmentListener externalListener)
	{
		this.discovery = discovery;
		this.externalListener = externalListener;
		listener = new ServiceListener()
		{
			@Override
			public void serviceChanged(ChangeEvent changeEvent)
			{
				GridEnvironmentCollector.this.serviceChanged(changeEvent);	
			}
		};
	}
	
	public void stop()
	{
		discovery.removeServiceListener(listener);
	}

	public void start()
	{
		discovery.addServiceListener(new TrueFilter(), listener, true);
	}
	
	private synchronized void serviceChanged(ChangeEvent changeEvent)
	{
		GridResource changedResource = changeEvent.getChangedResource();
		
		
		if (changedResource instanceof BrokerService)
		{
			if (changeEvent.getChangeType() == ChangeType.ADDED)
				addBroker((BrokerService) changedResource);
			else if (changeEvent.getChangeType() == ChangeType.REMOVED)
				removeBroker((BrokerService) changedResource);
		} else if (changedResource instanceof TargetSystemFactoryService)
		{
			if (changeEvent.getChangeType() == ChangeType.REMOVED)
				removeTsf((TargetSystemFactoryService) changedResource);
			else
				updateTsf((TargetSystemFactoryService) changedResource);
		} else if (changedResource instanceof StorageFactoryService)
		{
			if (changeEvent.getChangeType() == ChangeType.REMOVED)
				removeSfs((StorageFactoryService) changedResource);
			else if (changeEvent.getChangeType() == ChangeType.ADDED)
				addSfs((StorageFactoryService) changedResource);
		}
	}
	
	private void addBroker(BrokerService broker)
	{
		log.debug("Broker found");
		if (brokers.add(broker))
			updateStatus();
	}

	private void removeBroker(BrokerService broker)
	{
		if (brokers.remove(broker))
			updateStatus();
	}
	
	private void addSfs(StorageFactoryService sfs)
	{
		log.debug("SFS found");
		if (sfses.add(sfs))
			updateStatus();
	}

	private void removeSfs(StorageFactoryService sfs)
	{
		if (sfses.remove(sfs))
			updateStatus();
	}
	
	private void updateTsf(TargetSystemFactoryService tsf)
	{
		if (supportsSinusMed(tsf))
		{
			log.debug("TSF found");
			if (tsfs.add(tsf))
				recalculateResources();
		} else
		{
			removeTsf(tsf);
		}
	}
	
	private void removeTsf(TargetSystemFactoryService tsf)
	{
		if (tsfs.remove(tsf))
			recalculateResources();
	}
	
	private void recalculateResources()
	{
		int newMaxCpus = 1;
		for (TargetSystemFactoryService tsf: tsfs)
		{
			RangeValueType individualCPUCount = tsf.getProperties().getIndividualCPUCount();
			if (individualCPUCount == null)
				continue;
			int maxCpus = getMaxCpus(individualCPUCount);
			if (newMaxCpus < maxCpus)
				newMaxCpus = maxCpus;
		}
		if (newMaxCpus != this.maxCpus)
		{
			this.maxCpus = newMaxCpus;
			updateStatus();
		}
	}
	
	private int getMaxCpus(RangeValueType individualCPUCount)
	{
		if (individualCPUCount.getUpperBoundedRange() != null)
			return (int) individualCPUCount.getUpperBoundedRange().getDoubleValue();
		if (individualCPUCount.getExactArray() != null && individualCPUCount.getExactArray().length > 0)
			return (int) individualCPUCount.getExactArray()[0].getDoubleValue();
		return 1;
	}
	
	private boolean supportsSinusMed(TargetSystemFactoryService tsf)
	{
		ApplicationResourceType[] applications = tsf.getProperties().getApplicationResourceArray();
		if (applications != null)
		{
			for (ApplicationResourceType app: applications)
			{
				if (app.getApplicationName().equals(SinusMedJSDLCreator.SINUS_APPLICATON_NAME))
					return true;
			}
		}
		return false;
	}
	
	private void updateStatus()
	{
		if (!brokers.isEmpty() && !tsfs.isEmpty() && !sfses.isEmpty())
		{
			externalListener.gridReady(brokers.iterator().next(), maxCpus, sfses.iterator().next());
		} else
		{
			externalListener.gridNotReady();
		}
	}
	
	public interface SinusMedEnvironmentListener
	{
		void gridReady(BrokerService broker, int maxCpus, StorageFactoryService sfs);
		void gridNotReady();
	}
}
