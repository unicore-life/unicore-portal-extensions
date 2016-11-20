/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed.ui.job;

import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import de.fzj.unicore.uas.client.JobClient;
import de.fzj.unicore.uas.client.StorageClient;
import eu.unicore.portal.core.PortalThreadPool;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.grid.ui.helpers.FileTransfersSpec;
import eu.unicore.portal.grid.ui.helpers.FileTransfersSpec.FileTransferSpec;
import eu.unicore.portal.grid.ui.helpers.UnicoreFileDownloaderComponent;
import eu.unicore.portal.grid.ui.helpers.UnicoreFileDownloaderComponent.TransferListener;
import eu.unicore.portal.ui.Styles;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import pl.edu.icm.unicore.portal.sinusmed.SinusMedJSDLCreator;
import pl.edu.icm.unicore.portal.sinusmed.SinusMedJobSpecification;
import pl.edu.icm.unicore.portal.sinusmed.StorageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
		
		Label info = new Label(msg.getMessage("SinusMed.JobOutputPanel.info"));
		info.addStyleName(Reindeer.LABEL_H2);
		
		downloader = new UnicoreFileDownloaderComponent(true, msg, tpool);
		downloader.setVisible(false);
		downloadLink = new Link();
		downloadLink.setVisible(false);
		visualizer = new ResultsVisualizer(msg);
		visualizer.setVisible(false);
		
		error = new Label();
		error.addStyleName(Styles.ERROR_COLOR);
		error.setVisible(false);
		
		main.addComponents(info, error, downloader, downloadLink, visualizer);
		setCompositionRoot(main);
	}
	
	public void setJob(StorageUtil storageUtil, JobClient jobClient, SinusMedJobSpecification jobSpec) 
	{
		String inputId = jobSpec.getInputId();
		final File output = storageUtil.getOutputLocation(inputId);
		final File outputDir = storageUtil.getUnpackedOutputLocation(inputId);
		final File input = storageUtil.getConvertedInputLocation(inputId);
		StorageClient storageClient;
		try
		{
			storageClient = jobClient.getUspaceClient();
		} catch (Exception e)
		{
			log.error("Creating USpace client problem", e);
			setError(msg.getMessage("SinusMed.JobOutputPanel.errorDownloadingFile", e.getMessage()));
			return;
		}

		FileTransfersSpec toDownload = new FileTransfersSpec();
		if (!input.exists())
			toDownload.add(SinusMedJSDLCreator.CONVERTED_INPUT, input.toString());
		if (!output.exists())
			toDownload.add(SinusMedJSDLCreator.OUTPUT, output.toString());
		
		downloader.setTransferListener(new TransferListener()
		{
			private boolean error = false;
			
			@Override
			public boolean finished(FileTransferSpec fileTransfer)
			{
				return fileTransfer.getSource().equals(SinusMedJSDLCreator.OUTPUT);
			}

			@Override
			public void allCompleted()
			{
				if (!error)
					showVis(output, outputDir, input);
			}

			@Override
			public void failed(FileTransferSpec fileTransfer, Exception e)
			{
				log.error("Download problem", e);
				error = true;
				safeSetError(msg.getMessage("SinusMed.JobOutputPanel.errorDownloadingFile", e.getMessage()));
				
			}
		});
		
		if (toDownload.getSpecs().isEmpty())
		{
			downloadLink.setCaption(msg.getMessage("SinusMed.JobOutputPanel.downloadOutput", output.getName()));
			downloadLink.setResource(new FileResource(output));
			downloadLink.setVisible(true);
			showVis(output, outputDir, input);
		} else
		{
			downloader.setVisible(true);
			downloader.start(storageClient, toDownload);
		}
	}
	
	private void unpack(File archive, File outputDir) throws IOException
	{
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
	
	
	private void showVis(File archive, File outputDir, File input)
	{
		File unpackedInput;
		try
		{
			unpack(input, outputDir);
			String inputFileName = StorageUtil.getInputNameFromArchive(input);
			unpackedInput = new File(outputDir, inputFileName);
			unpack(archive, outputDir);
		} catch (IOException e)
		{
			log.error("Unpacking error", e);
			safeSetError(msg.getMessage("SinusMed.JobOutputPanel.errorExtractingResults", e.toString()));
			return;
		}
		
		try
		{
			visualizer.setSource(unpackedInput, outputDir);
			visualizer.setVisible(true);
		} catch (IOException e)
		{
			log.error("Parsing problem", e);
			setError(msg.getMessage("SinusMed.JobOutputPanel.errorParsingResults", e.toString()));
		}
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
