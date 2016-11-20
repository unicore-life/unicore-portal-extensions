/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed;

import eu.unicore.portal.core.User;
import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class StorageUtil
{
	public static final String ORIGINAL_INPUT = "originalInputFile.zip";
	public static final String CONVERTED_INPUT = "inputFile.zip";
	public static final String OUTPUT = "sinusmed-output.zip";
	public static final String OUTPUT_EXTRACTED = "output-unpack";
	
	private SinusMedProperties config;
	private User user;

	public StorageUtil(SinusMedProperties config, User user)
	{
		this.config = config;
		this.user = user;
	}

	public File getUserWorkspace()
	{
		File workspaceDir = config.getFileValue(SinusMedProperties.WORKSPACE, true);
		File userWorkspaceDir = new File(workspaceDir, getUserFolderId());
		userWorkspaceDir.mkdirs();
		return userWorkspaceDir;
	}

	private String getUserFolderId()
	{
		String userId = user.getId();
		char id[];
		try
		{
			byte[] bytesOfMessage = userId.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(bytesOfMessage);
			id = Hex.encodeHex(thedigest);
		} catch (Exception e)
		{
			throw new IllegalStateException(e);
		}
		return new String(id);
	}
	
	public File getOriginalInputLocation(String jobId)
	{
		return getWorkspaceFileLocation(jobId, ORIGINAL_INPUT);
	}

	public File getConvertedInputLocation(String jobId)
	{
		return getWorkspaceFileLocation(jobId, CONVERTED_INPUT);
	}
	
	private File getWorkspaceFileLocation(String jobId, String file)
	{
		File inputDir = new File(getUserWorkspace(), jobId);
		inputDir.mkdirs();
		return new File(inputDir, file);
	}
	
	public static String getInputNameFromArchive(File inputArchive) throws ZipException, IOException
	{
		ZipFile zip = new ZipFile(inputArchive);
		try
		{
			Enumeration<? extends ZipEntry> entries = zip.entries();
			for (ZipEntry e = entries.nextElement(); entries.hasMoreElements(); e = entries.nextElement())
			{
				if (!e.isDirectory() && e.getName().endsWith(".rdata"))
					return e.getName();
			}
			throw new IOException("Input file with '.rdata' suffix not found in the input archive");
		} finally 
		{
			zip.close();
		}
	}
	
	public File getOutputLocation(String jobId)
	{
		File outputDir = new File(getUserWorkspace(), jobId);
		outputDir.mkdirs();
		return new File(outputDir, OUTPUT);
	}
	
	public File getUnpackedOutputLocation(String jobId)
	{
		File outputDir = new File(getUserWorkspace(), jobId);
		outputDir.mkdirs();
		return new File(outputDir, OUTPUT_EXTRACTED);
	}	
	
	public static String sizeAsHumanReadableString(long size)
	{
		if (size < 1024)
			return size+"B";
		if (size < 1024*1024)
			return round(((float)size/1024)) + "kB";
		return round(((float)size/(1024*1024))) + "MB";
	}
	
	private static String round(double number)
	{
		return new DecimalFormat("#.##").format(number);
	}
}
