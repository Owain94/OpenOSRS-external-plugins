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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Runecrafting Profit",
	description = "Shows various runecrafting stats"
)
@Slf4j
public class RunecraftingProfitPlugin extends Plugin
{
	private static final int RUNECRAFTING_ANIMATION = 791;

	@Inject
	private Client client;

	@Inject
	private RunecraftingProfitConfig config;

	@Inject
	private RunecraftingProfitOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Getter(AccessLevel.PACKAGE)
	private RunecraftingProfitSession session;

	private Multiset<Integer> inventorySnapshot;

	@Provides
	RunecraftingProfitConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RunecraftingProfitConfig.class);
	}

	@Override
	protected void startUp()
	{
		session = null;
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		session = null;
	}

	@Subscribe
	private void onOverlayMenuClicked(OverlayMenuClicked overlayMenuClicked)
	{
		OverlayMenuEntry overlayMenuEntry = overlayMenuClicked.getEntry();
		if (overlayMenuEntry.getMenuAction() == MenuAction.RUNELITE_OVERLAY
			&& overlayMenuClicked.getEntry().getOption().equals(RunecraftingProfitOverlay.RUNECRAFT_PROFIT_RESET)
			&& overlayMenuClicked.getOverlay() == overlay)
		{
			session = null;
		}
	}

	@Subscribe
	private void onGameTick(GameTick gameTick)
	{
		if (session == null || config.timeout() == 0)
		{
			return;
		}

		Duration statTimeout = Duration.ofMinutes(config.timeout());
		Duration sinceCut = Duration.between(session.getLastRunecraftAction(), Instant.now());

		if (sinceCut.compareTo(statTimeout) >= 0)
		{
			session = null;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (client.getLocalPlayer() == null || event.getContainerId() != InventoryID.INVENTORY.getId())
		{
			return;
		}

		Player localPlayer = client.getLocalPlayer();

		if (localPlayer.getAnimation() == RUNECRAFTING_ANIMATION)
		{
			if (session == null)
			{
				session = new RunecraftingProfitSession(itemManager);
			}

			session.updateLastRunecraftAction();
			processInventoryChange();
		}

		takeInventorySnapshot();
	}

	private void takeInventorySnapshot()
	{
		final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
		if (itemContainer != null)
		{
			inventorySnapshot = HashMultiset.create();
			Arrays.stream(itemContainer.getItems())
				.forEach(item -> inventorySnapshot.add(item.getId(), item.getQuantity()));
		}
	}

	private void processInventoryChange()
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);

		if (inventorySnapshot == null || itemContainer == null)
		{
			return;
		}

		Multiset<Integer> currentInventory = HashMultiset.create();
		Arrays.stream(itemContainer.getItems())
			.forEach(item -> currentInventory.add(item.getId(), item.getQuantity()));

		final Multiset<Integer> diff = Multisets.difference(currentInventory, inventorySnapshot);

		List<ItemStack> items = diff.entrySet().stream()
			.map(e -> new ItemStack(e.getElement(), e.getCount(), client.getLocalPlayer().getLocalLocation()))
			.collect(Collectors.toList());

		for (ItemStack i : items)
		{
			session.updateCraftedRunes(i.getId(), i.getQuantity());
		}

		inventorySnapshot = null;
	}
}
