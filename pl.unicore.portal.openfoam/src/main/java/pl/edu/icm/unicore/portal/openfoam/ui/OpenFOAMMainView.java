/*
 * Copyright (c) 2017, ICM University of Warsaw. All rights reserved.
 * See LICENSE file for licensing information.
 */
package pl.edu.icm.unicore.portal.openfoam.ui;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
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
import pl.edu.icm.unicore.portal.openfoam.GridEnvironmentCollector;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

/**
 * Entry point of the TemplatePlugin integrated with UNICORE portal.
 *
 * @author R.Kluszczynski
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OpenFOAMMainView extends AbstractView {
    private static final Logger log = Logger.getLogger(OpenFOAMMainView.class);

    public static final String PORTAL_URL_FRAGMENT = "openfoam";

    private final UserProfilesManager profilesManager;
    private final PortalThreadPool threadPool;
    private MainPortalComponent contents;
    private PortalConfigurationSource configSource;
    private GridEnvironmentCollector collector;
//    private

    @Autowired
    public OpenFOAMMainView(PortalConfigurationSource configurationSource,
                                  MessageProvider messageProvider,
                                  PortalThreadPool threadPool,
                                  UserProfilesManager profilesManager) {
        super(messageProvider);
        this.configSource = configurationSource;
        this.threadPool = threadPool;
        this.profilesManager = profilesManager;
    }

    private com.vaadin.ui.Component initUI()
	{
		setTitle(msgProvider.getMessage("OpenFOAM.MainView.uiTitle"));
		VerticalLayout main = new VerticalLayout();
		try
		{
			contents = new MainPortalComponent(configSource, msgProvider, threadPool, profilesManager);
			contents.setVisible(true);
			main.addComponent(contents);

//			errorContents = new GridNotReadyComponent(msgProvider);
//			main.addComponent(errorContents);
		} catch (Exception e)
		{
			log.error("OpenFOAM app init error", e);
			Label errorL = new Label();
			errorL.setContentMode(ContentMode.PREFORMATTED);
			CharArrayWriter buffer = new CharArrayWriter();
			PrintWriter pw = new PrintWriter(buffer);
			e.printStackTrace(pw);
			pw.flush();
			errorL.setValue("OpenFOAM app can not be initialized. Error:\n\n" + buffer.toString());
			main.addComponent(errorL);
		}
		return main;
	}
    @Override
    protected com.vaadin.ui.Component initializeViewComponent() {
//        TODO gdzieś tutaj musi pojawić się zawołanie MainPortalComponent, żeby wszystko ruszyło...
        setTitle(msgProvider.getMessage("OpenFOAM.MainView.uiTitle"));

        com.vaadin.ui.Component main = initUI();
        collector = new GridEnvironmentCollector(
                Session.getCurrent().getUserGridDiscovery(),
                (broker, sfs) -> contents.setGridEnvironment(broker, sfs)
        );//, new EnvironmentListener(UI.getCurrent()));
		collector.start();
		return main;
    }

    @Override
    public String getFragment() {
        return PORTAL_URL_FRAGMENT;
    }
}
