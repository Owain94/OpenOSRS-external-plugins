package com.owain.chinmanager.models;

import lombok.Value;
import net.runelite.api.coords.WorldPoint;

@Value
public class Transport
{
	WorldPoint source;
	WorldPoint destination;
}
