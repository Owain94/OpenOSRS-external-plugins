package com.owain.chinmanager.overlay;

import com.google.inject.Inject;
import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ui.plugins.options.OptionsConfig;
import io.reactivex.rxjava3.annotations.NonNull;
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
import org.jetbrains.annotations.Nullable;

public class ManagerClickboxOverlay extends Overlay
{
	private final ModelOutlineRenderer modelOutliner;
	private final ChinManager chinManager;
	private final OptionsConfig optionsConfig;

	@Inject
	public ManagerClickboxOverlay(ModelOutlineRenderer modelOutlineRenderer, ChinManager chinManager, @NonNull ConfigManager configManager)
	{
		this.modelOutliner = modelOutlineRenderer;
		this.chinManager = chinManager;
		this.optionsConfig = configManager.getConfig(OptionsConfig.class);

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Nullable
	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (modelOutliner == null || chinManager.getActivePlugins().size() == 0 || !optionsConfig.showOverlays())
		{
			return null;
		}

		if (ChinManagerPlugin.getHighlightActor() != null)
		{
			if (ChinManagerPlugin.getHighlightActor() instanceof NPC)
			{
				modelOutliner.drawOutline((NPC) ChinManagerPlugin.getHighlightActor(), 4, new Color(255, 100, 100), 4);
			}
			else if (ChinManagerPlugin.getHighlightActor() instanceof Player)
			{
				modelOutliner.drawOutline((Player) ChinManagerPlugin.getHighlightActor(), 4, new Color(255, 100, 100), 4);
			}
		}
		else if (ChinManagerPlugin.getHighlightItemLayer() != null)
		{
			modelOutliner.drawOutline(ChinManagerPlugin.getHighlightItemLayer(), 4, new Color(255, 100, 100), 4);
		}
		else if (ChinManagerPlugin.getHighlightTileObject() != null)
		{
			modelOutliner.drawOutline(ChinManagerPlugin.getHighlightTileObject(), 4, new Color(255, 100, 100), 4);
		}

		return null;
	}
}
