package com.owain.chinmanager.utils;

import net.runelite.client.plugins.Plugin;

public class ConfigGroup
{
	public static String getConfigGroup(Plugin plugin)
	{
		return getConfigGroup(plugin.getName());
	}

	public static String getConfigGroup(String plugin)
	{
		String config = Plugins.sanitizedName(plugin);

		switch (config)
		{
			case "chincursealch":
				return "stunalch";
			case "chingiantseaweed":
				return "seaweedcheat";
			default:
				return config;
		}
	}
}
