package com.owain.chinmanager.utils;

import io.reactivex.rxjava3.annotations.NonNull;
import net.runelite.client.plugins.Plugin;

public class ConfigGroup
{
	public static @NonNull String getConfigGroup(@NonNull Plugin plugin)
	{
		return getConfigGroup(plugin.getName());
	}

	public static @NonNull String getConfigGroup(@NonNull String plugin)
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
