package com.owain.chinmanager.ui.teleports.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AmuletOfGlory
{
	AMULET_OF_GLORY("Amulet of glory"),
	POH("PoH (Jewellery box)"),

	;

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
