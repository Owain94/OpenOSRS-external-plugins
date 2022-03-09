package com.owain.chinmanager.utils;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

public class Banking
{
	private final Client client;
	private final ClientThread clientThread;
	private final ItemManager itemManager;

	@Inject
	public Banking(
		Client client,
		ClientThread clientThread,
		ItemManager itemManager
	)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.itemManager = itemManager;
	}

	@Subscribe(priority = -98)
	public void onWidgetLoaded(final WidgetLoaded widgetLoaded)
	{
		if (com.owain.automation.Banking.ITEMS.isEmpty())
		{
			return;
		}

		com.owain.automation.Banking.onWidgetLoaded(widgetLoaded, client, clientThread);
	}

	@Subscribe(priority = -98)
	public void onScriptPreFired(final ScriptPreFired scriptPreFired)
	{
		if (com.owain.automation.Banking.ITEMS.isEmpty())
		{
			return;
		}

		com.owain.automation.Banking.onScriptPreFired(scriptPreFired, client);
	}

	@Subscribe(priority = -98)
	public void onScriptCallbackEvent(final ScriptCallbackEvent scriptCallbackEvent)
	{
		if (com.owain.automation.Banking.ITEMS.isEmpty())
		{
			return;
		}

		com.owain.automation.Banking.onScriptCallbackEvent(scriptCallbackEvent, client, itemManager);
	}

	@Subscribe(priority = -98)
	public void onScriptPostFired(final ScriptPostFired scriptPostFired)
	{
		if (com.owain.automation.Banking.ITEMS.isEmpty())
		{
			return;
		}

		com.owain.automation.Banking.onScriptPostFired(scriptPostFired, client);
	}
}
