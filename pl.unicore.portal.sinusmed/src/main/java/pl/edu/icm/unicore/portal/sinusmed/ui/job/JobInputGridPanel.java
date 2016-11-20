/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed.ui.job;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.grid.ui.browser.StorageFileChooser;
import eu.unicore.portal.grid.ui.browser.VFSFileChooser;
import eu.unicore.portal.grid.ui.browser.VFSFileChooser.SelectionCallback;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Allows to select a Grid input file.
 * @author K. Benedyczak
 */
public class JobInputGridPanel extends CustomComponent
{
	private final MessageProvider msg; 
	private URI gridInputFile = null;
	private Label inputInfo;
	
	public JobInputGridPanel(MessageProvider msg)
	{
		this.msg = msg;
		initUI();
	}
	
	private void initUI()
	{
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		
		inputInfo = new Label(msg.getMessage("SinusMed.GridInputPanel.noFile"));
		
		Button gridFileChooserButton = new Button(msg.getMessage("SinusMed.GridInputPanel.selectFile"));
		gridFileChooserButton.addClickListener(new ClickListener()
		{
			@Override
			public void buttonClick(ClickEvent event)
			{
				showChooser();
			}
		});
		
		vl.addComponents(gridFileChooserButton, inputInfo);
		setCompositionRoot(vl);
	}

	private void showChooser()
	{
		StorageFileChooser chooser = new StorageFileChooser(msg, 
				msg.getMessage("SinusMed.GridInputPanel.selectFileStorage"),
				msg.getMessage("SinusMed.GridInputPanel.selectFile"), 
				false, 
				new VFSFileChooser.GenericSelectionFilter(FileType.FILE), 
				new SelectionCallback()
				{
					@Override
					public void selected(FileObject selected)
					{
						fileSelected(selected);
					}
				});
		chooser.show();
	}
	
	private void fileSelected(FileObject filen)
	{
		try
		{
			FileName name = filen.getName();
			String nameStr = filen.toString().substring(name.getScheme().length());
			gridInputFile = new URI("BFT:https" + nameStr);
		} catch (URISyntaxException e)
		{
			throw new IllegalStateException("Can't parse remote file URL", e);
		}
		inputInfo.setValue(msg.getMessage("SinusMed.GridInputPanel.selectedFile",
				gridInputFile));
	}
	
	public URI getGridInputFile()
	{
		return gridInputFile;
	}
}
