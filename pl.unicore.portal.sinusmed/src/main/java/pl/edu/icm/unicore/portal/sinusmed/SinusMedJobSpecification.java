/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed;

import java.util.Date;


/**
 * Description of a complete input required to prepare the sinusmed job.
 * Classic Java Bean.
 * @author K. Benedyczak
 */
public class SinusMedJobSpecification
{
	private String name;
	private String atlas;
	private String project;
	private JobProfile execProfile;
	private String inputId;
	private String gridInputLocation;
	private String gridOutputLocation;
	
	private String executionSite;
	private Date executionStart;
	
	public SinusMedJobSpecification()
	{
	}

	public SinusMedJobSpecification(String name, String atlas, String project,
			JobProfile execProfile, String inputId, String gridInputLocation, String gridOutputLocation)
	{
		this.name = name;
		this.atlas = atlas;
		this.project = project;
		this.execProfile = execProfile;
		this.inputId = inputId;
		this.gridInputLocation = gridInputLocation;
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

	public String getAtlas()
	{
		return atlas;
	}

	public void setAtlas(String atlas)
	{
		this.atlas = atlas;
	}

	public String getProject()
	{
		return project;
	}

	public void setProject(String project)
	{
		this.project = project;
	}

	public JobProfile getExecProfile()
	{
		return execProfile;
	}

	public void setExecProfile(JobProfile execProfile)
	{
		this.execProfile = execProfile;
	}

	public String getInputId()
	{
		return inputId;
	}

	public void setInputId(String inputId)
	{
		this.inputId = inputId;
	}

	public String getGridInputLocation()
	{
		return gridInputLocation;
	}

	public void setGridInputLocation(String gridInputLocation)
	{
		this.gridInputLocation = gridInputLocation;
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
