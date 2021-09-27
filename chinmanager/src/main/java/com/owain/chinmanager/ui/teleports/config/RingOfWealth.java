package com.owain.chinmanager.ui.teleports.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RingOfWealth
{
	RING_OF_WEALTH("Ring of wealth"),
	POH("PoH (Jewellery box)"),

	;

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
