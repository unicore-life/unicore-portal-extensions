/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.openfoam.ui.job;

import com.vaadin.ui.*;
import com.vaadin.ui.Upload.*;
import eu.unicore.portal.core.User;
import eu.unicore.portal.core.i18n.MessageProvider;
import org.apache.log4j.Logger;
import pl.edu.icm.unicore.portal.openfoam.OpenFOAMProperties;
import pl.edu.icm.unicore.portal.openfoam.StorageUtil;

import java.io.*;

/**
 * Handles uploads of job's input. Input file is stored locally in user's Sinusmed workspace using a provided 
 * id as a filename base. 
 * @author K. Benedyczak
 */
public class JobInputUploadPanel extends CustomComponent
{
	private static final Logger log = Logger.getLogger(JobInputUploadPanel.class);

	private Upload inputUpload;
	private ProgressBar uploadProgress;
	private Label inputInfo;
	
	private final MessageProvider msg; 
	private final String inputId;
	private final StorageUtil storageUtil;
	
	private boolean uploadCompleted = false;
	
	public JobInputUploadPanel(String inputId, OpenFOAMProperties config, MessageProvider msg, User user)
	{
		this.inputId = inputId;
		this.msg = msg;
		this.storageUtil = new StorageUtil(config, user);
		initUI();
	}

	private void initUI()
	{
		VerticalLayout main = new VerticalLayout();
		main.setSpacing(true);
		InputReceiver inputReceiver = new InputReceiver();
		inputUpload = new Upload();
		inputUpload.setReceiver(inputReceiver);
		inputUpload.setImmediate(true);
		inputUpload.addFinishedListener(inputReceiver);
		inputUpload.addFailedListener(inputReceiver);
		inputUpload.addProgressListener(inputReceiver);
		inputUpload.addStartedListener(inputReceiver);
		
		uploadProgress = new ProgressBar();
		uploadProgress.setVisible(false);
		uploadProgress.setWidth(90, Unit.PERCENTAGE);
		uploadProgress.setImmediate(true);
		
		inputInfo = new Label();
		inputInfo.setValue(msg.getMessage("OpenFOAM.UploadPanel.noInput"));
		
		main.addComponents(inputUpload, uploadProgress, inputInfo);
		
		setCompositionRoot(main);
	}
	
	public synchronized boolean isUploadCompleted()
	{
		return uploadCompleted;
	}

	private synchronized void setUploadCompleted(boolean uploadCompleted)
	{
		this.uploadCompleted = uploadCompleted;
	}

	private class InputReceiver implements Receiver, ProgressListener, FinishedListener, 
		FailedListener, StartedListener 
	{
		@Override
		public OutputStream receiveUpload(String filename, String mimeType)
		{
			File outputFile = storageUtil.getOriginalInputLocation(inputId);
			OutputStream os;
			try
			{
				os = new BufferedOutputStream(new FileOutputStream(outputFile));
			} catch (FileNotFoundException e)
			{
				log.error("Can not create file to store uploaded input, " + outputFile, e);
				throw new RuntimeException(e);
			}
			return os;
		}

		@Override
		public void updateProgress(long readBytes, long contentLength)
		{
			if (contentLength == -1 || readBytes == 0)
				return;
			uploadProgress.setValue(((float)readBytes/contentLength));
		}

		@Override
		public void uploadFinished(FinishedEvent event)
		{
			uploadProgress.setVisible(false);

			long len = event.getLength();
			inputInfo.setValue(msg.getMessage("OpenFOAM.UploadPanel.uploadOK",
					StorageUtil.sizeAsHumanReadableString(len)));
			setUploadCompleted(true);
			inputUpload.setComponentError(null);
			inputUpload.setEnabled(true);
		}

		@Override
		public void uploadFailed(FailedEvent event)
		{
			uploadProgress.setVisible(false);
			inputInfo.setValue(msg.getMessage("OpenFOAM.UploadPanel.uploadFailed", event.getReason()));
			inputUpload.setEnabled(true);
		}

		@Override
		public void uploadStarted(StartedEvent event)
		{
			uploadProgress.setVisible(true);
			inputInfo.setValue(msg.getMessage("OpenFOAM.UploadPanel.uploading"));
			uploadProgress.setValue(0f);
			uploadProgress.setIndeterminate(event.getContentLength() <= 0);
			inputUpload.setEnabled(false);
			setUploadCompleted(false);
		}
	}
}
