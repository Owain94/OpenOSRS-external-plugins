package com.owain.chinmanager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;

@AllArgsConstructor
@Getter
public enum RuneReplacement
{
	TOME_OF_FIRE("Tome of fire", ItemID.TOME_OF_FIRE, ItemID.FIRE_RUNE),
	TOME_OF_WATER("Tome of water", ItemID.TOME_OF_WATER, ItemID.WATER_RUNE),

	;

	private final String name;
	private final int itemID;
	private final int rune;

	@Override
	public String toString()
	{
		return name;
	}
}
