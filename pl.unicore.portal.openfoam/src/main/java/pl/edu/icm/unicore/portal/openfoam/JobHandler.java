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

    public JobHandler(BrokeredJobSubmissionHelper submissionService) {
        this.submissionService = submissionService;
    }

    public String submitJob(OpenFOAMJobSpecification jobSpec, OpenFOAMGridEnvironment gridEnvironment)
            throws Exception {
        JobDefinitionDocument job = OpenFOAMJSDLCreator.createJobDocument(jobSpec);

        FileTransfersSpec localImports = null;
        FileTransfersSpec exports = null;
        FileTransfersSpec gridImports = null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String folder = "OpenFOAM-" + sdf.format(new Date()) + "-" + (new Random().nextInt());
        return submissionService.submitBrokeredJob(localImports, gridImports,
                exports, gridEnvironment.getStorageFactoryService(), folder,
                job, gridEnvironment.getBrokerService());
    }

    public void submitDateJob(OpenFOAMGridEnvironment gridEnvironment) {
        JobDefinitionDocument jobDocument = DateJobSpecification.createJobDocument("testing-job");

        try {
            String workAssignmentID = submissionService.submitBrokeredJob(
                    null,
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
