/*
 * Copyright (c) 2015 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.openfoam;

import eu.unicore.griddisco.core.api.ChangeEvent;
import eu.unicore.griddisco.core.api.ChangeType;
import eu.unicore.griddisco.core.api.ServiceListener;
import eu.unicore.griddisco.core.api.UserGridDiscovery;
import eu.unicore.griddisco.core.api.filter.TrueFilter;
import eu.unicore.griddisco.core.model.BrokerService;
import eu.unicore.griddisco.core.model.GridResource;
import eu.unicore.griddisco.core.model.StorageFactoryService;
import org.apache.log4j.Logger;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects information about required resources in the Grid to submit AngioMerge jobs.
 * @author K. Benedyczak
 */
public class GridEnvironmentCollector
{
	private static final Logger log = Logger.getLogger(GridEnvironmentCollector.class);

	private Set<BrokerService> brokers = new LinkedHashSet<>();
	private Set<StorageFactoryService> sfses = new LinkedHashSet<>();
	private ServiceListener listener;
	private UserGridDiscovery discovery;
	private GridEnvironmentListener externalListener;
	
	public GridEnvironmentCollector(UserGridDiscovery discovery, GridEnvironmentListener externalListener)
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
	
	private void updateStatus()
	{
		BrokerService broker = brokers.isEmpty() ? null : brokers.iterator().next();
		StorageFactoryService sfs = sfses.isEmpty() ? null : sfses.iterator().next();
		externalListener.update(broker, sfs);
	}
	
	public interface GridEnvironmentListener
	{
		void update(BrokerService broker, StorageFactoryService sfs);
	}
}
