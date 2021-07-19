package com.owain.chinmanager.ui.utils;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JSeparator;
import net.runelite.client.ui.ColorScheme;

public class Separator extends JSeparator
{
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		int width = this.getSize().width;
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(2));
		g2.setColor(ColorScheme.BRAND_BLUE);
		g2.drawLine(0, 0, width, 0);
	}
}
