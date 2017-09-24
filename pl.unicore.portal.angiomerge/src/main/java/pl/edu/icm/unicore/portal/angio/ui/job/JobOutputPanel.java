/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.angio.ui.job;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import de.fzj.unicore.uas.client.JobClient;
import de.fzj.unicore.uas.client.StorageClient;
import eu.unicore.portal.core.PortalThreadPool;
import eu.unicore.portal.core.data.DataEndpoint;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.grid.ui.helpers.FileTransfersSpec;
import eu.unicore.portal.grid.ui.helpers.FileTransfersSpec.FileTransferSpec;
import eu.unicore.portal.grid.ui.helpers.UnicoreFileDownloaderComponent;
import eu.unicore.portal.grid.ui.helpers.UnicoreFileDownloaderComponent.TransferListener;
import eu.unicore.portal.ui.Styles;
import eu.unicore.portal.ui.data.VFSFileResource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.log4j.Logger;
import pl.edu.icm.unicore.portal.angio.AngioJobSpecification;
import pl.edu.icm.unicore.portal.angio.AngioMergeJSDLCreator;
import pl.edu.icm.unicore.portal.angio.StorageHelper;
import pl.edu.icm.unicore.portal.angio.ui.vis.ResultsVisualizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Component showing job's output. Includes a functionality to download the results if are not available locally.
 * 
 * @author K. Benedyczak
 */
public class JobOutputPanel extends CustomComponent
{
	private static final Logger log = Logger.getLogger(JobOutputPanel.class);

	private final MessageProvider msg;
	private UnicoreFileDownloaderComponent downloader;
	private Link downloadLink;
	private Label error;
	private ResultsVisualizer visualizer;

	private PortalThreadPool tpool;
	
	public JobOutputPanel(MessageProvider msg, PortalThreadPool tpool)
	{
		this.msg = msg;
		this.tpool = tpool;
		initUI();
	}
	
	private void initUI()
	{
		VerticalLayout main = new VerticalLayout();
		main.setSpacing(true);
		
		Label info = new Label(msg.getMessage("Angio.JobOutputPanel.info"));
		info.addStyleName(Reindeer.LABEL_H2);
		
		downloader = new UnicoreFileDownloaderComponent(true, msg, tpool);
		downloader.setVisible(false);
		downloadLink = new Link();
		downloadLink.setVisible(false);
		visualizer = new ResultsVisualizer();
		visualizer.setVisible(false);
		visualizer.setWidth(100, Unit.PERCENTAGE);
		visualizer.setHeight(600, Unit.PIXELS);
		
		error = new Label();
		error.addStyleName(Styles.ERROR_COLOR);
		error.setVisible(false);
		
		main.addComponents(info, error, downloader, downloadLink, visualizer);
		setCompositionRoot(main);
	}
	
	public void setJob(DataEndpoint workspaceLocation, 
			JobClient jobClient, AngioJobSpecification jobSpec) throws FileSystemException, UnsupportedEncodingException {
		FileObject jobDirectory = StorageHelper.getWorkspaceJobDirectory(workspaceLocation, 
				jobSpec.getInputId());
		final FileObject outputDirectory = jobDirectory.resolveFile(AngioMergeJSDLCreator.OUTPUT_DIRECTORY);
		outputDirectory.createFolder();
		final FileObject output = outputDirectory.resolveFile(AngioMergeJSDLCreator.OUTPUT);
		
		StorageClient storageClient;
		try
		{
			storageClient = jobClient.getUspaceClient();
		} catch (Exception e)
		{
			log.error("Creating USpace client problem", e);
			setError(msg.getMessage("Angio.JobOutputPanel.errorDownloadingFile", e.getMessage()));
			return;
		}

		FileTransfersSpec toDownload = new FileTransfersSpec();
		if (!output.exists())
			toDownload.add(AngioMergeJSDLCreator.OUTPUT, StorageHelper.localFileToPath(output));
		
		downloader.setTransferListener(new TransferListener()
		{
			private boolean error = false;
			
			@Override
			public boolean finished(FileTransferSpec fileTransfer)
			{
				return fileTransfer.getSource().equals(AngioMergeJSDLCreator.OUTPUT);
			}

			@Override
			public void allCompleted()
			{
				if (!error)
					showVis(output, outputDirectory);
			}

			@Override
			public void failed(FileTransferSpec fileTransfer, Exception e)
			{
				log.error("Download problem", e);
				error = true;
				safeSetError(msg.getMessage("Angio.JobOutputPanel.errorDownloadingFile", e.getMessage()));
				
			}
		});
		
		if (toDownload.getSpecs().isEmpty())
		{
			File file = new File(StorageHelper.localFileToPath(output));
			downloadLink.setCaption(msg.getMessage("Angio.JobOutputPanel.downloadOutput", 
					file.getName()));
			downloadLink.setResource(new VFSFileResource(output));
			downloadLink.setVisible(true);
			showVis(output, outputDirectory);
		} else
		{
			downloader.setVisible(true);
			downloader.start(storageClient, toDownload);
		}
	}
	
	private void unpack(FileObject archiveVFS, FileObject outputDirVFS) throws IOException
	{
		File archive = new File(StorageHelper.localFileToPath(archiveVFS));
		File outputDir = new File(StorageHelper.localFileToPath(outputDirVFS));
		ZipFile zipFile = new ZipFile(archive);
		try
		{
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) 
			{
				ZipEntry entry = entries.nextElement();
				File entryDestination = new File(outputDir,  entry.getName());
				entryDestination.getParentFile().mkdirs();
				if (entry.isDirectory())
					entryDestination.mkdirs();
				else 
				{
					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new FileOutputStream(entryDestination);
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);
				}
			}
		} finally
		{
			zipFile.close();
		}
	}
	
	
	private void showVis(FileObject archive, FileObject outputDir)
	{
		try
		{
			unpack(archive, outputDir);
		} catch (IOException e)
		{
			log.error("Unpacking error", e);
			safeSetError(msg.getMessage("Angio.JobOutputPanel.errorExtractingResults", e.toString()));
			return;
		}
		
		visualizer.setSource(outputDir);
		visualizer.setVisible(true);
	}
	
	private void safeSetError(String msg)
	{
		VaadinSession.getCurrent().lock();
		try
		{
			setError(msg);
		} finally
		{
			VaadinSession.getCurrent().unlock();
		}
	}
	
	private void setError(String errorMsg)
	{
		error.setValue(errorMsg);
		error.setVisible(true);
	}
}
