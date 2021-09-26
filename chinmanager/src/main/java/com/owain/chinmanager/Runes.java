/*
 * Copyright (c) 2017, Tyler <https://github.com/tylerthardy>
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
package com.owain.chinmanager;


import io.reactivex.rxjava3.annotations.NonNull;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.ItemID;

public enum Runes
{
	AIR(1, ItemID.AIR_RUNE),
	WATER(2, ItemID.WATER_RUNE),
	EARTH(3, ItemID.EARTH_RUNE),
	FIRE(4, ItemID.FIRE_RUNE),
	MIND(5, ItemID.MIND_RUNE),
	CHAOS(6, ItemID.CHAOS_RUNE),
	DEATH(7, ItemID.DEATH_RUNE),
	BLOOD(8, ItemID.BLOOD_RUNE),
	COSMIC(9, ItemID.COSMIC_RUNE),
	NATURE(10, ItemID.NATURE_RUNE),
	LAW(11, ItemID.LAW_RUNE),
	BODY(12, ItemID.BODY_RUNE),
	SOUL(13, ItemID.SOUL_RUNE),
	ASTRAL(14, ItemID.ASTRAL_RUNE),
	MIST(15, ItemID.MIST_RUNE),
	MUD(16, ItemID.MUD_RUNE),
	DUST(17, ItemID.DUST_RUNE),
	LAVA(18, ItemID.LAVA_RUNE),
	STEAM(19, ItemID.STEAM_RUNE),
	SMOKE(20, ItemID.SMOKE_RUNE),
	WRATH(21, ItemID.WRATH_RUNE),
	BANANA(-1, ItemID.BANANA),
	STONYY_BASALT(-1, ItemID.STONY_BASALT),
	ICY_BASALT(-1, ItemID.ICY_BASALT),

	;

	private static final Map<Integer, Runes> runes = new HashMap<>();

	static
	{
		for (Runes rune : values())
		{
			runes.put(rune.getId(), rune);
		}
	}

	@Getter
	private final int id;
	@Getter
	private final int itemId;

	Runes(int id, int itemId)
	{
		this.id = id;
		this.itemId = itemId;
	}

	public static Runes getRune(int varbit)
	{
		return runes.get(varbit);
	}

	public @NonNull String getName()
	{
		String name = this.name();
		name = name.charAt(0) + name.substring(1).toLowerCase();
		return name;
	}
}
