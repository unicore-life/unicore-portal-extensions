/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed.ui.job;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import eu.unicore.portal.core.PortalConfiguration;
import eu.unicore.portal.core.Session;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.grid.core.utils.URIUtils;
import org.apache.log4j.Logger;
import pl.edu.icm.unicore.portal.sinusmed.SinusMedJobSpecification;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Responsible for showing submitted job settings.
 * @author K. Benedyczak
 */
public class SinusInputROPanel extends CustomComponent
{
	private static final Logger log = Logger.getLogger(SinusInputROPanel.class);

	private final MessageProvider msg;
	
	private Label project;
	private Label jobName;
	private Label atlas;
	private Label execProfile;
	private Label inputSource;
	private Label output;
	private Label submissionTime;
	private Label executionSite;

	public SinusInputROPanel(MessageProvider msg)
	{
		this.msg = msg;
		initUI();
	}

	private void initUI()
	{
		VerticalLayout main = new VerticalLayout();
		main.setMargin(true);
		setCompositionRoot(main);
		main.setSpacing(true);
		
		jobName = new Label();
		jobName.setCaption(msg.getMessage("SinusMed.SinusInput.jobName"));
		submissionTime = new Label();
		submissionTime.setCaption(msg.getMessage("SinusMed.SinusInput.submissionTime"));

		project = new Label();
		project.setCaption(msg.getMessage("SinusMed.SinusInput.grant"));

		inputSource = new Label();
		inputSource.setCaption(msg.getMessage("SinusMed.SinusInput.input"));
		
		output = new Label();
		output.setCaption(msg.getMessage("SinusMed.SinusInput.output"));

		executionSite = new Label();
		executionSite.setCaption(msg.getMessage("SinusMed.SinusInput.execSite"));

		atlas = new Label();
		atlas.setCaption(msg.getMessage("SinusMed.SinusInput.atlas"));
		
		execProfile = new Label();
		execProfile.setCaption(msg.getMessage("SinusMed.SinusInput.profile"));
		
		FormLayout params = new FormLayout(jobName, submissionTime, executionSite, project, 
				inputSource, output, atlas, execProfile);
		main.addComponents(params);
	}
	
	@Override
	public String getCaption()
	{
		return jobName.getValue();
	}
	
	public void setJob(SinusMedJobSpecification jobSpec)
	{
		jobName.setValue(jobSpec.getName());
		
		Date submission = jobSpec.getExecutionStart();
		if (submission != null)
		{
			Locale l = PortalConfiguration.getDefaultLocale();
			Session session = Session.getCurrent();
			if (session != null && session.getLocale() != null)
				l = session.getLocale();

			submissionTime.setValue(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL,
					l).format(submission));
		}
		project.setValue(jobSpec.getProject());
		inputSource.setValue(jobSpec.getGridInputLocation());
		output.setValue(jobSpec.getGridOutputLocation());
		atlas.setValue(jobSpec.getAtlas());
		execProfile.setValue(jobSpec.getExecProfile().name());
		
		try
		{
			if (jobSpec.getExecutionSite() != null)
				executionSite.setValue(URIUtils.extractUnicoreServiceContainerName(
					new URI(jobSpec.getExecutionSite())));
		} catch (URISyntaxException e)
		{
			log.error(e);
		}
	}
}
