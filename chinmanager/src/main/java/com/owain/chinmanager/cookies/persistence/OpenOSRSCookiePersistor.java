package com.owain.chinmanager.cookies.persistence;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.owain.chinmanager.ChinManagerPlugin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;
import okhttp3.Cookie;

@Singleton
public class OpenOSRSCookiePersistor implements CookiePersistor
{
	private final ConfigManager configManager;

	@Inject
	OpenOSRSCookiePersistor(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	@Override
	public List<Cookie> loadAll()
	{
		List<Cookie> cookies = new ArrayList<>();

		String config = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "cookies");

		if (config == null || config.equals(""))
		{
			return List.of();
		}

		for (Map.Entry<String, JsonElement> entry : new JsonParser().parse(config).getAsJsonObject().entrySet())
		{
			String serializedCookie = entry.getValue().getAsString();
			Cookie cookie = new SerializableCookie().decode(serializedCookie);
			if (cookie != null)
			{
				cookies.add(cookie);
			}
		}
		return cookies;
	}

	@Override
	public void saveAll(Collection<Cookie> cookies)
	{
		JsonObject json = new JsonObject();

		for (Cookie cookie : cookies)
		{
			json.addProperty(createCookieKey(cookie), new SerializableCookie().encode(cookie));
		}

		configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "cookies", json.toString());
	}

	private static String createCookieKey(Cookie cookie)
	{
		return (cookie.secure() ? "https" : "http") + "://" + cookie.domain() + cookie.path() + "|" + cookie.name();
	}

	@Override
	public void clear()
	{
		configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "cookies", "");
	}
}
