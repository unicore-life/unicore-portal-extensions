/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.angio;

import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.portal.grid.ui.helpers.BrokeredJobSubmissionHelper;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStagingType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobIdentificationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ResourcesType;
import org.unigrids.x2006.x04.services.jms.JobPropertiesDocument.JobProperties;
import org.unigrids.x2006.x04.services.jms.SubmissionTimeDocument;
import org.unigrids.x2006.x04.services.jms.TargetSystemReferenceDocument;

import java.util.List;


/**
 * Preparation of the job description in UNICORE format. Imports/exports are not added.
 * @author K. Benedyczak
 */
public class AngioMergeJSDLCreator
{
	private static final Logger log = Logger.getLogger(AngioMergeJSDLCreator.class);

	public static final String APPLICATON_NAME = "AngioMerge";
	private static final String APPLICATION_VERSION = "1.0";
	
	/**
	 * Name of the main input file in the job's working directory and in portal workspace.
	 */
	public static final String INPUT = "input.zip";

	/**
	 * Name of the main output file in the job's working directory and in portal output folder after download.
	 */
	public static final String OUTPUT = "output.zip";

	/**
	 * Portal workspace directory where job output is fetched.
	 */
	public static final String OUTPUT_DIRECTORY = "output";
	
	
	public static AngioJobSpecification createJobSpecification(JobProperties jobProperties)
	{
		AngioJobSpecification ret = new AngioJobSpecification();
		JobDescriptionType jobDesc = jobProperties.getOriginalJSDL().getJobDescription();
		ret.setName(jobDesc.getJobIdentification().getJobName());
		String[] projects = jobDesc.getJobIdentification().getJobProjectArray();
		if (projects != null && projects.length > 0)
			ret.setProject(projects[0]);
		String[] tags = jobDesc.getJobIdentification().getJobAnnotationArray();
		for (String tag: tags)
		{
			if (tag.startsWith("INPUT_ID="))
				ret.setInputId(tag.split("=", 2)[1]);
			if (tag.startsWith(BrokeredJobSubmissionHelper.OUTPUT_STOREAGE_URL))
				ret.setGridOutputLocation(tag.split("=", 2)[1]);
		}
		DataStagingType[] dss = jobDesc.getDataStagingArray();
		for (DataStagingType ds: dss)
		{
			//this is dead... SO removes all exports :[
			if (ds.getTarget() != null && ds.getFileName() != null && ds.getFileName().equals(OUTPUT))
			{
				ret.setGridOutputLocation(ds.getTarget().getURI());
			}
		}
		
		List<SubmissionTimeDocument> tt = WSUtilities.extractAnyElements(
				jobProperties, SubmissionTimeDocument.class);
		if (tt != null && tt.size() > 0)
			ret.setExecutionStart(tt.get(0).getSubmissionTime().getTime());
		
		List<TargetSystemReferenceDocument> tt2 = WSUtilities.extractAnyElements(
				jobProperties, TargetSystemReferenceDocument.class);
		if (tt2 != null && tt2.size() > 0)
			ret.setExecutionSite(tt2.get(0).getTargetSystemReference().getAddress().getStringValue());
		return ret;
	}


	public static JobDefinitionDocument createJobDocument(AngioJobSpecification jobSpec) 
	{
		JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.newInstance();
		JobDefinitionType jobDefinition = jobDefinitionDocument.addNewJobDefinition();
		JobDescriptionType jobDesc = jobDefinition.addNewJobDescription();
		
		ApplicationDocument ad=ApplicationDocument.Factory.newInstance();
		ApplicationType app=ad.addNewApplication();
		app.setApplicationName(APPLICATON_NAME);
		app.setApplicationVersion(APPLICATION_VERSION);
		
		ResourcesType resReq = jobDesc.addNewResources();
		resReq.addNewIndividualPhysicalMemory().addNewExact().setStringValue("1000000000");
		resReq.addNewIndividualCPUCount().addNewExact().setStringValue("1");
		double walltime = 1 * 3600.0;
		resReq.addNewIndividualCPUTime().addNewExact().setStringValue(String.valueOf(Math.round(walltime)));
		
		jobDesc.setApplication(app);
		
		JobIdentificationType jobId = jobDesc.addNewJobIdentification();
		jobId.setJobName(jobSpec.getName());
		if (jobSpec.getProject() != null)
			jobId.setJobProjectArray(new String[] {jobSpec.getProject()});
		jobId.addJobAnnotation("INPUT_ID=" + jobSpec.getInputId());
		
		log.debug("Job description:\n" + jobDefinitionDocument.xmlText(new XmlOptions().setSavePrettyPrint()));
		return jobDefinitionDocument;
	}
}
