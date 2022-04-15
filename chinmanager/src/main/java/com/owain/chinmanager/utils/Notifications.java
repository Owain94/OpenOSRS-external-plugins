package com.owain.chinmanager.utils;

import com.google.inject.Inject;
import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.api.NotificationsApi;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

public class Notifications
{
	private static final Pattern LEVEL_UP_PATTERN = Pattern.compile(".*Your ([a-zA-Z]+) (?:level is|are)? now (\\d+)\\.");
	private static final String COLLECTION_LOG_TEXT = "New item added to your collection log: ";
	private static final List<String> PET_MESSAGES = List.of("You have a funny feeling like you're being followed",
		"You feel something weird sneaking into your backpack",
		"You have a funny feeling like you would have been followed");

	private final Set<String> notifyLevels = new HashSet<>();

	private final Client client;
	private final ChinManager chinManager;
	private final NotificationsApi notificationsApi;

	@Inject
	public Notifications(
		Client client,
		ChinManager chinManager,
		NotificationsApi notificationsApi
	)
	{
		this.client = client;
		this.chinManager = chinManager;
		this.notificationsApi = notificationsApi;
	}

	@Subscribe(priority = -98)
	public void onGameTick(GameTick gameTick)
	{
		if (client.getWidget(WidgetInfo.LEVEL_UP_LEVEL) != null)
		{
			parseLevelUpWidget(WidgetInfo.LEVEL_UP_LEVEL);
		}
		else if (client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT) != null)
		{
			String text = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT).getText();
			if (!Text.removeTags(text).contains("High level gamble"))
			{
				parseLevelUpWidget(WidgetInfo.DIALOG_SPRITE_TEXT);
			}
		}
	}

	@Subscribe(priority = -98)
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE
			&& chatMessage.getType() != ChatMessageType.SPAM
			&& chatMessage.getType() != ChatMessageType.TRADE
			&& chatMessage.getType() != ChatMessageType.FRIENDSCHATNOTIFICATION)
		{
			return;
		}

		if (chinManager.getCurrentlyActive() == null)
		{
			return;
		}

		String message = chatMessage.getMessage();

		if (PET_MESSAGES.stream().anyMatch(message::contains))
		{
			notificationsApi.sendNotification(
				"pet",
				Map.of(
					"plugin", chinManager.getCurrentlyActive().getName()
				)
			);
		}

		if (message.startsWith(COLLECTION_LOG_TEXT) && client.getVarbitValue(Varbits.COLLECTION_LOG_NOTIFICATION) == 1)
		{
			String entry = Text.removeTags(message).substring(COLLECTION_LOG_TEXT.length());

			notificationsApi.sendNotification(
				"collectionlog",
				Map.of(
					"plugin", chinManager.getCurrentlyActive().getName(),
					"entry", entry
				)
			);
		}
	}

	private void parseLevelUpWidget(WidgetInfo levelUpLevel)
	{
		Widget levelChild = client.getWidget(levelUpLevel);
		if (levelChild == null)
		{
			return;
		}

		Matcher m = LEVEL_UP_PATTERN.matcher(levelChild.getText());
		if (!m.matches())
		{
			return;
		}

		String skillName = m.group(1);
		String skillLevel = m.group(2);
		String combined = skillName + " " + skillLevel;

		if (!notifyLevels.contains(combined))
		{
			notificationsApi.sendNotification(
				"level",
				Map.of(
					"skill", skillName,
					"level", skillLevel
				)
			);
			notifyLevels.add(combined);
		}
	}
}
