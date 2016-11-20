/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed;

import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.portal.grid.ui.helpers.BrokeredJobSubmissionHelper;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStagingType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobIdentificationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ResourcesType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.EnvironmentType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.POSIXApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.POSIXApplicationType;
import org.unigrids.x2006.x04.services.jms.JobPropertiesDocument.JobProperties;
import org.unigrids.x2006.x04.services.jms.SubmissionTimeDocument;
import org.unigrids.x2006.x04.services.jms.TargetSystemReferenceDocument;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Preparation of the job description in UNICORE format. Imports/exports are not added.
 * @author K. Benedyczak
 */
public class SinusMedJSDLCreator
{
	private static final Logger log = Logger.getLogger(SinusMedJSDLCreator.class);

	public static final String SINUS_APPLICATON_NAME = "SinusMED";
	private static final String SINUS_APPLICATION_VERSION = "0.3";
	/**
	 * Name of the main input file in the job's working directory.
	 */
	public static final String INPUT = "sinusmed-input";
	/**
	 * Name of the main output file in the job's working directory.
	 */
	public static final String OUTPUT = "output.zip";
	/**
	 * Name of the output file in the job's working directory containing packed input, 
	 * after conversion to the raw format.
	 */
	public static final String CONVERTED_INPUT = "input.zip";
	
	/**
	 * The map indexed with 
	 */
	public static final double[] WALLTIME_COEFF = {0.5, 2, 8, 12};
	
	public static SinusMedJobSpecification createSinusmedJobSpecification(JobProperties jobProperties)
	{
		SinusMedJobSpecification ret = new SinusMedJobSpecification();
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
		ApplicationType app = jobDesc.getApplication();
		QName posixAppNS = new QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "POSIXApplication");
		XmlObject[] posixApps = WSUtilities.extractAnyElements(app, posixAppNS);
		if (posixApps != null && posixApps.length > 0)
		{
			POSIXApplicationType posixApp = ((POSIXApplicationDocument) posixApps[0]).getPOSIXApplication();
			EnvironmentType[] envs = posixApp.getEnvironmentArray();
			for (EnvironmentType env: envs)
			{
				if (env.getName().equals("SinusMED_PREDEFSPEC"))
					ret.setExecProfile(JobProfile.valueOf(env.getStringValue()));
				if (env.getName().equals("SinusMED_ATLAS"))
					ret.setAtlas(env.getStringValue());
			}
		}
		DataStagingType[] dss = jobDesc.getDataStagingArray();
		for (DataStagingType ds: dss)
		{
			if (ds.getSource() != null && ds.getFileName() != null && ds.getFileName().equals(
					SinusMedJSDLCreator.INPUT))
			{
				ret.setGridInputLocation(ds.getSource().getURI());
			}

			//this is dead... SO removes all exports :[
			if (ds.getTarget() != null && ds.getFileName() != null && ds.getFileName().equals(
					SinusMedJSDLCreator.OUTPUT))
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


	public static JobDefinitionDocument createSinusmedJobDocument(SinusMedJobSpecification jobSpec, int maxCores) 
	{
		JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.newInstance();
		JobDefinitionType jobDefinition = jobDefinitionDocument.addNewJobDefinition();
		JobDescriptionType jobDesc = jobDefinition.addNewJobDescription();
		
		ApplicationDocument ad=ApplicationDocument.Factory.newInstance();
		ApplicationType app=ad.addNewApplication();
		app.setApplicationName(SINUS_APPLICATON_NAME);
		app.setApplicationVersion(SINUS_APPLICATION_VERSION);
		
		ResourcesType resReq = jobDesc.addNewResources();
		resReq.addNewIndividualPhysicalMemory().addNewExact().setStringValue("4000000000");
		resReq.addNewIndividualCPUCount().addNewExact().setStringValue(String.valueOf(maxCores));
		//we ignore N_CPUS > 8 as then sinusmed doesn't scale that well. However for lower numbers of CPUs
		// we increase the time linearly. 
		double mult = 8.0 / maxCores;
		if (mult < 1)
			mult = 1;
		double walltime = jobSpec.getExecProfile().getCoefficient() * 3600.0 * mult;
		resReq.addNewIndividualCPUTime().addNewExact().setStringValue(String.valueOf(Math.round(walltime)));
		
		POSIXApplicationDocument pd = POSIXApplicationDocument.Factory.newInstance();
		POSIXApplicationType p = pd.addNewPOSIXApplication();
		
		EnvironmentType e1 = p.addNewEnvironment();
		e1.setName("SinusMED_INPUTFILE");
		e1.setStringValue(INPUT);
		EnvironmentType e2 = p.addNewEnvironment();
		e2.setName("SinusMED_ATLAS");
		e2.setStringValue(jobSpec.getAtlas());
		EnvironmentType e3 = p.addNewEnvironment();
		e3.setName("SinusMED_PREDEFSPEC");
		e3.setStringValue(jobSpec.getExecProfile().name());
		
		WSUtilities.append(pd, ad);
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
