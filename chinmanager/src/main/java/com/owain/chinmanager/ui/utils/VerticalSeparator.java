package com.owain.chinmanager.ui.utils;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JSeparator;
import net.runelite.client.ui.ColorScheme;

public class VerticalSeparator extends JSeparator
{
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(15, super.getPreferredSize().height);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		int height = this.getSize().height;
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(1));
		g2.setColor(ColorScheme.BRAND_BLUE);
		g2.drawLine(13, 0, 13, height - 5);
	}
}
