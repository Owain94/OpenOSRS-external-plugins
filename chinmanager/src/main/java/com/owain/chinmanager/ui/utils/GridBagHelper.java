package com.owain.chinmanager.ui.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class GridBagHelper
{
	private static final Insets DEFAULT_INSETS = new Insets(5, 0, 0, 0);

	@SuppressWarnings("SameParameterValue")
	public static void addComponent(Container container, Component component, int gridx, int gridy,
									int gridwidth, int gridheight, int anchor, int fill)
	{
		addComponent(container, component, gridx, gridy, gridwidth, gridheight, anchor, fill, DEFAULT_INSETS);
	}

	public static void addComponent(Container container, Component component, int gridx, int gridy,
									int gridwidth, int gridheight, int anchor, int fill, Insets insets)
	{
		addComponent(container, component, gridx, gridy, gridwidth, gridheight, anchor, fill, 1.0, 1.0, DEFAULT_INSETS);
	}

	public static void addComponent(Container container, Component component, int gridx, int gridy,
									int gridwidth, int gridheight, int anchor, int fill, double weightx,
									double weighty)
	{
		addComponent(container, component, gridx, gridy, gridwidth, gridheight, anchor, fill, weightx, weighty, DEFAULT_INSETS);
	}

	public static void addComponent(Container container, Component component, int gridx, int gridy,
									int gridwidth, int gridheight, int anchor, int fill, double weightx,
									double weighty, Insets insets)
	{
		GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty,
			anchor, fill, insets, 0, 0);
		container.add(component, gbc);
	}
}
