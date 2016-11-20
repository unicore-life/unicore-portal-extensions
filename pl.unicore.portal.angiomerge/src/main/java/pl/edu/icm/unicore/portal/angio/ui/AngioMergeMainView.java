/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.angio.ui;

import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import eu.unicore.griddisco.core.model.BrokerService;
import eu.unicore.griddisco.core.model.StorageFactoryService;
import eu.unicore.portal.core.PortalConfigurationSource;
import eu.unicore.portal.core.PortalThreadPool;
import eu.unicore.portal.core.Session;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.core.userprefs.UserProfilesManager;
import eu.unicore.portal.ui.views.AbstractView;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.edu.icm.unicore.portal.angio.AngioGridEnvironment;
import pl.edu.icm.unicore.portal.angio.GridEnvironmentCollector;
import pl.edu.icm.unicore.portal.angio.GridEnvironmentCollector.GridEnvironmentListener;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

/**
 * Entry point of the AngioMerge app integrated with UNICORE portal.
 * @author K. Benedyczak
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AngioMergeMainView extends AbstractView
{
	private static final Logger log = Logger.getLogger(AngioMergeMainView.class);
	
	public static final String FRAGMENT = "angiomerge";

	private PortalConfigurationSource configSource;

	private PortalThreadPool tpool;

	private UserProfilesManager profilesMan;

	private MainPortalComponent contents;

	private GridNotReadyComponent errorContents;

	private GridEnvironmentCollector collector;
	
	
	@Autowired
	public AngioMergeMainView(PortalConfigurationSource configSource, MessageProvider msg, 
			PortalThreadPool tpool, UserProfilesManager profilesMan)
	{
		super(msg);
		this.configSource = configSource;
		this.tpool = tpool;
		this.profilesMan = profilesMan;
	}
	
	@Override
	protected com.vaadin.ui.Component initializeViewComponent()
	{
		setTitle(msgProvider.getMessage("Angio.MainView.uiTitle"));
		VerticalLayout main = new VerticalLayout();
		try
		{
			contents = new MainPortalComponent(configSource, msgProvider, tpool,
					profilesMan);
			main.addComponent(contents);
			errorContents = new GridNotReadyComponent(msgProvider);
			main.addComponent(errorContents);
		} catch (Exception e)
		{
			log.error("Angiomerge app init error", e);
			Label errorL = new Label();
			errorL.setContentMode(ContentMode.PREFORMATTED);
			CharArrayWriter buffer = new CharArrayWriter();
			PrintWriter pw = new PrintWriter(buffer);
			e.printStackTrace(pw);
			pw.flush();
			errorL.setValue("AngioMerge app can not be initialized. Error:\n\n" + buffer.toString());
			main.addComponent(errorL);
		}
		collector = new GridEnvironmentCollector(
				Session.getCurrent().getUserGridDiscovery(), new EnvironmentListener(UI.getCurrent()));
		collector.start();
		return main;
	}

	@Override
	public String getFragment()
	{
		return FRAGMENT;
	}
	
	private class EnvironmentListener implements GridEnvironmentListener
	{
		private final UI ui;
		
		public EnvironmentListener(UI ui)
		{
			this.ui = ui;
			ui.addDetachListener(new DetachListener()
			{
				@Override
				public void detach(DetachEvent event)
				{
					collector.stop();
				}
			});
			
		}

		public void gridReady(BrokerService broker, StorageFactoryService sfs)
		{
			VaadinSession session = ui.getSession();
			if (session == null)
			{
				//only needed when detach event was not yet handled and discovery notification was faster
				collector.stop(); 
				return;
			}
			session.lock();
			try
			{
				contents.setGridEnvironment(new AngioGridEnvironment(broker, sfs));
				errorContents.setVisible(false);
				contents.setVisible(true);
			} finally
			{
				session.unlock();
			}
		}
		
		public void gridNotReady()
		{
			VaadinSession session = ui.getSession();
			if (session == null)
			{
				//only needed when detach event was not yet handled and discovery notification was faster
				collector.stop(); 
				return;
			}
			session.lock();
			try
			{
				errorContents.setVisible(true);
				contents.setVisible(false);
			} finally
			{
				session.unlock();
			}
		}

		@Override
		public void update(BrokerService broker, StorageFactoryService sfs)
		{
			if (broker == null || sfs == null)
				gridNotReady();
			else
				gridReady(broker, sfs);
		}
	}

}
