/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.angio.ui.job;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import eu.unicore.portal.core.PortalConfiguration;
import eu.unicore.portal.core.Session;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.grid.core.utils.URIUtils;
import org.apache.log4j.Logger;
import pl.edu.icm.unicore.portal.angio.AngioJobSpecification;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Responsible for showing submitted job settings.
 * @author K. Benedyczak
 */
public class AngioInputROPanel extends CustomComponent
{
	private static final Logger log = Logger.getLogger(AngioInputROPanel.class);

	private final MessageProvider msg;
	
	private Label project;
	private Label jobName;
	private Label output;
	private Label submissionTime;
	private Label executionSite;

	public AngioInputROPanel(MessageProvider msg)
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
		jobName.setCaption(msg.getMessage("AngioInputROPanel.jobName"));
		submissionTime = new Label();
		submissionTime.setCaption(msg.getMessage("AngioInputROPanel.submissionTime"));

		project = new Label();
		project.setCaption(msg.getMessage("AngioInputROPanel.grant"));

		output = new Label();
		output.setCaption(msg.getMessage("AngioInputROPanel.output"));

		executionSite = new Label();
		executionSite.setCaption(msg.getMessage("AngioInputROPanel.execSite"));

		FormLayout params = new FormLayout(jobName, submissionTime, executionSite, project, 
				output);
		main.addComponents(params);
	}
	
	@Override
	public String getCaption()
	{
		return jobName.getValue();
	}
	
	public void setJob(AngioJobSpecification jobSpec)
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
		output.setValue(jobSpec.getGridOutputLocation());
		
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
