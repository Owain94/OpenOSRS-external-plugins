/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.owain.chinmanager.overlay;

import com.google.inject.Inject;
import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ui.plugins.options.OptionsConfig;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class ManagerTileIndicatorsOverlay extends Overlay
{
	private final Client client;
	private final ChinManager chinManager;
	private final OptionsConfig optionsConfig;

	@Inject
	public ManagerTileIndicatorsOverlay(Client client, ChinManager chinManager, ConfigManager configManager)
	{
		this.client = client;
		this.chinManager = chinManager;
		this.optionsConfig = configManager.getConfig(OptionsConfig.class);

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (chinManager.getActiveSortedPlugins().size() == 0 || !optionsConfig.showOverlays())
		{
			return null;
		}

		if (ChinManagerPlugin.getHighlightDaxPath() != null)
		{
			WorldPoint previous = null;
			for (WorldPoint point : ChinManagerPlugin.getHighlightDaxPath())
			{
				if (previous != null)
				{
					lineBetweenTiles(graphics, previous, point);
				}
				previous = point;
			}
		}

		return null;
	}

	private void lineBetweenTiles(Graphics2D graphics, WorldPoint tile1, WorldPoint tile2)
	{
		if (tile1.getPlane() != client.getPlane())
		{
			return;
		}

		if (tile2.getPlane() != client.getPlane())
		{
			return;
		}

		LocalPoint lp1 = LocalPoint.fromWorld(client, tile1);
		LocalPoint lp2 = LocalPoint.fromWorld(client, tile2);

		if (lp1 == null || lp2 == null)
		{
			return;
		}

		Polygon poly1 = Perspective.getCanvasTilePoly(client, lp1);
		Polygon poly2 = Perspective.getCanvasTilePoly(client, lp2);

		if (poly1 == null || poly2 == null)
		{
			return;
		}

		graphics.setStroke(new BasicStroke(3));
		graphics.setColor(new Color(255, 100, 100));
		graphics.drawLine(
			(int) Math.round(poly1.getBounds().getCenterX()), (int) Math.round(poly1.getBounds().getCenterY()),
			(int) Math.round(poly2.getBounds().getCenterX()), (int) Math.round(poly2.getBounds().getCenterY())
		);
	}
}
