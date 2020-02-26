/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Dalton <delps1001@gmail.com>
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

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.util.QuantityFormatter;


public class RunecraftingProfitOverlay extends Overlay
{
	private final RunecraftingProfitPlugin plugin;
	private final RunecraftingProfitConfig config;
	private final PanelComponent panelComponent = new PanelComponent();
	RunecraftingProfitSession session;

	@Inject
	RunecraftingProfitOverlay(RunecraftingProfitPlugin plugin, RunecraftingProfitConfig config)
	{
		setPosition(OverlayPosition.TOP_LEFT);
		this.plugin = plugin;
		this.config = config;
		this.session = plugin.getSession();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{

		//only display the overlay after the first RC animation occurs...
		if (!plugin.isFirstRunecraft())
		{
			return null;
		}

		//only display the UI if the player has done the RC animation in the last TIMEOUT_INTERVAL minutes
		if (!plugin.isDisplayOverlay())
		{
			return null;
		}

		panelComponent.getChildren().clear();


		if (plugin.isDisplayProfit())
		{
			if (config.displayIndividualRuneTypes())
			{
				for (Runes rune : Runes.values())
				{
					int profitForRuneType = session.getProfitPerRuneType().get(rune);
					if (profitForRuneType > 0)
					{
						panelComponent.getChildren().add(LineComponent.builder()
							.left(rune.getName() + ":")
							.right(QuantityFormatter.quantityToStackSize(profitForRuneType) + " gp")
							.build());
					}
				}
			}

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Total: ")
				.right(QuantityFormatter.quantityToStackSize(session.getTotalProfit()) + " gp")
				.build());
		}
		else
		{
			if (config.displayIndividualRuneTypes())
			{
				for (Runes rune : Runes.values())
				{
					int totalForRuneType = session.getNumberOfTotalRunesCrafted().get(rune.getItemId());
					if (totalForRuneType > 0)
					{
						panelComponent.getChildren().add(LineComponent.builder()
							.left(rune.getName() + ":")
							.right(Integer.toString(totalForRuneType))
							.build());
					}

				}
			}

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Total: ")
				.right(Integer.toString(session.getTotalRunesCrafted()))
				.build());
		}

		//display profit per hour if enabled
		if (config.displayProfitPerHour())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Profit/hr: ")
				.right(QuantityFormatter.quantityToStackSize((long) session.getTotalProfitPerHour()))
				.build());
		}


		return panelComponent.render(graphics);
	}
}