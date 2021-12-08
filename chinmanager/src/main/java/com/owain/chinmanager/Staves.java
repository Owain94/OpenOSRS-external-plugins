package com.owain.chinmanager;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;

@AllArgsConstructor
@Getter
public enum Staves
{
	NONE("None", List.of(), List.of()),

	AIR("Air", List.of(ItemID.STAFF_OF_AIR, ItemID.AIR_BATTLESTAFF, ItemID.MYSTIC_AIR_STAFF), List.of(ItemID.AIR_RUNE)),
	WATER("Water", List.of(ItemID.STAFF_OF_WATER, ItemID.WATER_BATTLESTAFF, ItemID.MYSTIC_WATER_STAFF), List.of(ItemID.WATER_RUNE)),
	EARTH("Earth", List.of(ItemID.STAFF_OF_EARTH, ItemID.EARTH_BATTLESTAFF, ItemID.MYSTIC_EARTH_STAFF), List.of(ItemID.EARTH_RUNE)),
	FIRE("Fire", List.of(ItemID.STAFF_OF_FIRE, ItemID.FIRE_BATTLESTAFF, ItemID.MYSTIC_FIRE_STAFF), List.of(ItemID.FIRE_RUNE)),

	LAVA("Lava", List.of(ItemID.LAVA_BATTLESTAFF, ItemID.LAVA_BATTLESTAFF_21198, ItemID.MYSTIC_LAVA_STAFF, ItemID.MYSTIC_LAVA_STAFF_21200), List.of(ItemID.EARTH_RUNE, ItemID.FIRE_RUNE)),
	MUD("Mud", List.of(ItemID.MUD_BATTLESTAFF, ItemID.MYSTIC_MUD_STAFF), List.of(ItemID.EARTH_RUNE, ItemID.WATER_RUNE)),
	STEAM("Steam", List.of(ItemID.STEAM_BATTLESTAFF, ItemID.STEAM_BATTLESTAFF_12795, ItemID.MYSTIC_STEAM_STAFF, ItemID.MYSTIC_STEAM_STAFF_12796), List.of(ItemID.FIRE_RUNE, ItemID.WATER_RUNE)),
	SMOKE("Smoke", List.of(ItemID.SMOKE_BATTLESTAFF, ItemID.MYSTIC_SMOKE_STAFF), List.of(ItemID.FIRE_RUNE, ItemID.AIR_RUNE)),
	MIST("Mist", List.of(ItemID.MIST_BATTLESTAFF, ItemID.MYSTIC_MIST_STAFF), List.of(ItemID.WATER_RUNE, ItemID.AIR_RUNE)),
	DUST("Dust", List.of(ItemID.DUST_BATTLESTAFF, ItemID.MYSTIC_DUST_STAFF), List.of(ItemID.EARTH_RUNE, ItemID.AIR_RUNE)),

	;

	private final String name;
	private final List<Integer> itemIDs;
	private final List<Integer> runes;

	@Override
	public String toString()
	{
		return name;
	}
}
