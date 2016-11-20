/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.angio.ui.job;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import eu.unicore.portal.core.Session;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.grid.ui.helpers.ProjectChooserComponent;
import eu.unicore.portal.ui.VaadinUpload;
import eu.unicore.portal.ui.VaadinUpload.FileSelectedEvent;
import eu.unicore.portal.ui.VaadinUpload.FileUploadedEvent;
import eu.unicore.portal.ui.VaadinUpload.FileUploadedListener;
import org.apache.commons.vfs2.FileObject;
import pl.edu.icm.unicore.portal.angio.AngioJobSpecification;
import pl.edu.icm.unicore.portal.angio.AngioMergeJSDLCreator;
import pl.edu.icm.unicore.portal.angio.AngioProperties;
import pl.edu.icm.unicore.portal.angio.StorageHelper;

/**
 * Responsible for getting job's input.
 * @author K. Benedyczak
 */
public class AngioInputPanel extends CustomComponent
{
	private Tab parent = null;
	private final MessageProvider msg;
	
	private ProjectChooserComponent project;
	private TextField jobName;

	private VaadinUpload inputUpload;
	private Label uploaded;
	private AngioProperties config;
	private String inputId;
	private enum UploadStatus {notStarted, uploading, uploaded}
	private UploadStatus uploadStatus = UploadStatus.notStarted;
	
	public AngioInputPanel(String inputId, AngioProperties config, MessageProvider msg)
	{
		this.inputId = inputId;
		this.config = config;
		this.msg = msg;
		initUI();
	}

	
	
	private synchronized UploadStatus getUploadStatus()
	{
		return uploadStatus;
	}

	private synchronized void setUploadStatus(UploadStatus uploadStatus)
	{
		this.uploadStatus = uploadStatus;
	}



	private void initUI()
	{
		VerticalLayout main = new VerticalLayout();
		main.setMargin(true);
		setCompositionRoot(main);
		main.setSpacing(true);
		
		jobName = new TextField(msg.getMessage("AngioInputPanel.jobName"));
		jobName.setRequired(true);
		jobName.setValue(msg.getMessage("AngioInputPanel.defaultJobName"));
		jobName.addValueChangeListener(new ValueChangeListener()
		{
			@Override
			public void valueChange(ValueChangeEvent event)
			{
				if (parent != null)
					parent.setCaption(jobName.getValue());
			}
		});
		jobName.setImmediate(true);
		String grantsAttribute = config.getValue(AngioProperties.GRANTS_ATTRIBUTE);
		project = new ProjectChooserComponent(grantsAttribute, msg.getMessage("AngioInputPanel.grant"));

		FileObject workspaceDirectory = StorageHelper.getWorkspaceJobDirectory(
				Session.getCurrent().getUser().getWorkspaceLocation(), inputId);
		inputUpload = new VaadinUpload(workspaceDirectory, msg);
		inputUpload.addFileUploadedListener(new FileUploadedListener()
		{
			@Override
			public void fileUploaded(FileUploadedEvent event)
			{
				setUploadStatus(UploadStatus.uploaded);
				uploaded.addStyleName(ValoTheme.LABEL_SUCCESS);
				uploaded.removeStyleName(ValoTheme.LABEL_FAILURE);				
				uploaded.setValue(msg.getMessage("AngioInputPanel.uploaded"));
			}
			
			@Override
			public void fileUploadStarted(FileSelectedEvent event)
			{
				setUploadStatus(UploadStatus.uploading);
				uploaded.removeStyleName(ValoTheme.LABEL_SUCCESS);
				uploaded.addStyleName(ValoTheme.LABEL_FAILURE);				
				uploaded.setValue(msg.getMessage("AngioInputPanel.notUploaded"));
			}
		});
		inputUpload.setDoOverwrite(true);
		inputUpload.setFixedTargetFileName(AngioMergeJSDLCreator.INPUT);
		uploaded = new Label(msg.getMessage("AngioInputPanel.notUploaded"));
		uploaded.addStyleName(ValoTheme.LABEL_FAILURE);				
		uploaded.setWidthUndefined();
		FormLayout params = new FormLayout(jobName, project, inputUpload, uploaded);
		
		main.addComponents(params);
	}

	public void setParent(Tab parent)
	{
		this.parent = parent;
	}
	
	public String getJobName()
	{
		return jobName.getValue();
	}
	
	public AngioJobSpecification getJobDescription() throws Exception
	{
		validate();
		String grant = project.getProject();
		if (grant.trim().equals(""))
			grant = null;
		return new AngioJobSpecification(jobName.getValue(),
				grant, 
				inputId, 
				null);
	}
	
	private void validate() throws Exception
	{
		boolean error = false;
		if (jobName.getValue() == null || jobName.getValue().equals(""))
		{
			jobName.setComponentError(new UserError(msg.getMessage("AngioInputPanel.errorRequired")));
			error = true;
		} else
			jobName.setComponentError(null);
		
		if (getUploadStatus() != UploadStatus.uploaded)
		{
			inputUpload.setComponentError(new UserError(
					msg.getMessage("AngioInputPanel.errorNoUploadedInput")));
			error = true;
		} else
		{
			inputUpload.setComponentError(null);
		}
		
		if (error)
			throw new IllegalStateException();
	}
}
