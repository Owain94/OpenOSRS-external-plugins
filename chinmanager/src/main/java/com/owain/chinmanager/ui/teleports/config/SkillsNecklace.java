package com.owain.chinmanager.ui.teleports.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SkillsNecklace
{
	SKILLS_NECKLACE("Skills necklace"),
	POH("PoH (Jewellery box)"),

	;

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
