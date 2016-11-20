package pl.edu.icm.unicore.portal.sinusmed.io;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * /**
 * Example use:
 * <code>
   RDataSeries rdata = new RDataSeries(new File("data.rdata"));				
   BufferedImage bi = rdata.readZLayerImage(0, 1099, 1099, ResizeCrop.NONE,
				Interpolation.LINEAR, LayerSeries.ValueMapping.NORMALIZE_12BIT,
				0xffff00ff); 
   </code>
 * @author rudy
 */
public interface LayerSeries
{
	/**
	 * Resize/Crop policy for layer rendering
	 */
	public enum ResizeCrop
	{
		/**
		 * Fit to output image, keep aspect ratio, add borders if needed
		 */
		FIT,
		/**
		 * Fit to fill whole output image, keep aspect ratio
		 */
		FILL_FIT,
		/**
		 * Fit to output image exactly
		 */
		NONE
	};

	/**
	 * Interpolation type used during layer rendering: NEAREST - fast LINEAR
	 * - slower, better quality
	 */
	public enum Interpolation
	{
		NEAREST, LINEAR
	};

	/**
	 * Mapping for short to byte conversion of data points
	 */
	public enum ValueMapping
	{
		/**
		 * Simple cut of the value
		 */
		CUT, 
		/**
		 * map to RGB grayscale
		 */
		NORMALIZE_12BIT, 
		/**
		 * map to colorful RGB so that the grayscale spans 3 different color ranges. 
		 */
		TO_RGB
	};

	/**
	 * Returns name of this series that may be used as it's label.
	 * 
	 * @return Series label name
	 */
	public String getName();

	/**
	 * Returns path of this series inside it's source dataset (eg DICOM
	 * tree).
	 * 
	 * @return Series of path nodes from root to one representing this
	 *         series
	 */
	public List<String> getPath();

	/**
	 * Returns first (x) dimensions of bounding volume in voxel space
	 * 
	 * @return
	 */
	public int getWidth();

	/**
	 * Returns second (y) dimensions of bounding volume in voxel space
	 * 
	 * @return
	 */
	public int getHeight();

	/**
	 * Returns third (z) dimensions of bounding volume in voxel space. May
	 * be interpreted as number of layers in series.
	 * 
	 * @return
	 */
	public int getDepth();

	/**
	 * Renders layer (z) onto BufferedImage with given dimensions
	 * (outWidth,outHeight), using preferable default settings.
	 * 
	 * @param z
	 * @param outWidth
	 * @param outHeight
	 * @return
	 * @throws IOException 
	 */
	public BufferedImage readZLayerImage(int z, int outWidth, int outHeight) throws IOException;

	/**
	 * Renders layer (z) onto BufferedImage with given dimensions
	 * (outWidth,outHeight).
	 * 
	 * @param z
	 *                layer number to render
	 * @param outWidth
	 *                output image width
	 * @param outHeight
	 *                output image height
	 * @param resizeCrop
	 *                resize policy
	 * @param interpolation
	 *                interpolation used by resize operation (impacts
	 *                execution speed)
	 * @param valueMapping
	 *                method used to cast sample values to 8bit image
	 *                channels
	 * @param backgroundColor
	 *                color (in 32bit ARGB format), used for background (eg
	 *                when resizeCrop == FIT)
	 * @return 32-bit BufferedImage of size (outWidth,outHeight)
	 * @throws IOException 
	 */
	public BufferedImage readZLayerImage(int z, int outWidth, int outHeight,
			ResizeCrop resizeCrop, Interpolation interpolation,
			ValueMapping valueMapping, int backgroundColor) throws IOException;
}
