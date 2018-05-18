package pl.edu.icm.unicore.portal.openfoam;

import eu.unicore.portal.grid.ui.helpers.BrokeredJobSubmissionHelper;
import eu.unicore.portal.grid.ui.helpers.FileTransfersSpec;
import org.apache.log4j.Logger;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import pl.edu.icm.unicore.portal.openfoam.ui.test.DateJobSpecification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class JobHandler {
    private static final Logger log = Logger.getLogger(JobHandler.class);

    private final BrokeredJobSubmissionHelper submissionService;
    private StorageUtil storageUtil;

    public JobHandler(BrokeredJobSubmissionHelper submissionService, StorageUtil storageUtil) {
        this.submissionService = submissionService;
        this.storageUtil = storageUtil;
    }

    public String submitJob(OpenFOAMJobSpecification jobSpec, OpenFOAMGridEnvironment gridEnvironment)
            throws Exception {
        JobDefinitionDocument job = OpenFOAMJSDLCreator.createJobDocument(jobSpec);
//        JobDefinitionDocument job = DateJobSpecification.createJobDocument("test-date-job");

        FileTransfersSpec exports = null;
        FileTransfersSpec gridImports = null;


        FileTransfersSpec localImports = new FileTransfersSpec(storageUtil.getOriginalInputLocation(
                jobSpec.getInputId()).getAbsolutePath(),
                OpenFOAMJSDLCreator.INPUT);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String folder = "OpenFOAM-" + sdf.format(new Date()) + "-" + (new Random().nextInt());
        String jobAssignmentID = submissionService.submitBrokeredJob(localImports, gridImports,
                exports, gridEnvironment.getStorageFactoryService(), folder,
                job, gridEnvironment.getBrokerService());
        log.info("Submitted work assignment: " + jobAssignmentID);
        return jobAssignmentID;
    }


    public void submitDateJob(OpenFOAMGridEnvironment gridEnvironment) {
        JobDefinitionDocument jobDocument = DateJobSpecification.createJobDocument("testing-job");

        FileTransfersSpec fileTransfersSpec = new FileTransfersSpec("/tmp/local-file.md", "test.txt");
        try {
            String workAssignmentID = submissionService.submitBrokeredJob(
                    fileTransfersSpec,
                    null,
                    null,
                    gridEnvironment.getStorageFactoryService(),
                    null,
                    jobDocument,
                    gridEnvironment.getBrokerService()
            );

            log.info("Submitted work assignment: " + workAssignmentID);
        } catch (Exception e) {
            log.error("Error during work assignment submission!", e);
        }
    }
}
