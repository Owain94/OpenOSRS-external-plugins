package com.owain.chinmanager.overlay;

import com.google.inject.Inject;
import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ui.plugins.options.OptionsConfig;
import com.owain.chinmanager.utils.Overlays;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
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
	public ManagerClickboxDebugOverlay(Client client, ModelOutlineRenderer modelOutlineRenderer, ChinManager chinManager, ConfigManager configManager)
	{
		this.client = client;
		this.modelOutliner = modelOutlineRenderer;
		this.chinManager = chinManager;
		this.optionsConfig = configManager.getConfig(OptionsConfig.class);

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

		if (modelOutliner == null || chinManager.getActiveSortedPlugins().size() == 0 || !optionsConfig.showOverlays())
		{
			return null;
		}

		Set<WorldPoint> reachableWorldAreas = Overlays.getDebugReachableWorldAreas();
		Map<WorldPoint, Integer> reachableTiles = Overlays.getDebugReachableTiles();
		Map<TileObject, Integer> tileObjectMap = Overlays.getDebugTileObjectMap();

		if (!reachableWorldAreas.isEmpty())
		{
			for (WorldPoint worldPoint : Set.copyOf(reachableWorldAreas))
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

		Player localPlayer = client.getLocalPlayer();

		if (!reachableTiles.isEmpty())
		{
			for (Map.Entry<WorldPoint, Integer> objectMap : Map.copyOf(reachableTiles).entrySet())
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

				if (localPlayer == null)
				{
					continue;
				}

				Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, objectMap.getValue() + "(" + objectMap.getKey().distanceTo(localPlayer.getWorldLocation()) + ")", 0);

				if (canvasTextLocation != null)
				{
					OverlayUtil.renderTextLocation(graphics, canvasTextLocation, objectMap.getValue() + "(" + objectMap.getKey().distanceTo(localPlayer.getWorldLocation()) + ")", Color.WHITE);
				}
			}
		}

		if (!tileObjectMap.isEmpty())
		{
			for (Map.Entry<TileObject, Integer> objectMap : Map.copyOf(tileObjectMap).entrySet())
			{
				modelOutliner.drawOutline(objectMap.getKey(), 1, new Color(255, 0, 255), 4);

				LocalPoint lp = LocalPoint.fromWorld(client, objectMap.getKey().getWorldLocation());

				if (lp == null || localPlayer == null)
				{
					continue;
				}

				Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, objectMap.getValue() + "(" + objectMap.getKey().getWorldLocation().distanceTo(localPlayer.getWorldLocation()) + ")", 0);

				if (canvasTextLocation != null)
				{
					OverlayUtil.renderTextLocation(graphics, canvasTextLocation, objectMap.getValue() + "(" + objectMap.getKey().getWorldLocation().distanceTo(localPlayer.getWorldLocation()) + ")", Color.WHITE);
				}
			}
		}

		return null;
	}
}
