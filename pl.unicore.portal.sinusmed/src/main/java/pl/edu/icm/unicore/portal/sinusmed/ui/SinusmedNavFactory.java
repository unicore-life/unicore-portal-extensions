/*
 * Copyright (c) 2015 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed.ui;

import eu.unicore.portal.ui.menu.AbstractSwitchToMenuEntryFactory;
import eu.unicore.portal.ui.menu.NavigationMenu;
import org.springframework.stereotype.Component;

/**
 * Switch to SinusMed view in main navigation.
 * @author K. Benedyczak
 */
@Component
public class SinusmedNavFactory extends AbstractSwitchToMenuEntryFactory
{
	public SinusmedNavFactory()
	{
		super("SinusMed.MenuEntry",
				NavigationMenu.MENU_ID, 
				null, 
				null, 
				SinusMedMainView.class.getName());
	}
}
