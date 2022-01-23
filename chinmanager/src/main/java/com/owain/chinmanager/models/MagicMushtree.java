package com.owain.chinmanager.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.WidgetInfo;

@Data
@AllArgsConstructor
public class MagicMushtree
{
	private final WorldPoint position;
	private final WidgetInfo widget;
}