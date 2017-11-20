package pl.edu.icm.unicore.portal.openfoam;

import java.util.List;

import org.apache.log4j.Logger;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStagingType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.unigrids.x2006.x04.services.jms.SubmissionTimeDocument;
import org.unigrids.x2006.x04.services.jms.TargetSystemReferenceDocument;
import org.unigrids.x2006.x04.services.jms.JobPropertiesDocument.JobProperties;

import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.portal.grid.ui.helpers.BrokeredJobSubmissionHelper;

public class OpenFOAMJSDLCreator 
{
	private static final Logger log = Logger.getLogger(OpenFOAMJSDLCreator.class);
	
	public static final String APPLICATON_NAME = "OpenFOAM";
	private static final String APPLICATION_VERSION = "2.2.2";
	
	public static final String INPUT = "input.txt";
	public static final String OUTPUT = "output.txt";
	
	public static OpenFOAMJobSpecification createJobSpecification(JobProperties jobProperties) 
	{
		OpenFOAMJobSpecification ret = new OpenFOAMJobSpecification();
		
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
}
