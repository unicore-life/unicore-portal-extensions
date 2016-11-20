/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed.ui.job;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import eu.unicore.portal.core.User;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.grid.ui.helpers.ProjectChooserComponent;
import pl.edu.icm.unicore.portal.sinusmed.JobProfile;
import pl.edu.icm.unicore.portal.sinusmed.SinusMedJobSpecification;
import pl.edu.icm.unicore.portal.sinusmed.SinusMedProperties;

/**
 * Responsible for getting job's input.
 * @author K. Benedyczak
 */
public class SinusInputPanel extends CustomComponent
{
	private static final String LOCAL_L = "l";
	private static final String GRID_L = "g";
	
	private String inputId;
	private SinusMedProperties config;
	private Tab parent = null;
	private final MessageProvider msg;
	
	private ProjectChooserComponent project;
	private TextField jobName;
	private ComboBox atlasChooser;
	private ComboBox execProfileChooser;
	private OptionGroup inputSourceChooser;

	private JobInputGridPanel gridInput;
	private JobInputUploadPanel uploadedInput;
	private User user;
	
	public SinusInputPanel(String inputId, SinusMedProperties config, MessageProvider msg, User user)
	{
		this.msg = msg;
		this.inputId = inputId;
		this.config = config;
		this.user = user;
		initUI();
	}

	private void initUI()
	{
		VerticalLayout main = new VerticalLayout();
		main.setMargin(true);
		setCompositionRoot(main);
		main.setSpacing(true);
		
		jobName = new TextField(msg.getMessage("SinusMed.SinusInput.jobName"));
		jobName.setRequired(true);
		jobName.setValue(msg.getMessage("SinusMed.SinusInput.defaultJobName"));
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
		String grantsAttribute = config.getValue(SinusMedProperties.GRANTS_ATTRIBUTE);
		project = new ProjectChooserComponent(grantsAttribute, msg.getMessage("SinusMed.SinusInput.grant"));

		uploadedInput = new JobInputUploadPanel(inputId, config, msg, user);
		gridInput = new JobInputGridPanel(msg);
		gridInput.setVisible(false);
		
		inputSourceChooser = new OptionGroup();
		inputSourceChooser.addItem(LOCAL_L);
		inputSourceChooser.setItemCaption(LOCAL_L, msg.getMessage("SinusMed.SinusInput.localInput"));
		inputSourceChooser.addItem(GRID_L);
		inputSourceChooser.setItemCaption(GRID_L, msg.getMessage("SinusMed.SinusInput.gridInput"));
		inputSourceChooser.setImmediate(true);
		inputSourceChooser.select(LOCAL_L);
		inputSourceChooser.addValueChangeListener(new ValueChangeListener()
		{
			@Override
			public void valueChange(ValueChangeEvent event)
			{
				boolean localInput = LOCAL_L.equals(inputSourceChooser.getValue());
				uploadedInput.setVisible(localInput);
				gridInput.setVisible(!localInput);
			}
		});

		HorizontalLayout inputsLay = new HorizontalLayout();
		inputsLay.setSpacing(true);
		inputsLay.addComponents(uploadedInput, gridInput);
		
		//FIXME - both from Apps desc
		atlasChooser = new ComboBox(msg.getMessage("SinusMed.SinusInput.atlas"));
		atlasChooser.addItem("02");
		atlasChooser.select("02");
		atlasChooser.setNullSelectionAllowed(false);
		
		execProfileChooser = new ComboBox(msg.getMessage("SinusMed.SinusInput.profile"));
		for (JobProfile p: JobProfile.values())
			execProfileChooser.addItem(p.name());
		execProfileChooser.select(JobProfile.normal.name());
		execProfileChooser.setNullSelectionAllowed(false);
		
		FormLayout params = new FormLayout(jobName, project, inputSourceChooser, inputsLay, 
				atlasChooser, execProfileChooser);
		
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
	
	public SinusMedJobSpecification getJobDescription() throws Exception
	{
		validate();
		boolean localInput = LOCAL_L.equals(inputSourceChooser.getValue());
		String grant = project.getProject();
		if (grant.trim().equals(""))
			grant = null;
		return new SinusMedJobSpecification(jobName.getValue(),
				atlasChooser.getValue().toString(), 
				grant, 
				JobProfile.valueOf(execProfileChooser.getValue().toString()), 
				inputId, 
				localInput ? null : gridInput.getGridInputFile().toString(),
				null);
	}
	
	private void validate() throws Exception
	{
		boolean error = false;
		if (jobName.getValue() == null || jobName.getValue().equals(""))
		{
			jobName.setComponentError(new UserError(msg.getMessage("SinusMed.SinusInput.errorRequired")));
			error = true;
		} else
			jobName.setComponentError(null);
		
		boolean localInput = LOCAL_L.equals(inputSourceChooser.getValue());
		if (localInput)
		{
			if (!uploadedInput.isUploadCompleted())
			{
				uploadedInput.setComponentError(new UserError(
						msg.getMessage("SinusMed.SinusInput.errorNoUploadedInput")));
				error = true;
			} else
			{
				uploadedInput.setComponentError(null);
			}
			gridInput.setComponentError(null);
		} else
		{
			if (gridInput.getGridInputFile() == null)
			{
				gridInput.setComponentError(new UserError(
						msg.getMessage("SinusMed.SinusInput.errorNoGridInput")));
				error = true;
			} else
				gridInput.setComponentError(null);
			uploadedInput.setComponentError(null);
		}
		
		if (error)
			throw new IllegalStateException();
	}
}
