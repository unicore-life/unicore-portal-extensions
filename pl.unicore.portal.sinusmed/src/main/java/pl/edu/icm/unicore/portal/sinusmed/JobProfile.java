/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed;

/**
 * Job execution profile specifies how detailed the computations are. The more detailed computations 
 * are longer.
 * @author K. Benedyczak
 */
public enum JobProfile
{
	low(0.5), medium(2), normal(8), high(12);

	private double coefficient;
	
	JobProfile(double coefficient)
	{
		this.coefficient = coefficient;
	}
	
	/**
	 * @return coefficient by which the execution time should be multiplied to obtain the max job walltime.
	 * By default 1 hour is the scaling base, which is for the 8 cores.  
	 */
	public double getCoefficient()
	{
		return coefficient;
	}
}
