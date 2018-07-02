package pl.edu.icm.unicore.portal.openfoam;

import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.jsdl.extensions.ResourceRequestDocument;
import eu.unicore.portal.grid.ui.helpers.BrokeredJobSubmissionHelper;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStagingType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobIdentificationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ResourcesDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ResourcesType;
import org.unigrids.x2006.x04.services.jms.JobPropertiesDocument.JobProperties;
import org.unigrids.x2006.x04.services.jms.SubmissionTimeDocument;
import org.unigrids.x2006.x04.services.jms.TargetSystemReferenceDocument;

public class OpenFOAMJSDLCreator {
    private static final Logger log = Logger.getLogger(OpenFOAMJSDLCreator.class);

    // testowo
//    public static final String APPLICATON_NAME = "Date";
//    private static final String APPLICATION_VERSION = "8.4";
    // docelowo
    public static final String APPLICATON_NAME = "OpenFOAM";
    private static final String APPLICATION_VERSION = "5.0";

    public static final String INPUT = "job_input.zip";
    public static final String OUTPUT = "job_output.zip";

    public static OpenFOAMJobSpecification createJobSpecification(JobProperties jobProperties) {
        OpenFOAMJobSpecification ret = new OpenFOAMJobSpecification();

        JobDescriptionType jobDesc = jobProperties.getOriginalJSDL().getJobDescription();
        ret.setName(jobDesc.getJobIdentification().getJobName());
        String[] projects = jobDesc.getJobIdentification().getJobProjectArray();
        if (projects != null && projects.length > 0)
            ret.setProject(projects[0]);
        String[] tags = jobDesc.getJobIdentification().getJobAnnotationArray();
        for (String tag : tags) {
            if (tag.startsWith("INPUT_ID="))
                ret.setInputId(tag.split("=", 2)[1]);
            if (tag.startsWith(BrokeredJobSubmissionHelper.OUTPUT_STOREAGE_URL))
                ret.setGridOutputLocation(tag.split("=", 2)[1]);
        }
        DataStagingType[] dss = jobDesc.getDataStagingArray();
        for (DataStagingType ds : dss) {
            //this is dead... SO removes all exports :[
            if (ds.getTarget() != null && ds.getFileName() != null && ds.getFileName().equals(OUTPUT)) {
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

    public static JobDefinitionDocument createJobDocument(OpenFOAMJobSpecification jobSpec) {
        JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.newInstance();
        JobDefinitionType jobDefinition = jobDefinitionDocument.addNewJobDefinition();
        JobDescriptionType jobDesc = jobDefinition.addNewJobDescription();

        ApplicationDocument applicationDocument = ApplicationDocument.Factory.newInstance();
        ApplicationType applicationType = applicationDocument.addNewApplication();
        applicationType.setApplicationName(APPLICATON_NAME);
        applicationType.setApplicationVersion(APPLICATION_VERSION);

        ResourcesDocument resourcesDocument = ResourcesDocument.Factory.newInstance();
        ResourcesType resourcesType = resourcesDocument.addNewResources();

        resourcesType.addNewIndividualPhysicalMemory()
            .addNewExact()
            .setStringValue(String.valueOf(32_000_000));
        resourcesType.addNewIndividualCPUCount()
            .addNewExact()
            .setStringValue("1");
        resourcesType.addNewIndividualCPUTime()
            .addNewExact()
            .setStringValue("3600");

        insertResourceRequest("Queue", "hydra", resourcesDocument);

//		ResourcesType resReq = jobDesc.addNewResources();
//		resReq.addNewIndividualPhysicalMemory().addNewExact().setStringValue("1000000000");
//		resReq.addNewIndividualCPUCount().addNewExact().setStringValue("1");
//		double walltime = 1 * 3600.0;
//		resReq.addNewIndividualCPUTime().addNewExact().setStringValue(String.valueOf(Math.round(walltime)));

        jobDesc.setApplication(applicationType);
        jobDesc.setResources(resourcesType);

        JobIdentificationType jobId = jobDesc.addNewJobIdentification();
        jobId.setJobName(jobSpec.getName());
        if (jobSpec.getProject() != null)
            jobId.setJobProjectArray(new String[]{jobSpec.getProject()});
        jobId.addJobAnnotation("INPUT_ID=" + jobSpec.getInputId());

        log.debug("Job description:\n" + jobDefinitionDocument.xmlText(new XmlOptions().setSavePrettyPrint()));
        return jobDefinitionDocument;
    }

    private static void insertResourceRequest(String name, String value, ResourcesDocument resourcesDocument) {
        ResourceRequestDocument resourceRequestDocument = ResourceRequestDocument.Factory.newInstance();
        resourceRequestDocument.addNewResourceRequest().setName(name);
        resourceRequestDocument.getResourceRequest().setValue(value);
        WSUtilities.append(resourceRequestDocument, resourcesDocument);
    }
}
