/*
 * Copyright (c) 2015 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.angio.ui;

import eu.unicore.portal.ui.menu.AbstractSwitchToMenuEntryFactory;
import eu.unicore.portal.ui.menu.NavigationMenu;
import org.springframework.stereotype.Component;

/**
 * Switch to AngioMerge view in main navigation.
 * @author K. Benedyczak
 */
@Component
public class AngioMergeNavFactory extends AbstractSwitchToMenuEntryFactory
{
	public AngioMergeNavFactory()
	{
		super("Angio.MenuEntry",
				NavigationMenu.MENU_ID, 
				null, 
				null, 
				AngioMergeMainView.class.getName());
	}
}
