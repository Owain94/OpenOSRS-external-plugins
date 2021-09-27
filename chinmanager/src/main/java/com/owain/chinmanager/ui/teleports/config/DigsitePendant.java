package com.owain.chinmanager.ui.teleports.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DigsitePendant
{
	DIGSITE_PENDANT("Digsite pendant"),
	POH("PoH (Mounted digsite pendant)"),

	;

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
