package com.owain.chinmanager.api;

import com.google.gson.JsonObject;
import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import static com.owain.chinmanager.ChinManagerPlugin.JSON;
import com.owain.chinmanager.ui.plugins.options.OptionsConfig;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.client.config.ConfigManager;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Slf4j
public class NotificationsApi
{
	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	private final ConfigManager configManager;
	private final OptionsConfig optionsConfig;

	private String cachedUsername = "";
	public String previousPlugin = "";
	public String cachedPlugin = "";

	@Inject
	NotificationsApi(ChinManager chinManager, ChinManagerPlugin chinManagerPlugin)
	{
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;
		this.configManager = chinManagerPlugin.getConfigManager();
		this.optionsConfig = configManager.getConfig(OptionsConfig.class);
	}

	public void sendNotification(String type, Map<String, String> content)
	{
		if (chinManager.getActivePlugins().isEmpty() || !shouldSendNotification(type))
		{
			return;
		}

		Map<String, String> newContent = new HashMap<>();

		if (optionsConfig.username())
		{
			Player localPlayer = chinManagerPlugin.getClient().getLocalPlayer();

			if (localPlayer != null)
			{
				cachedUsername = localPlayer.getName();
			}

			newContent.put("accountName", cachedUsername);
		}
		else
		{
			newContent.put("accountName", "");
		}

		if (!optionsConfig.discordNotifications() || !checkCookie())
		{
			return;
		}

		newContent.putAll(content);

		notification(type, newContent)
			.subscribeOn(Schedulers.io())
			.take(1)
			.subscribe();
	}

	private boolean shouldSendNotification(String type)
	{
		if (!optionsConfig.discordNotifications())
		{
			return false;
		}

		switch (type)
		{
			case "break":
				return optionsConfig.breaks();
			case "resume":
				return optionsConfig.resume();
			case "plugin":
				return optionsConfig.plugin();
			case "level":
				return optionsConfig.level();
			case "pet":
				return optionsConfig.pet();
			case "collectionlog":
				return optionsConfig.collectionLog();
			default:
				return false;
		}
	}

	private boolean checkCookie()
	{
		String value = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "cookies");

		return value != null && !value.equals("");
	}

	private String buildNotification(Map<String, String> content)
	{
		JsonObject json = new JsonObject();

		for (Map.Entry<String, String> entry : content.entrySet())
		{
			json.addProperty(entry.getKey(), entry.getValue());
		}

		return json.toString();
	}

	private Observable<Boolean> notification(String type, Map<String, String> content)
	{
		return Observable.defer(() ->
		{
			HttpUrl httpUrl = BaseApi.notification(type);

			RequestBody body = RequestBody.create(buildNotification(content), JSON);

			Request request = new Request.Builder()
				.addHeader("accept", "application/json")
				.url(httpUrl)
				.post(body)
				.build();

			try (Response response = chinManagerPlugin.getOkHttpClient().newCall(request).execute())
			{
				ResponseBody responseBody = response.body();
				if (responseBody == null)
				{
					return Observable.just(false);
				}

				responseBody.close();
				return Observable.just(true);
			}
			catch (Exception ignored)
			{
				return Observable.just(false);
			}
		});
	}
}
