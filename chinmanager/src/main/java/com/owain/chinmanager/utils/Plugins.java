package com.owain.chinmanager.utils;

import io.reactivex.rxjava3.annotations.NonNull;
import net.runelite.client.plugins.Plugin;
import org.jetbrains.annotations.Nullable;

public class Plugins
{
	public static @NonNull String sanitizedName(@Nullable Plugin plugin)
	{
		if (plugin == null)
		{
			return "";
		}

		return sanitizedName(plugin.getName());
	}

	public static @NonNull String sanitizedName(@NonNull String plugin)
	{
		return plugin.toLowerCase().replace(" ", "");
	}
}
