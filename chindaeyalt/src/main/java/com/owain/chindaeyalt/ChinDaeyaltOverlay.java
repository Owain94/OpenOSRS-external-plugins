package com.owain.chindaeyalt;

import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import static com.owain.chindaeyalt.ChinDaeyaltPlugin.PLUGIN_NAME;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;

public final class ChinDaeyaltOverlay extends OverlayPanel
{
	private final ChinDaeyaltPlugin plugin;

	@Inject
	private ChinDaeyaltOverlay(final ChinDaeyaltPlugin plugin)
	{
		super(plugin);

		this.plugin = plugin;
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
	}

	@Override
	public Dimension render(final Graphics2D graphics)
	{
		if (plugin.isInRegion())
		{
			final TableComponent tableComponent = new TableComponent();
			tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

			panelComponent.getChildren().add(TitleComponent.builder()
				.text(PLUGIN_NAME)
				.color(Color.GREEN)
				.build());

			tableComponent.addRow("", "");
			tableComponent.addRow("Enabled (hotkey):", Boolean.toString(plugin.isEnabled()));

			panelComponent.getChildren().add(tableComponent);
		}

		return super.render(graphics);
	}
}
