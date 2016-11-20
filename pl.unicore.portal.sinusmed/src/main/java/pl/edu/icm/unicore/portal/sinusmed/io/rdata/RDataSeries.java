package pl.edu.icm.unicore.portal.sinusmed.io.rdata;

import org.apache.log4j.Logger;
import pl.edu.icm.unicore.portal.sinusmed.io.LayerSeriesBase;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


public class RDataSeries extends LayerSeriesBase
{
	private static final Logger log = Logger.getLogger(RDataSeries.class);

	private int w, h, d;

	private float scale[] = new float[] { 1, 1, 1 };

	private File dataSrc;

	public RDataSeries(File dataSrc) throws FileNotFoundException, IOException
	{
		this(dataSrc, new File(dataSrc.getAbsolutePath() + ".header"));
	}

	public RDataSeries(File dataSrc, File header) throws FileNotFoundException, IOException
	{
		this.dataSrc = dataSrc;

		BufferedReader inH = new BufferedReader(new FileReader(header));
		int m = -1;
		while (true)
		{
			String tt = inH.readLine();
			if (tt == null)
				break;
			tt = tt.trim();
			if (tt.isEmpty())
			{
				continue;
			}

			if (m == -1)
			{
				if (tt.equals("SIZES:"))
				{
					m = 1;
				} else if (tt.equals("VOXEL_DIMS:"))
				{
					m = 2;
				} else if (tt.equals("START_POS:"))
				{
					m = 3;
				}
			} else
			{
				String[] v = tt.split("[ \t]+");
				switch (m)
				{
				case 1:
					w = Integer.parseInt(v[0]);
					h = Integer.parseInt(v[1]);
					d = Integer.parseInt(v[2]);
					break;
				case 2:
					scale[0] = Float.parseFloat(v[0]);
					scale[1] = Float.parseFloat(v[1]);
					scale[2] = Float.parseFloat(v[2]);
					break;
				case 3:
					// ignore start pos
					break;
				default:
					break;
				}
				m = -1;
			}
		}
		long sd = dataSrc.length() - (long) w * h * d * 2;
		if (sd < 0)
		{
			log.warn("'" + dataSrc.getName()
					+ "': size not coherent with volume dimensions");
		}
		inH.close();
	}

	@Override
	public String getName()
	{
		return dataSrc.getName();
	}

	@Override
	public List<String> getPath()
	{
		List<String> path = new ArrayList<String>();
		path.add(dataSrc.getName());
		return path;
	}

	@Override
	public int getWidth()
	{
		return w;
	}

	@Override
	public int getHeight()
	{
		return h;
	}

	@Override
	public int getDepth()
	{
		return d;
	}

	@Override
	public BufferedImage readZLayerImage(int z, int outWidth, int outHeight) throws IOException
	{
		return readZLayerImage(z, outWidth, outHeight, ResizeCrop.FILL_FIT,
				Interpolation.NEAREST, ValueMapping.NORMALIZE_12BIT, 0);
	}

	@Override
	public BufferedImage readZLayerImage(int z, int outWidth, int outHeight,
			ResizeCrop resizeCrop, Interpolation interpolation,
			ValueMapping valueMapping, int backgroundColor) throws IOException
	{
		byte data[] = new byte[w * h * 2];

		if (z < 0)
			z = 0;
		if (z >= d)
			z = d - 1;

		RandomAccessFile f = new RandomAccessFile(dataSrc, "r");
		f.seek(z * w * h * 2);
		f.readFully(data);
		f.close();
		return renderLayer(data, outWidth, outHeight, resizeCrop, interpolation,
				valueMapping, backgroundColor);
	}
}