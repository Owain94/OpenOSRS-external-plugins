package com.owain.chinmanager.ui.teleports.config;

import io.reactivex.rxjava3.annotations.NonNull;
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

	private final @NonNull String name;

	@Override
	public @NonNull String toString()
	{
		return name;
	}
}
