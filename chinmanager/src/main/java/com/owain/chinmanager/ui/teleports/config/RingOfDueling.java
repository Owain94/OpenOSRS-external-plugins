package com.owain.chinmanager.ui.teleports.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RingOfDueling
{
	RING_OF_DUELING("Ring of dueling"),
	POH("PoH (Jewellery box)"),

	;

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
