package com.owain.chinmanager.ui.teleports.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum XericsTalisman
{
	XERICS_TALISMAN("Xeric's talisman"),
	POH("PoH (Mounted xeric's talisman)"),

	;

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
