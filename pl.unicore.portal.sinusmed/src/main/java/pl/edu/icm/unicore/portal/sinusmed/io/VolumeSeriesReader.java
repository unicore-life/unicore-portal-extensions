package pl.edu.icm.unicore.portal.sinusmed.io;

import java.util.List;

/**
 * 
 * @author rudy
 */
public interface VolumeSeriesReader
{
	/**
	 * Returns list of different series available in given dataset.
	 * 
	 * @return List of initialized series (layered volumes)
	 */
	public List<LayerSeries> getSeries();
}
