package com.owain.chinmanager.ui.teleports.config;

import io.reactivex.rxjava3.annotations.NonNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DigsitePendant
{
	DIGSITE_PENDANT("Digsite pendant"),
	POH("PoH (Mounted digsite pendant)"),

	;

	private final @NonNull String name;

	@Override
	public @NonNull String toString()
	{
		return name;
	}
}
