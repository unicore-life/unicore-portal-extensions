/*
 * Copyright (c) 2015 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.openfoam;

import eu.unicore.portal.core.data.DataEndpoint;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.eclipse.jetty.util.URIUtil;

public class StorageHelper
{
	public static String resolveWorkspaceFile(DataEndpoint workspace, String id, String file)
	{
		FileObject resolvedFile;
		try
		{
			resolvedFile = workspace.getVFSRoot().resolveFile(id + "/" + file);
		} catch (Exception e)
		{
			throw new IllegalStateException("Can't resolve workspace file", e);
		}
		return localFileToPath(resolvedFile);
	}
	
	public static String localFileToPath(FileObject resolvedFile)
	{
		if (!(resolvedFile instanceof LocalFile))
			throw new IllegalStateException("File was not resolved to a local file");
		
		try
		{
			return URIUtil.compactPath(resolvedFile.getURL().getPath());
		} catch (FileSystemException e)
		{
			throw new IllegalStateException("File URL was not established", e);
		}
	}
	
	public static FileObject getWorkspaceJobDirectory(DataEndpoint workspace, String id)
	{
		FileObject resolvedDir;
		try
		{
			resolvedDir = workspace.getVFSRoot().resolveFile(id);
			resolvedDir.createFolder();
		} catch (Exception e)
		{
			throw new IllegalStateException("Can't resolve workspace job directory", e);
		}
		return resolvedDir;
	}
}
