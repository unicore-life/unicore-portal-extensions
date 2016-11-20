/*
 * Copyright (c) 2015 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed;

import eu.unicore.griddisco.core.model.BrokerService;
import eu.unicore.griddisco.core.model.StorageFactoryService;

/**
 * Stores information about required grid settings and resources required for Sinusmed job submission.
 * @author K. Benedyczak
 */
public class SinusMedGridEnvironment
{
	public final int ncpus;
	public final BrokerService broker;
	public final StorageFactoryService sfs;
	
	public SinusMedGridEnvironment(int ncpus, BrokerService broker, StorageFactoryService sfs)
	{
		this.ncpus = ncpus;
		this.broker = broker;
		this.sfs = sfs;
	}
}
