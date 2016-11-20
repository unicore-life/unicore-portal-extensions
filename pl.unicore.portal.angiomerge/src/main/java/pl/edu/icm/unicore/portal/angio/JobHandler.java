/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.angio;

import eu.unicore.portal.core.User;
import eu.unicore.portal.grid.ui.helpers.BrokeredJobSubmissionHelper;
import eu.unicore.portal.grid.ui.helpers.FileTransfersSpec;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


/**
 * Handles SinusMed job's submission.
 * @author K. Benedyczak
 */
public class JobHandler
{
	private BrokeredJobSubmissionHelper submissionService;
	
	public JobHandler(BrokeredJobSubmissionHelper submissionService)
	{
		this.submissionService = submissionService;
	}

	public String submitJob(User user, AngioJobSpecification jobSpec, AngioGridEnvironment gridEnvironment) 
			throws Exception
	{
		JobDefinitionDocument job = AngioMergeJSDLCreator.createJobDocument(jobSpec);
		
		FileTransfersSpec localImports = null;
		FileTransfersSpec exports = null;
		FileTransfersSpec gridImports = null;
		
		localImports = new FileTransfersSpec(StorageHelper.resolveWorkspaceFile(user.getWorkspaceLocation(), 
				jobSpec.getInputId(), 
				AngioMergeJSDLCreator.INPUT),
				AngioMergeJSDLCreator.INPUT);

		exports = new FileTransfersSpec(AngioMergeJSDLCreator.OUTPUT, 
					jobSpec.getGridOutputLocation());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String folder = "AngioMerge-" + sdf.format(new Date()) + "-" + (new Random().nextInt());
		return submissionService.submitBrokeredJob(localImports, gridImports, 
				exports, gridEnvironment.sfs, folder,
				job, gridEnvironment.broker);
	}
}
