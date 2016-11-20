package pl.edu.icm.unicore.portal.sinusmed.io;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

/**
 * 
 * @author rudy
 */
public abstract class LayerSeriesBase implements LayerSeries
{
	protected BufferedImage renderLayer(byte data[], int outWidth, int outHeight,
			ResizeCrop resizeCrop, Interpolation interpolation,
			ValueMapping valueMapping, int backgroundColor)
	{
		int x1, y1, x2, y2;
		int w = getWidth();
		int h = getHeight();

		int[] pixels = new int[outWidth * outHeight];

		if (backgroundColor > 0)
		{
			for (int i = 0; i < pixels.length; ++i)
			{
				pixels[i] = backgroundColor;
			}
		}

		if (resizeCrop == ResizeCrop.NONE)
		{
			x1 = 0;
			y1 = 0;
			x2 = outWidth - 1;
			y2 = outHeight - 1;
		} else
		{
			if (w * outHeight > h * outWidth
					&& resizeCrop == ResizeCrop.FIT // <=
									// w/h >
									// outWidth/outHeight
					|| w * outHeight <= h * outWidth
					&& resizeCrop == ResizeCrop.FILL_FIT)
			{
				// fit x to outWidth, scale Y keeping aspect
				// ratio
				x1 = 0;
				x2 = outWidth - 1;
				int nh = h * outWidth / w;
				y1 = (outHeight - nh) / 2;
				y2 = y1 + nh - 1;
			} else
			{
				// fit y to outHeight, scale X keeping aspect
				// ratio
				y1 = 0;
				y2 = outHeight - 1;
				int nw = w * outHeight / h;
				x1 = (outWidth - nw) / 2;
				x2 = x1 + nw - 1;
			}
		}

		float sx1 = 0;
		float sy1 = 0;
		int ow = x2 - x1 + 1;
		int oh = y2 - y1 + 1;
		if (ow <= 0)
			ow = 1;
		if (oh <= 0)
			oh = 1;

		// scaling step
		float stepx = (w - 1 - sx1) / (x2 - x1 != 0 ? x2 - x1 : 1);
		float stepy = (h - 1 - sy1) / (y2 - y1 != 0 ? y2 - y1 : 1);

		// crop to out dimensions
		if (x1 < 0)
		{
			sx1 = -w * x1 / ow;
			x1 = 0;
		}
		if (x2 > outWidth - 1)
		{
			x2 = outWidth - 1;
		}
		if (y1 < 0)
		{
			sy1 = -h * y1 / oh;
			y1 = 0;
		}
		if (y2 > outHeight - 1)
		{
			y2 = outHeight - 1;
		}

		if (interpolation == Interpolation.NEAREST)
		{
			for (int y = y1; y <= y2; ++y)
			{
				int datay = (int) (sy1 + stepy * (y - y1));
				for (int x = x1; x <= x2; ++x)
				{
					int datax = (int) (sx1 + stepx * (x - x1));
					int dataa = 2 * (datay * w + datax);
					int col = (data[dataa] & 0xff)
							| (data[dataa + 1] & 0xff) << 8;
					
					pixels[y * outWidth + x] = mapValue(col, valueMapping, backgroundColor);
				}
			}
		} else if (interpolation == Interpolation.LINEAR)
		{
			for (int y = y1; y <= y2; ++y)
			{
				float datayf = sy1 + stepy * (y - y1);
				int dataa1 = (int) datayf;
				int dataa2 = dataa1 + 1;
				if (dataa2 >= h)
				{
					dataa2 = h - 1;
				}
				dataa1 *= w;
				dataa2 *= w;
				int ya = (int) (256 * (datayf - (int) datayf));
				for (int x = x1; x <= x2; ++x)
				{
					float dataxf = sx1 + stepx * (x - x1);
					int datax1 = (int) dataxf;
					int datax2 = datax1 + 1;
					if (datax2 >= w)
					{
						datax2 = w - 1;
					}
					int xa = (int) (256 * (dataxf - datax1));
					int col1, col2, col12, col34, dataa;
					dataa = 2 * (dataa1 + datax1);
					col1 = (data[dataa] & 0xff) | (data[dataa + 1] & 0xff) << 8;
					dataa = 2 * (dataa1 + datax2);
					col2 = (data[dataa] & 0xff) | (data[dataa + 1] & 0xff) << 8;
					col12 = ((col1 << 8) + xa * (col2 - col1)) >> 8;

					dataa = 2 * (dataa2 + datax1);
					col1 = (data[dataa] & 0xff) | (data[dataa + 1] & 0xff) << 8;
					dataa = 2 * (dataa2 + datax2);
					col2 = (data[dataa] & 0xff) | (data[dataa + 1] & 0xff) << 8;
					col34 = ((col1 << 8) + xa * (col2 - col1)) >> 8;

					int col = ((col12 << 8) + ya * (col34 - col12)) >> 8;

					pixels[y * outWidth + x] = mapValue(col, valueMapping, backgroundColor);
				}
			}
		}

		int[] bitMasks = new int[] { 0xff0000, 0xff00, 0xff, 0xff000000 };
		SinglePixelPackedSampleModel sm = new SinglePixelPackedSampleModel(
				DataBuffer.TYPE_INT, outWidth, outHeight, bitMasks);
		DataBufferInt db = new DataBufferInt(pixels, pixels.length);
		WritableRaster raster = Raster.createWritableRaster(sm, db, new Point());
		BufferedImage im = new BufferedImage(ColorModel.getRGBdefault(), raster, false,
				null);
		return im;
	}
	
	/**
	 * Maps 2b grayscale value to RGB
	 * @param valueMapping
	 * @return
	 */
	private int mapValue(int col, ValueMapping valueMapping, int bgCol)
	{
		if (col == bgCol)
			return 0x00000000;
		switch (valueMapping)
		{
		case CUT:
			if (col > 255)
				col = 255;
			else if (col < 0)
				col = 0;
			return 0xff000000 | (col << 16)	| (col << 8);
		case TO_RGB:
			col >>= 4;
			if (col > 255)
				col = 255;
			else if (col < 0)
				col = 0;
			int p = col % 3;
			int mov = 8 * p;
			return 0xff000000 | (col << mov);
		case NORMALIZE_12BIT:
		default:
			col >>= 4;
			if (col > 255)
				col = 255;
			else if (col < 0)
				col = 0;			
			return 0xff000000 | (col << 16)	| (col << 8) | col;
		}
		
	}
}
