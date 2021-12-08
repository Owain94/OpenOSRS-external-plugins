package com.owain.chinmanager.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.owain.chinmanager.ChinManagerPlugin;
import io.reactivex.rxjava3.core.Observable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Slf4j
public class LicenseApi
{
	private final ChinManagerPlugin chinManagerPlugin;
	private final ConfigManager configManager;

	@Inject
	LicenseApi(ChinManagerPlugin chinManagerPlugin)
	{
		this.chinManagerPlugin = chinManagerPlugin;
		this.configManager = chinManagerPlugin.getConfigManager();
	}

	public Observable<Map<String, Map<String, String>>> getLicenses()
	{
		return Observable.defer(() ->
		{
			String value = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "cookies");

			if (value == null || value.equals(""))
			{
				return Observable.just(Map.of());
			}

			HttpUrl httpUrl = BaseApi.license("user");

			Request request = new Request.Builder()
				.addHeader("accept", "application/json")
				.get()
				.url(httpUrl)
				.build();

			try (Response response = chinManagerPlugin.getOkHttpClient().newCall(request).execute())
			{
				ResponseBody responseBody = response.body();
				if (responseBody == null)
				{
					return Observable.just(Map.of());
				}

				String responseBodyString = responseBody.string();

				if (responseBodyString.isEmpty())
				{
					responseBody.close();
					return Observable.just(Map.of());
				}

				responseBody.close();
				return Observable.just(processLicenses(responseBodyString));
			}
			catch (Exception ignored)
			{
			}

			return Observable.just(Map.of());
		});
	}

	public Map<String, Map<String, String>> processLicenses(String licenses)
	{
		JsonArray json;
		try
		{
			json = new JsonParser().parse(licenses).getAsJsonArray();
		}
		catch (Exception ignored)
		{
			return Map.of();
		}

		Map<String, Map<String, String>> licenseMap = new HashMap<>();

		for (JsonElement entry : json)
		{
			JsonObject jsonObject = entry.getAsJsonObject();

			if (jsonObject.get("blocked").getAsBoolean() || jsonObject.get("trial").getAsBoolean())
			{
				continue;
			}

			String pluginName = jsonObject.getAsJsonObject("product").get("name").getAsString();
			int validFor = jsonObject.get("validFor").getAsInt();
			JsonElement activatedAtJson = jsonObject.get("activatedAt");

			if (activatedAtJson == null)
			{
				licenseMap.put(pluginName, Map.of(jsonObject.get("token").getAsString(), validFor + " days"));
				continue;
			}

			if (validFor > 3000)
			{
				licenseMap.put(pluginName, Map.of(jsonObject.get("token").getAsString(), "Lifetime"));
				continue;
			}

			Instant activatedInstant = LocalDateTime.parse(
					jsonObject.get("activatedAt").getAsString(),
					DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
				)
				.atZone(
					ZoneId.of("UTC")
				)
				.toInstant()
				.plus(validFor, ChronoUnit.DAYS);

			Instant now = OffsetDateTime.now(ZoneOffset.UTC).toInstant();

			if (now.isAfter(activatedInstant))
			{
				continue;
			}

			int days = (int) Duration.between(now, activatedInstant).toDays();
			int hours = (int) Duration.between(now, activatedInstant).toHours();

			if (days == 0)
			{
				licenseMap.put(pluginName, Map.of(jsonObject.get("token").getAsString(), hours + " hours"));
			}
			else
			{
				licenseMap.put(pluginName, Map.of(jsonObject.get("token").getAsString(), days + " days"));
			}
		}

		return licenseMap;
	}
}
