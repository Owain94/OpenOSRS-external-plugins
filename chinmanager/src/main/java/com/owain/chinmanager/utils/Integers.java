package com.owain.chinmanager.utils;

public class Integers
{
	public int parseInt(String value, int def)
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

	public static boolean isNumeric(String strNum)
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
}
