/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed;

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
	private StorageUtil storageUtil;
	
	public JobHandler(BrokeredJobSubmissionHelper submissionService, StorageUtil storageUtil)
	{
		this.submissionService = submissionService;
		this.storageUtil = storageUtil;
	}

	public String submitJob(SinusMedJobSpecification jobSpec, SinusMedGridEnvironment gridEnvironment) 
			throws Exception
	{
		JobDefinitionDocument job = SinusMedJSDLCreator.createSinusmedJobDocument(jobSpec, 
				gridEnvironment.ncpus);
		
		FileTransfersSpec localImports = null;
		FileTransfersSpec exports = null;
		FileTransfersSpec gridImports = null;
		if (jobSpec.getGridInputLocation() != null)
		{
			gridImports = new FileTransfersSpec(jobSpec.getGridInputLocation(), 
					SinusMedJSDLCreator.INPUT);
		} else
		{
			localImports = new FileTransfersSpec(storageUtil.getOriginalInputLocation(
					jobSpec.getInputId()).getAbsolutePath(), 
					SinusMedJSDLCreator.INPUT);
		}

		exports = new FileTransfersSpec(SinusMedJSDLCreator.OUTPUT, 
					jobSpec.getGridOutputLocation());
		exports.add(SinusMedJSDLCreator.CONVERTED_INPUT, jobSpec.getGridOutputLocation());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String folder = "SinusMED-" + sdf.format(new Date()) + "-" + (new Random().nextInt());
		return submissionService.submitBrokeredJob(localImports, gridImports, 
				exports, gridEnvironment.sfs, folder,
				job, gridEnvironment.broker);
	}

	public synchronized StorageUtil getStorageUtil()
	{
		return storageUtil;
	}
	
	
}
