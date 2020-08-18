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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.client.game.ItemManager;
import org.apache.commons.lang3.tuple.Pair;

@Singleton
class RunecraftingProfitSession
{
	@Getter(AccessLevel.PACKAGE)
	private Instant lastRunecraftAction;
	@Getter(AccessLevel.PACKAGE)
	private final Map<Runes, Integer> craftedRunes = new HashMap<>();
	@Getter(AccessLevel.PACKAGE)
	private final Map<Runes, Long> runePrices = new HashMap<>();
	@Getter(AccessLevel.PACKAGE)
	private final Map<Runes, Long> runeProfit = new HashMap<>();
	@Getter(AccessLevel.PACKAGE)
	private Pair<Integer, Long> totalCrafted = Pair.of(0, 0L);

	RunecraftingProfitSession(final ItemManager itemManager)
	{
		for (Runes rune : Runes.values())
		{
			craftedRunes.put(rune, 0);
			runeProfit.put(rune, 0L);
			runePrices.put(rune, rune.getPrice(itemManager));
		}
	}

	void updateLastRunecraftAction()
	{
		this.lastRunecraftAction = Instant.now();
	}

	void updateCraftedRunes(int itemId, int qty)
	{
		for (Map.Entry<Runes, Integer> entry : craftedRunes.entrySet())
		{
			Runes rune = entry.getKey();
			if (rune.getItemId() == itemId)
			{
				int amount = entry.getValue() + qty;
				entry.setValue(amount);
				long profit = amount * runePrices.get(rune);
				runeProfit.put(rune, profit);

				totalCrafted = Pair.of(totalCrafted.getLeft() + qty, totalCrafted.getRight() + qty * runePrices.get(rune));
			}
		}
	}
}
