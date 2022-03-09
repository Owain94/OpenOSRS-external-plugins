package com.owain.chinmanager.overlay;

import com.google.inject.Inject;
import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ui.plugins.options.OptionsConfig;
import com.owain.chinmanager.utils.Overlays;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class ManagerClickboxOverlay extends Overlay
{
	private final ModelOutlineRenderer modelOutliner;
	private final ChinManager chinManager;
	private final OptionsConfig optionsConfig;

	@Inject
	public ManagerClickboxOverlay(ModelOutlineRenderer modelOutlineRenderer, ChinManager chinManager, ConfigManager configManager)
	{
		this.modelOutliner = modelOutlineRenderer;
		this.chinManager = chinManager;
		this.optionsConfig = configManager.getConfig(OptionsConfig.class);

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (modelOutliner == null || chinManager.getActiveSortedPlugins().size() == 0 || !optionsConfig.showOverlays())
		{
			return null;
		}

		if (Overlays.getHighlightActor() != null)
		{
			if (Overlays.getHighlightActor() instanceof NPC)
			{
				modelOutliner.drawOutline((NPC) Overlays.getHighlightActor(), 4, new Color(255, 100, 100), 4);
			}
			else if (Overlays.getHighlightActor() instanceof Player)
			{
				modelOutliner.drawOutline((Player) Overlays.getHighlightActor(), 4, new Color(255, 100, 100), 4);
			}
		}
		else if (Overlays.getHighlightItemLayer() != null)
		{
			modelOutliner.drawOutline(Overlays.getHighlightItemLayer(), 4, new Color(255, 100, 100), 4);
		}
		else if (Overlays.getHighlightTileObject() != null)
		{
			modelOutliner.drawOutline(Overlays.getHighlightTileObject(), 4, new Color(255, 100, 100), 4);
		}

		return null;
	}
}
