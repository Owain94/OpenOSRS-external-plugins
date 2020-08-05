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

import static net.runelite.api.ItemID.*;
import net.runelite.client.game.ItemManager;

public enum Runes
{
	AIR("Air", AIR_RUNE),
	MIND("Mind", MIND_RUNE),
	WATER("Water", WATER_RUNE),
	EARTH("Earth", EARTH_RUNE),
	FIRE("Fire", FIRE_RUNE),
	BODY("Body", BODY_RUNE),
	COSMIC("Cosmic", COSMIC_RUNE),
	CHAOS("Chaos", CHAOS_RUNE),
	ASTRAL("Astral", ASTRAL_RUNE),
	NATURE("Nature", NATURE_RUNE),
	LAW("Law", LAW_RUNE),
	DEATH("Death", DEATH_RUNE),
	BLOOD("Blood", BLOOD_RUNE),
	SOUL("Soul", SOUL_RUNE),
	WRATCH("Wrath", WRATH_RUNE),

	MIST("Mist", MIST_RUNE),
	DUST("Dust", DUST_RUNE),
	MUD("Mud", MUD_RUNE),
	SMOKE("Smoke", SMOKE_RUNE),
	STEAM("Steam", STEAM_RUNE),
	LAVA("Lava", LAVA_RUNE),

	;

	private final String name;
	private final int itemId;

	Runes(final String name, final int itemId)
	{
		this.name = name;
		this.itemId = itemId;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public int getItemId()
	{
		return itemId;
	}

	public long getPrice(ItemManager itemManager)
	{
		return itemManager.getItemPrice(itemId);
	}
}

