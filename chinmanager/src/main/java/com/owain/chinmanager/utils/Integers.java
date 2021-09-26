package com.owain.chinmanager.utils;

import io.reactivex.rxjava3.annotations.NonNull;
import org.jetbrains.annotations.Nullable;

public class Integers
{
	public static boolean isNumeric(@Nullable String strNum)
	{
		if (strNum == null)
		{
			return false;
		}
		try
		{
			Double.parseDouble(strNum);
		}
		catch (NumberFormatException nfe)
		{
			return false;
		}
		return true;
	}

	public int parseInt(@NonNull String value, int def)
	{
		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			return def;
		}
	}
}
