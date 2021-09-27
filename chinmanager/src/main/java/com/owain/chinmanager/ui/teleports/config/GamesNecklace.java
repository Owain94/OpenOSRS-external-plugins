package com.owain.chinmanager.ui.teleports.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GamesNecklace
{
	GAMES_NECKLACE("Games necklace"),
	POH("PoH (Jewellery box)"),

	;

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
