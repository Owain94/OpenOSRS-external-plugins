/*
 * Copyright (c) 2019 Owain van Brakel <https://github.com/Owain94>
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
package com.owain.runecraftingprofit;

import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.QuantityFormatter;
import org.apache.commons.lang3.tuple.Pair;

@Singleton
class RunecraftingProfitOverlay extends OverlayPanel
{
	private static final Color HIGHLIGHT_COLOR = new Color(255, 200, 0, 255);
	private static final Comparator<Map.Entry<Runes, Long>> CMP = Map.Entry.comparingByValue();
	static final String RUNECRAFT_PROFIT_RESET = "Reset";

	private final RunecraftingProfitPlugin plugin;
	private final RunecraftingProfitConfig config;

	@Inject
	private RunecraftingProfitOverlay(final RunecraftingProfitPlugin plugin, final RunecraftingProfitConfig config)
	{
		super(plugin);

		setPosition(OverlayPosition.TOP_LEFT);

		this.plugin = plugin;
		this.config = config;

		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Runecrafting profit overlay"));
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY, RUNECRAFT_PROFIT_RESET, "Runecrafting profit overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		RunecraftingProfitSession session = plugin.getSession();
		if (session == null)
		{
			return null;
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Runecrafting profit")
			.color(Color.GREEN)
			.build());

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.CENTER, TableAlignment.RIGHT);
		tableComponent.addRow(
			ColorUtil.prependColorTag("Rune", HIGHLIGHT_COLOR),
			ColorUtil.prependColorTag("Crafted", HIGHLIGHT_COLOR),
			ColorUtil.prependColorTag("Profit", HIGHLIGHT_COLOR));

		if (config.displayIndividualRuneTypes())
		{
			List<Map.Entry<Runes, Long>> sorted = new LinkedList<>(session.getRuneProfit().entrySet());
			sorted.sort(CMP.reversed());
			Map<Runes, Integer> craftedRunes = session.getCraftedRunes();

			for (Map.Entry<Runes, Long> runes : sorted)
			{
				Runes rune = runes.getKey();
				int crafted = craftedRunes.get(rune);

				if (crafted > 0)
				{
					long price = session.getRunePrices().get(rune) * crafted;
					tableComponent.addRow(rune.toString(), String.valueOf(crafted), QuantityFormatter.quantityToStackSize(price) + " gp");
				}
			}
		}

		Pair<Integer, Long> totalCrafted = session.getTotalCrafted();

		if (totalCrafted.getLeft() > 0 && totalCrafted.getRight() > 0)
		{
			tableComponent.addRow("", "", "");
			tableComponent.addRow("Total", String.valueOf(totalCrafted.getLeft()), QuantityFormatter.quantityToStackSize(totalCrafted.getRight()) + " gp");
		}

		panelComponent.getChildren().add(tableComponent);

		return super.render(graphics);
	}
}
