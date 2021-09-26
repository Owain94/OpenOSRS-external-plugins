package com.owain.chinmanager.ui.teleports.config;

import io.reactivex.rxjava3.annotations.NonNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RingOfDueling
{
	RING_OF_DUELING("Ring of dueling"),
	POH("PoH (Jewellery box)"),

	;

	private final @NonNull String name;

	@Override
	public @NonNull String toString()
	{
		return name;
	}
}
