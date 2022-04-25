package com.owain.chinmanager.api;

import okhttp3.HttpUrl;

public class BaseApi
{
	public static final boolean DEBUG = false;

	public static HttpUrl.Builder baseUrl()
	{
		HttpUrl.Builder httpUrl;

		if (DEBUG)
		{
			httpUrl = new HttpUrl.Builder()
				.scheme("http")
				.host("localhost")
				.port(4200)
				.addPathSegment("api");
		}
		else
		{
			httpUrl = new HttpUrl.Builder()
				.scheme("https")
				.host("chinplugins.xyz")
				.addPathSegment("api");
		}

		return httpUrl;
	}

	public static HttpUrl.Builder userBase()
	{
		return baseUrl()
			.addPathSegment("user");
	}

	public static HttpUrl user(String segment)
	{
		return userBase()
			.addPathSegment(segment)
			.build();
	}

	public static HttpUrl.Builder discordBase()
	{
		return baseUrl()
			.addPathSegment("discord");
	}

	public static HttpUrl discord(String segment)
	{
		return discordBase()
			.addPathSegment(segment)
			.build();
	}

	public static HttpUrl.Builder licensesBase()
	{
		return baseUrl()
			.addPathSegment("license");
	}

	public static HttpUrl license(String segment)
	{
		return licensesBase()
			.addPathSegment(segment)
			.build();
	}

	public static HttpUrl.Builder notificationsBase()
	{
		return baseUrl()
			.addPathSegment("notifications");
	}

	public static HttpUrl notification(String segment)
	{
		return notificationsBase()
			.addPathSegment(segment)
			.build();
	}

	public static HttpUrl.Builder xteaBase()
	{
		return baseUrl()
			.addPathSegment("xtea");
	}

	public static HttpUrl xtea()
	{
		return xteaBase()
			.build();
	}
}
