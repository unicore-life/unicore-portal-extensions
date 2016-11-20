/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed.ui;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Slider;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * vertical Slider with 2 +/- buttons. Dunno why (Vaadin bug?) this doesn't work well when programmed as CustomComponent.
 * @author K. Benedyczak
 */
public class EnhancedSlider extends CustomComponent
{
	private Slider slider;
	
	public EnhancedSlider(String label, ValueChangeListener vcl)
	{
		VerticalLayout vl = new VerticalLayout();
		vl.setSizeFull();
		
		Label top = new Label(label);
		vl.addComponent(top);
		vl.setComponentAlignment(top, Alignment.TOP_CENTER);

		Button plus = new Button("+");
		plus.addStyleName(Reindeer.BUTTON_SMALL);
		plus.setWidth(3, Unit.EM);
		Button minus = new Button("-");
		minus.addStyleName(Reindeer.BUTTON_SMALL);
		minus.setWidth(3, Unit.EM);
		vl.addComponents(plus, minus);
		vl.setComponentAlignment(plus, Alignment.TOP_CENTER);
		vl.setComponentAlignment(minus, Alignment.TOP_CENTER);
		
		slider = new Slider(0, 100);
		slider.setOrientation(SliderOrientation.VERTICAL);
		slider.setImmediate(true);
		slider.addValueChangeListener(vcl);
		slider.setSizeFull();
		vl.addComponent(slider);
		vl.setComponentAlignment(slider, Alignment.TOP_CENTER);
		
		
		plus.addClickListener(new ClickListener()
		{
			@Override
			public void buttonClick(ClickEvent event)
			{
				double cur = slider.getValue();
				cur += 1;
				if (cur > slider.getMax())
					cur = slider.getMax();
				slider.setValue(cur);
			}
		});
		minus.addClickListener(new ClickListener()
		{
			@Override
			public void buttonClick(ClickEvent event)
			{
				double cur = slider.getValue();
				cur -= 1;
				if (cur < slider.getMin())
					cur = slider.getMin();
				slider.setValue(cur);
			}
		});
		
		vl.setExpandRatio(slider, 1f);
		setCompositionRoot(vl);
	}
	
	public Slider getSlider()
	{
		return slider;
	}
}
