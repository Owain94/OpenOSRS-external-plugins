package com.owain.chinmanager.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class SpiritTree
{
	private final WorldPoint position;
	private final String location;
}
