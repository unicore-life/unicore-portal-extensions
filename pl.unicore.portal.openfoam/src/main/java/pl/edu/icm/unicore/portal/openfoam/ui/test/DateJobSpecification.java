package pl.edu.icm.unicore.portal.openfoam.ui.test;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobIdentificationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ResourcesType;

/**
 * Class responsible only for preparing date job description.
 */
public final class DateJobSpecification {
    private static final Logger log = Logger.getLogger(DateJobSubmitter.class);

    private static final String DATE_APPLICATION_NAME = "Date";

    private DateJobSpecification() {
    }

    public static JobDefinitionDocument createJobDocument(String jobName) {
        JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.newInstance();
        JobDefinitionType jobDefinition = jobDefinitionDocument.addNewJobDefinition();
        JobDescriptionType jobDescription = jobDefinition.addNewJobDescription();

        ApplicationDocument applicationDocument = ApplicationDocument.Factory.newInstance();
        ApplicationType applicationType = applicationDocument.addNewApplication();
        applicationType.setApplicationName(DATE_APPLICATION_NAME);

        ResourcesType resReq = jobDescription.addNewResources();
        resReq.addNewIndividualPhysicalMemory().addNewExact().setStringValue("16000000");
        resReq.addNewIndividualCPUCount().addNewExact().setStringValue("1");
        resReq.addNewIndividualCPUTime().addNewExact().setStringValue("3600");

        jobDescription.setApplication(applicationType);

        JobIdentificationType jobIdentification = jobDescription.addNewJobIdentification();
        jobIdentification.setJobName(jobName);

        log.debug("JOB DESCRIPTION: \n" + jobDefinitionDocument.xmlText(new XmlOptions().setSavePrettyPrint()));
        return jobDefinitionDocument;
    }
}
