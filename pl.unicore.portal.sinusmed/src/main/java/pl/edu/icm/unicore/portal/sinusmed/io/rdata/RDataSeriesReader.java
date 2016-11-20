package pl.edu.icm.unicore.portal.sinusmed.io.rdata;

import pl.edu.icm.unicore.portal.sinusmed.io.LayerSeries;
import pl.edu.icm.unicore.portal.sinusmed.io.VolumeSeriesReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author rudy
 */
public class RDataSeriesReader implements VolumeSeriesReader
{
	protected RDataSeries series;

	public RDataSeriesReader(File f) throws IOException
	{
		series = new RDataSeries(f);
	}

	public RDataSeriesReader(File f, File header) throws IOException
	{
		series = new RDataSeries(f, header);
	}

	@Override
	public List<LayerSeries> getSeries()
	{
		ArrayList<LayerSeries> list = new ArrayList<LayerSeries>();
		list.add(series);
		return list;
	}
}
