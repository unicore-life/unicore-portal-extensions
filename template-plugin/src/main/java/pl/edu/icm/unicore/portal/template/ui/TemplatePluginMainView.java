/*
 * Copyright (c) 2017, ICM University of Warsaw. All rights reserved.
 * See LICENSE file for licensing information.
 */
package pl.edu.icm.unicore.portal.template.ui;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import eu.unicore.portal.core.PortalConfigurationSource;
import eu.unicore.portal.core.PortalThreadPool;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.core.userprefs.UserProfilesManager;
import eu.unicore.portal.ui.views.AbstractView;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Entry point of the TemplatePlugin integrated with UNICORE portal.
 *
 * @author R.Kluszczynski
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TemplatePluginMainView extends AbstractView {
    private static final Logger log = Logger.getLogger(TemplatePluginMainView.class);

    public static final String PORTAL_URL_FRAGMENT = "template";

    private final PortalConfigurationSource configurationSource;
    private final UserProfilesManager profilesManager;
    private final PortalThreadPool threadPool;

    @Autowired
    public TemplatePluginMainView(PortalConfigurationSource configurationSource,
                                  MessageProvider messageProvider,
                                  PortalThreadPool threadPool,
                                  UserProfilesManager profilesManager) {
        super(messageProvider);
        this.configurationSource = configurationSource;
        this.threadPool = threadPool;
        this.profilesManager = profilesManager;
    }

    @Override
    protected com.vaadin.ui.Component initializeViewComponent() {
        setTitle(msgProvider.getMessage("TemplatePlugin.MainView.uiTitle"));

        final VerticalLayout main = new VerticalLayout();
        main.addComponent(
                new Label("Template Plugin!", ContentMode.PREFORMATTED)
        );
        return main;
    }

    @Override
    public String getFragment() {
        return PORTAL_URL_FRAGMENT;
    }
}
