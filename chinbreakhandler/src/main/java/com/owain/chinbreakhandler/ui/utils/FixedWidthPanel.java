package com.owain.chinbreakhandler.ui.utils;

import java.awt.Dimension;
import javax.swing.JPanel;
import net.runelite.client.ui.PluginPanel;

public class FixedWidthPanel extends JPanel
{
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PluginPanel.PANEL_WIDTH, super.getPreferredSize().height);
	}
}
