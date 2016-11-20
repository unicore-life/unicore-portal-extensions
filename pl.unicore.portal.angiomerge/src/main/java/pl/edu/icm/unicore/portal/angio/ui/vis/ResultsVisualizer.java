/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.angio.ui.vis;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import eu.unicore.portal.core.server.WorkspaceAccessServlet;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

/**
 * Results viewer component server side. The only function is to load the external Java Script and to send 
 * it the output location.
 * 
 * @author K. Benedyczak
 */
@JavaScript({"three.min.js", "jquery-1.11.2.min.js", "OrbitControls.js", "dat.gui.min.js", 
	"medviewww.js", "resultsVisualizer.js"})
public class ResultsVisualizer extends AbstractJavaScriptComponent
{
	public void setSource(FileObject outputDir)
	{
		String base = outputDir.getName().getBaseName();
		String parent;
		try
		{
			parent = outputDir.getParent().getName().getBaseName();
		} catch (FileSystemException e)
		{
			throw new IllegalStateException("Can't establish parent of output directory", e);
		}
		String path = WorkspaceAccessServlet.CONTEXT_PATH + "/" + parent + "/" + base;
		getState().outputPath = path;
	}
	
	@Override
	protected VisualizationState getState()
	{
		return (VisualizationState) super.getState();
	}
}
