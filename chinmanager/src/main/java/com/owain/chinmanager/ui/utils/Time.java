package com.owain.chinmanager.ui.utils;

import io.reactivex.rxjava3.annotations.NonNull;
import java.time.Duration;

public class Time
{
	public static String formatDuration(@NonNull Duration duration)
	{
		long seconds = duration.getSeconds();
		long absSeconds = Math.abs(seconds);

		return String.format(
			"%02d:%02d:%02d",
			absSeconds / 3600,
			(absSeconds % 3600) / 60,
			absSeconds % 60);
	}

	public static String formatDuration(long duration)
	{
		long absSeconds = Math.abs(duration / 1000);

		return String.format(
			"%02d:%02d:%02d",
			absSeconds / 3600,
			(absSeconds % 3600) / 60,
			absSeconds % 60);
	}
}
