package com.owain.chinmanager.ui.teleports.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Poh
{
	RUNES("Runes"),
	TELEPORT_TABLET("Teleport tablet"),
	CONSTRUCTION_CAPE("Construction cape"),

	;

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
