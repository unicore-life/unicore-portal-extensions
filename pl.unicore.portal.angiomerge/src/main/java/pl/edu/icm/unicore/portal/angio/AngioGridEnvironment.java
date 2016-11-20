/*
 * Copyright (c) 2015 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.angio;

import eu.unicore.griddisco.core.model.BrokerService;
import eu.unicore.griddisco.core.model.StorageFactoryService;

/**
 * Stores information about required grid settings and resources required for AngioMerge job submission.
 * @author K. Benedyczak
 */
public class AngioGridEnvironment
{
	public final BrokerService broker;
	public final StorageFactoryService sfs;
	
	public AngioGridEnvironment(BrokerService broker, StorageFactoryService sfs)
	{
		this.broker = broker;
		this.sfs = sfs;
	}
}
