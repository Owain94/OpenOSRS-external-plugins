package com.owain.chinmanager.overlay;

import com.google.inject.Inject;
import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ui.plugins.options.OptionsConfig;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@Slf4j
public class ManagerClickboxDebugOverlay extends Overlay
{
	private final Client client;
	private final ModelOutlineRenderer modelOutliner;
	private final ChinManager chinManager;
	private final OptionsConfig optionsConfig;

	@Inject
	public ManagerClickboxDebugOverlay(Client client, ModelOutlineRenderer modelOutlineRenderer, ChinManager chinManager, OptionsConfig optionsConfig)
	{
		this.client = client;
		this.modelOutliner = modelOutlineRenderer;
		this.chinManager = chinManager;
		this.optionsConfig = optionsConfig;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!log.isDebugEnabled())
		{
			return null;
		}

		if (modelOutliner == null || chinManager.getActivePlugins().size() == 0 || !optionsConfig.showOverlays())
		{
			return null;
		}

		if (!ChinManagerPlugin.debugReachableWorldAreas.isEmpty())
		{
			for (WorldPoint worldPoint : Set.copyOf(ChinManagerPlugin.debugReachableWorldAreas))
			{
				LocalPoint lp = LocalPoint.fromWorld(client, worldPoint);
				if (lp == null)
				{
					continue;
				}

				Polygon poly = Perspective.getCanvasTilePoly(client, lp);

				if (poly != null)
				{
					OverlayUtil.renderPolygon(graphics, poly, Color.BLACK);
				}
			}
		}

		if (!ChinManagerPlugin.debugReachableTiles.isEmpty())
		{
			for (Map.Entry<WorldPoint, Integer> objectMap : Map.copyOf(ChinManagerPlugin.debugReachableTiles).entrySet())
			{
				LocalPoint lp = LocalPoint.fromWorld(client, objectMap.getKey());
				if (lp == null)
				{
					continue;
				}

				Polygon poly = Perspective.getCanvasTilePoly(client, lp);

				if (poly != null)
				{
					OverlayUtil.renderPolygon(graphics, poly, Color.RED);
				}

				Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, objectMap.getValue() + "(" + objectMap.getKey().distanceTo(client.getLocalPlayer().getWorldLocation()) + ")", 0);

				if (canvasTextLocation != null)
				{
					OverlayUtil.renderTextLocation(graphics, canvasTextLocation, objectMap.getValue() + "(" + objectMap.getKey().distanceTo(client.getLocalPlayer().getWorldLocation()) + ")", Color.WHITE);
				}
			}
		}

		if (!ChinManagerPlugin.debugTileObjectMap.isEmpty())
		{
			for (Map.Entry<TileObject, Integer> objectMap : Map.copyOf(ChinManagerPlugin.debugTileObjectMap).entrySet())
			{
				modelOutliner.drawOutline(objectMap.getKey(), 1, new Color(255, 0, 255), 4);

				LocalPoint lp = LocalPoint.fromWorld(client, objectMap.getKey().getWorldLocation());

				Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, objectMap.getValue() + "(" + objectMap.getKey().getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) + ")", 0);

				if (canvasTextLocation != null)
				{
					OverlayUtil.renderTextLocation(graphics, canvasTextLocation, objectMap.getValue() + "(" + objectMap.getKey().getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) + ")", Color.WHITE);
				}
			}
		}

		return null;
	}
}
