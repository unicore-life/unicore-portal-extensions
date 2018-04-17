/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.openfoam.ui;

import com.google.common.collect.Sets;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import eu.unicore.griddisco.core.api.UserGridDiscovery;
import eu.unicore.griddisco.core.api.filter.Filter;
import eu.unicore.griddisco.core.api.filter.JobApplicationFilter;
import eu.unicore.griddisco.core.model.AtomicJob;
import eu.unicore.griddisco.core.model.BrokerService;
import eu.unicore.griddisco.core.model.StorageFactoryService;
import eu.unicore.portal.core.PortalConfigurationSource;
import eu.unicore.portal.core.PortalThreadPool;
import eu.unicore.portal.core.Session;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.core.userprefs.UserProfilesManager;
import eu.unicore.portal.grid.ui.actions.AbortJobAction;
import eu.unicore.portal.grid.ui.actions.BrowseJobDirectoryAction;
import eu.unicore.portal.grid.ui.actions.DeleteJobAction;
import eu.unicore.portal.grid.ui.actions.RestartJobAction;
import eu.unicore.portal.grid.ui.actions.ShowJobDetailsAction;
import eu.unicore.portal.grid.ui.helpers.BrokeredJobSubmissionHelper;
import eu.unicore.portal.grid.ui.views.JobsTable;
import eu.unicore.portal.ui.IconUtil;
import eu.unicore.portal.ui.Styles;
import eu.unicore.portal.ui.icons.IconRepository;
import eu.unicore.portal.ui.menu.PortalHandler;
import eu.unicore.portal.ui.menu.SingleContextAction;
import org.apache.log4j.Logger;
import pl.edu.icm.unicore.portal.openfoam.JobHandler;
import pl.edu.icm.unicore.portal.openfoam.OpenFOAMGridEnvironment;
import pl.edu.icm.unicore.portal.openfoam.OpenFOAMJSDLCreator;
import pl.edu.icm.unicore.portal.openfoam.OpenFOAMProperties;
import pl.edu.icm.unicore.portal.openfoam.ui.test.DateJobSubmitter;

import java.util.Collection;

/**
 * Top level component of SinusMed interface. The top part contains a jobs
 * table. The lower part contains tabs where completed/running jobs can be seen
 * and where new jobs can be prepared and submitted.
 *
 * @author K. Benedyczak
 */
public class MainPortalComponent extends CustomComponent
{
	private static final Logger log = Logger.getLogger(MainPortalComponent.class);

	private OpenFOAMProperties config;
	private JobHandler jobHandler;

	private final MessageProvider msg;

	private TabSheet simulations;

	private PortalThreadPool tpool;

	private UserProfilesManager profilesMan;

	private final OpenFOAMGridEnvironment gridEnvironment = new OpenFOAMGridEnvironment();

	public MainPortalComponent(PortalConfigurationSource configSource, MessageProvider msg,
                               PortalThreadPool tpool, UserProfilesManager profilesMan)
	{
		this.msg = msg;
		this.tpool = tpool;
		this.profilesMan = profilesMan;
		config = new OpenFOAMProperties(configSource.getProperties());
		jobHandler = new JobHandler(new BrokeredJobSubmissionHelper());
		initUI();
	}

//	public void setGridEnvironment(OpenFOAMGridEnvironment gridEnvironment)
//	{
//		this.gridEnvironment = gridEnvironment;
//	}

    public void setGridEnvironment(BrokerService broker, StorageFactoryService sfs) {
        gridEnvironment.setBrokerService(broker);
        gridEnvironment.setStorageFactoryService(sfs);
	}

    private void initUI()
	{
		VerticalLayout main = new VerticalLayout();
		main.setSizeFull();
		main.setMargin(true);
		setCompositionRoot(main);

        Button submitDateJobButton = new Button("Submit Date Job");
        submitDateJobButton.addClickListener(new DateJobSubmitter(jobHandler, gridEnvironment));
        main.addComponent(submitDateJobButton);

		HorizontalLayout mainToolbar = new HorizontalLayout();
		main.addComponents(mainToolbar);

		simulations = new TabSheet();
		simulations.addStyleName(Reindeer.TABSHEET_MINIMAL);
		simulations.setSizeFull();

		VerticalLayout wrapperBottom = new VerticalLayout();
		wrapperBottom.setMargin(new MarginInfo(true, false, false, false));
		wrapperBottom.addComponent(simulations);

		final JobApplicationFilter filter = new JobApplicationFilter(
				OpenFOAMJSDLCreator.APPLICATON_NAME);
		final SingleContextAction<AtomicJob> openJob = new SingleContextAction<AtomicJob>(
				msg.getMessage("OpenFOAM.MainComponent.openSimulation"),
				IconRepository.ICON_ID_VIEW,
				AtomicJob.class)
		{
			@Override
			public void invoke(AtomicJob job)
			{
//				openExistingJob(job);
			}
		};

		JobsTable table = new JobsTable(msg, tpool, profilesMan)
		{
			protected Filter getJobsFilter() {
				return filter;
			}

			@Override
			protected void createUI()
			{
				super.createUI();
				table.setPageLength(5);
			}

			protected PortalHandler[] getInitHandlers() {
				UserGridDiscovery userGridDiscovery = Session.getCurrent().getUserGridDiscovery();
				return new PortalHandler[] {
						openJob,
						new BrowseJobDirectoryAction(msgProvider, tpool.getExecutor(), profilesMan),
						new ShowJobDetailsAction(msgProvider),
						new DeleteJobAction(msgProvider, tpool.getExecutor(), userGridDiscovery),
						new AbortJobAction(msgProvider, tpool.getExecutor(), userGridDiscovery),
						new RestartJobAction(msgProvider, tpool.getExecutor(), userGridDiscovery)
					};
			}

			protected Collection<String> getColumnsCollapsedByDefault()
			{
				return Sets.newHashSet(ShowJobDetailsAction.DETAIL_ESTIMATED_FINISH,
						ShowJobDetailsAction.DETAIL_EXIT_CODE,
						ShowJobDetailsAction.DETAIL_APPLICATION,
						ShowJobDetailsAction.DETAIL_TAGS);
			}
		};


//		Button newSimulationButton = new Button(
//				msg.getMessage("OpenFOAM.MainComponent.newSimulationButton"));
//		newSimulationButton.setIcon(IconUtil.getIconFromTheme(IconRepository.ICON_ID_NEW_FILE));
//		newSimulationButton.addClickListener(new Button.ClickListener()
//		{
//			@Override
//			public void buttonClick(Button.ClickEvent event)
//			{
//				SinusJobPanel jobPanel = new SinusJobPanel(config, jobHandler, msg, tpool,
//						Session.getCurrent().getUserGridDiscovery(), gridEnvironment);
//				addTab(jobPanel);
//			}
//		});


		final VerticalLayout vl = new VerticalLayout();
		vl.addComponents(table);

		final Button hideJobsTable = new Button(
				msg.getMessage("SinusMed.MainComponent.hideJobsTableButton"));
		hideJobsTable.setData(true);
		hideJobsTable.setIcon(IconUtil.getIconFromTheme(IconRepository.ICON_ID_UPLOAD_FILE));
		hideJobsTable.addClickListener(new Button.ClickListener()
		{
			@Override
			public void buttonClick(Button.ClickEvent event)
			{
				updateShowHideButton(hideJobsTable, vl);
			}
		});

//		mainToolbar.addComponents(newSimulationButton, hideJobsTable);
		mainToolbar.addStyleName(Styles.MARGIN_TOP_BOTTOM_15);
		mainToolbar.setSpacing(true);

		main.addComponents(vl, wrapperBottom);
	}

	private void updateShowHideButton(Button hideJobsTable, Component toHide)
	{
		if (hideJobsTable.getData().equals(true))
		{
			hideJobsTable.setCaption(msg.getMessage("SinusMed.MainComponent.showJobsTableButton"));
			hideJobsTable.setData(false);
			toHide.setVisible(false);
		} else
		{
			hideJobsTable.setCaption(msg.getMessage("SinusMed.MainComponent.hideJobsTableButton"));
			hideJobsTable.setData(true);
			toHide.setVisible(true);
		}
	}

//	private void openExistingJob(AtomicJob job)
//	{
//		SinusMedJobSpecification jobSpec;
//		try
//		{
//			jobSpec = SinusMedJSDLCreator.createSinusmedJobSpecification(job.getProperties());
//		} catch (Exception e)
//		{
//			log.error("Error parsing job's properties", e);
//			Notification.show(msg.getMessage("SinusMed.MainComponent.errorParseJob", e.toString()),
//					Type.ERROR_MESSAGE);
//			return;
//		}
//		SinusJobPanel jobPanel = new SinusJobPanel(config, jobSpec, job, jobHandler, msg,
//				tpool, Session.getCurrent().getUserGridDiscovery(), gridEnvironment);
//		addTab(jobPanel);
//	}
//
//	private void addTab(SinusJobPanel jobPanel)
//	{
//		Tab tab = simulations.addTab(jobPanel);
//		tab.setClosable(true);
//		jobPanel.setParent(tab);
//		simulations.setSelectedTab(tab);
//	}
}
