/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.openfoam;

import java.util.Date;


/**
 * Description of a complete input required to prepare the OpenFOAM job.
 * Classic Java Bean.
 * @author K. Benedyczak
 */
public class OpenFOAMJobSpecification
{
	private String name;
	private String project;
	private String inputId;
	private String gridOutputLocation;
	
	private String executionSite;
	private Date executionStart;
	
	public OpenFOAMJobSpecification()
	{
	}

	public OpenFOAMJobSpecification(String name, String project, String inputId,
			String gridOutputLocation)
	{
		this.name = name;
		this.project = project;
		this.inputId = inputId;
		this.gridOutputLocation = gridOutputLocation;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getProject()
	{
		return project;
	}

	public void setProject(String project)
	{
		this.project = project;
	}

	public String getInputId()
	{
		return inputId;
	}

	public void setInputId(String intputId)
	{
		this.inputId = intputId;
	}

	public String getGridOutputLocation()
	{
		return gridOutputLocation;
	}

	public void setGridOutputLocation(String gridOutputLocation)
	{
		this.gridOutputLocation = gridOutputLocation;
	}

	public String getExecutionSite()
	{
		return executionSite;
	}

	public void setExecutionSite(String executionSite)
	{
		this.executionSite = executionSite;
	}

	public Date getExecutionStart()
	{
		return executionStart;
	}

	public void setExecutionStart(Date executionStart)
	{
		this.executionStart = executionStart;
	}
}
