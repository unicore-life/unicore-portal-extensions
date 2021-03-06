/*
 * Copyright (c) 2017, ICM University of Warsaw. All rights reserved.
 * See LICENSE file for licensing information.
 */
package pl.edu.icm.unicore.portal.template.ui;

import eu.unicore.portal.ui.menu.AbstractSwitchToMenuEntryFactory;
import eu.unicore.portal.ui.menu.NavigationMenu;
import org.springframework.stereotype.Component;

/**
 * Switch to plugin view in main navigation.
 *
 * @author R.Kluszczynski
 */
@Component
public class TemplatePluginNavFactory extends AbstractSwitchToMenuEntryFactory {
    public TemplatePluginNavFactory() {
        super("TemplatePlugin.MenuEntry",
                NavigationMenu.MENU_ID,
                null,
                null,
                TemplatePluginMainView.class.getName());
    }
}
