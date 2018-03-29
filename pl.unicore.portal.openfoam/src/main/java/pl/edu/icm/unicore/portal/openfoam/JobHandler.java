package pl.edu.icm.unicore.portal.openfoam;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;

import eu.unicore.portal.core.User;
import eu.unicore.portal.grid.ui.helpers.BrokeredJobSubmissionHelper;
import eu.unicore.portal.grid.ui.helpers.FileTransfersSpec;

public class JobHandler 
{
	private BrokeredJobSubmissionHelper submissionService;
	
	public JobHandler(BrokeredJobSubmissionHelper submissionService)
	{
		this.submissionService = submissionService;
	}
	
	public String submitJob(OpenFOAMJobSpecification jobSpec, OpenFOAMGridEnvironment gridEnvironment)
			throws Exception
	{
		JobDefinitionDocument job = OpenFOAMJSDLCreator.createJobDocument(jobSpec);
		
		FileTransfersSpec localImports = null;
		FileTransfersSpec exports = null;
		FileTransfersSpec gridImports = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String folder = "AngioMerge-" + sdf.format(new Date()) + "-" + (new Random().nextInt());
		return submissionService.submitBrokeredJob(localImports, gridImports, 
				exports, gridEnvironment.sfs, folder,
				job, gridEnvironment.broker);
	}
}
