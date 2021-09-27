package com.owain.chinmanager.utils;

import net.runelite.client.plugins.Plugin;

public class Plugins
{
	public static String sanitizedName(Plugin plugin)
	{
		if (plugin == null)
		{
			return "";
		}

		return sanitizedName(plugin.getName());
	}

	public static String sanitizedName(String plugin)
	{
		return plugin.toLowerCase().replace(" ", "");
	}
}
