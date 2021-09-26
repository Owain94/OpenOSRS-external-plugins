package com.owain.chinmanager.ui.utils;

import io.reactivex.rxjava3.annotations.NonNull;
import java.awt.Dimension;
import javax.swing.JPanel;
import net.runelite.client.ui.PluginPanel;

public class FixedWidthPanel extends JPanel
{
	@Override
	public @NonNull Dimension getPreferredSize()
	{
		return new Dimension(PluginPanel.PANEL_WIDTH, super.getPreferredSize().height);
	}
}
