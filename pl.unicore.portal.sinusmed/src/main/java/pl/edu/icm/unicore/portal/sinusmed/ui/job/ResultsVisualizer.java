/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed.ui.job;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Slider;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.ui.Styles;
import org.apache.log4j.Logger;
import pl.edu.icm.unicore.portal.sinusmed.io.LayerSeries;
import pl.edu.icm.unicore.portal.sinusmed.io.LayerSeries.Interpolation;
import pl.edu.icm.unicore.portal.sinusmed.io.LayerSeries.ResizeCrop;
import pl.edu.icm.unicore.portal.sinusmed.io.LayerSeries.ValueMapping;
import pl.edu.icm.unicore.portal.sinusmed.io.VolumeSeriesReader;
import pl.edu.icm.unicore.portal.sinusmed.io.rdata.RDataSeriesReader;
import pl.edu.icm.unicore.portal.sinusmed.ui.EnhancedSlider;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Shows results viewer. It is possible to check the input, and the input with several overlays, including the
 * final simulation result. Aplha and z-layer can be controlled with sliders. 
 * @author K. Benedyczak
 */
public class ResultsVisualizer extends CustomComponent
{
	private static final Logger log = Logger.getLogger(ResultsVisualizer.class);

	private final MessageProvider msg;
	
	private enum Overlay {NONE, RESULT, UNCROP, MASKED, MASKED_CLOSED};
	
	private Image image;
	private Slider z;
	private Slider alpha;
	private EnhancedSlider enhAlphaSlider;
	private OptionGroup overlayG;
	private Label error;
	private LayerSeries source;
	private LayerSeries result;
	private LayerSeries resultUncrop;
	private LayerSeries resultMasked;
	private LayerSeries resultMaskedClosed;

	private EnhancedSlider enhZSlider;
	
	public ResultsVisualizer(MessageProvider msg)
	{
		this.msg = msg;
		initUI();
	}
	
	private void initUI()
	{
		ValueChangeListener vcl = new ValueChangeListener()
		{
			@Override
			public void valueChange(ValueChangeEvent event)
			{
				update();
			}
		};

		enhAlphaSlider = new EnhancedSlider(
				msg.getMessage("SinusMed.ResultsVisualizer.transparency"), vcl);
		alpha = enhAlphaSlider.getSlider();

		enhZSlider = new EnhancedSlider(msg.getMessage("SinusMed.ResultsVisualizer.zLayer"), vcl);
		z = enhZSlider.getSlider();
		
		overlayG = new OptionGroup();
		overlayG.setNullSelectionAllowed(false);
		for (Overlay ov: Overlay.values())
		{
			overlayG.addItem(ov);
			overlayG.setItemCaption(ov, msg.getMessage("SinusMed.ResultsVisualizer." + ov.name()));
		}
		overlayG.select(Overlay.RESULT);
		overlayG.setImmediate(true);
		overlayG.addValueChangeListener(vcl);
		image = new Image();
		
		error = new Label();
		error.addStyleName(Styles.ERROR_COLOR);
		
		GridLayout main = new GridLayout(4, 2);
		main.setSpacing(true);
		setCompositionRoot(main);
		
		main.addComponent(overlayG, 0, 0);
		main.addComponent(enhZSlider, 1, 0);
		main.addComponent(enhAlphaSlider, 2, 0);
		main.addComponent(image, 3, 0);
		main.addComponent(error, 0, 1, 3, 1);
		
		main.setComponentAlignment(enhZSlider, Alignment.TOP_CENTER);
		main.setComponentAlignment(enhAlphaSlider, Alignment.TOP_CENTER);
		
		main.setRowExpandRatio(0, 1);
		main.setColumnExpandRatio(2, 1);
	}
	
	public void setSource(File input, File outputDirectory) throws IOException
	{
		source = load(input);
		result = load(new File(outputDirectory, "atlas.rdata"));
		resultMasked = load(new File(outputDirectory, "atlas-masked.rdata"));
		resultMaskedClosed = load(new File(outputDirectory, "atlas-masked-closed.rdata"));
		resultUncrop = load(new File(outputDirectory, "mask-uncrop.rdata"));
		z.setMax(source.getDepth()-1);
		z.setValue((double)source.getDepth()/2);
		enhZSlider.setHeight(source.getHeight(), Unit.PIXELS);
		alpha.setValue(50.0);
		enhAlphaSlider.setHeight(source.getHeight(), Unit.PIXELS);
		
		image.setWidth(source.getWidth(), Unit.PIXELS);
		image.setHeight(source.getHeight(), Unit.PIXELS);
		update();
	}
	
	private LayerSeries load(File f) throws IOException
	{
		VolumeSeriesReader reader = new RDataSeriesReader(f);
		return reader.getSeries().get(0);
	}
	
	private void update()
	{
		if (source == null)
			return;
		float alphaV = (float) (alpha.getValue()/100.0);
		int zV = (int)(double)z.getValue();
		int w = source.getWidth();
		int h = source.getHeight();
		LayerSeries overlayLs = getSelectedSeries();
		ValueMapping overlayMapping = getSelectedSeriesMapping();
		String id = overlayG.getValue().toString() + "_" + zV + "_" + ((int)(double)alpha.getValue());		

		BufferedImage bImage;
		try
		{
			bImage = createImage(overlayLs, overlayMapping, alphaV, zV, w, h);
		} catch (IOException e)
		{
			log.error(e);
			error.setValue(msg.getMessage("SinusMed.ResultsVisualizer.errorVisualize", e.toString()));
			return;
		}
		StreamResource sr = new StreamResource(new ImageBufferSource(bImage), id);
		image.setSource(sr);
	}
	
	private BufferedImage createImage(LayerSeries overlayLs, ValueMapping overlayMapping, 
			float alphaV, int zV, int w, int h) throws IOException
	{
		BufferedImage imageInput = source.readZLayerImage(zV, w, h, ResizeCrop.FILL_FIT,
				Interpolation.LINEAR, ValueMapping.NORMALIZE_12BIT, -1);
		if (overlayLs == null)
			return imageInput;

		BufferedImage imageR = overlayLs.readZLayerImage(zV, w, h, ResizeCrop.FILL_FIT,
				Interpolation.LINEAR, overlayMapping, 0);

		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaV);
		Graphics2D g2d = imageInput.createGraphics();
		g2d.setComposite(ac);
		g2d.drawImage(imageR, 0, 0, null);
		g2d.dispose();
		return imageInput;
	}
	
	private LayerSeries getSelectedSeries()
	{
		Overlay overlay = (Overlay) overlayG.getValue();
		switch (overlay)
		{
		case MASKED:
			return resultMasked;
		case MASKED_CLOSED:
			return resultMaskedClosed;
		case NONE:
			return null;
		case RESULT:
			return result;
		case UNCROP:
			return resultUncrop;
		}
		return null;
	}

	private ValueMapping getSelectedSeriesMapping()
	{
		Overlay overlay = (Overlay) overlayG.getValue();
		switch (overlay)
		{
		case NONE:
			return null;
		case MASKED:
		case RESULT:
		case MASKED_CLOSED:
			return ValueMapping.TO_RGB;
		case UNCROP:
			return ValueMapping.CUT;
		}
		return null;
	}
	
	private class ImageBufferSource implements StreamSource 
	{
		private BufferedImage bi;
		
		public ImageBufferSource(BufferedImage bi)
		{
			this.bi = bi;
		}

		@Override
		public InputStream getStream()
		{
			try
			{
				ByteArrayOutputStream imagebuffer = new ByteArrayOutputStream();
				ImageIO.write(bi, "png", imagebuffer);
				return new ByteArrayInputStream(imagebuffer.toByteArray());
			} catch (IOException e)
			{
				return null;
			}
		}
	}
}



