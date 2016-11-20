/*
 * Copyright (c) 2015 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed.ui;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import eu.unicore.portal.core.i18n.MessageProvider;
import pl.edu.icm.unicore.portal.sinusmed.ui.job.SinusJobPanel;

/**
 * Trivial component shown instead of the {@link SinusJobPanel} if the grid environment is not ready to submit
 * new jobs (i.e. all required services were not discovered yet, or are missing).
 * @author K. Benedyczak
 */
public class GridNotReadyComponent extends CustomComponent
{
	public GridNotReadyComponent(MessageProvider msg)
	{
		Label info = new Label(msg.getMessage("GridNotReadyComponent.info"));
		VerticalLayout main = new VerticalLayout();
		main.addComponent(info);
		setCompositionRoot(main);
	}
}
