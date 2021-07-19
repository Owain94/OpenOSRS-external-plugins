package com.owain.automation;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.SpriteID;
import net.runelite.api.Varbits;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;

@Slf4j
public final class Banking
{
	public static final String CHIN_INV_SEARCH = "chin bank filter";
	public static Set<Integer> ITEMS = Set.of();

	public static void doBankSearch(Client client, ClientThread clientThread)
	{
		if (!ITEMS.isEmpty())
		{
			clientThread.invoke(() ->
			{
				client.setVarbit(Varbits.CURRENT_BANK_TAB, 0);
				layoutBank(client);

				Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
				if (bankContainer != null && !bankContainer.isHidden())
				{
					Widget searchBackground = client.getWidget(WidgetInfo.BANK_SEARCH_BUTTON_BACKGROUND);
					if (searchBackground != null)
					{
						searchBackground.setSpriteId(SpriteID.EQUIPMENT_SLOT_TILE);
					}
				}
			});
		}
	}

	public static void layoutBank(Client client)
	{
		Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankContainer == null || bankContainer.isHidden())
		{
			return;
		}

		Object[] scriptArgs = bankContainer.getOnInvTransmitListener();
		if (scriptArgs == null)
		{
			return;
		}

		client.runScript(scriptArgs);
	}

	public static void onWidgetLoaded(WidgetLoaded event, Client client, ClientThread clientThread)
	{
		if (!ITEMS.isEmpty())
		{
			if (event.getGroupId() == WidgetID.BANK_GROUP_ID)
			{
				doBankSearch(client, clientThread);
			}
		}
	}

	public static void onScriptPreFired(ScriptPreFired event, Client client)
	{
		if (!ITEMS.isEmpty())
		{
			if (event.getScriptId() == ScriptID.BANKMAIN_FINISHBUILDING)
			{
				Widget bankTitle = client.getWidget(WidgetInfo.BANK_TITLE_BAR);
				if (bankTitle != null)
				{
					bankTitle.setText("<col=ff0000>" + CHIN_INV_SEARCH + "</col>");
				}
			}
		}
	}

	public static void onScriptPostFired(ScriptPostFired event, Client client)
	{
		if (!ITEMS.isEmpty())
		{
			if (event.getScriptId() == ScriptID.BANKMAIN_SEARCHING)
			{
				client.getIntStack()[client.getIntStackSize() - 1] = 1; // true
			}
		}
	}

	public static void onScriptCallbackEvent(ScriptCallbackEvent event, Client client, ItemManager itemManager)
	{
		String eventName = event.getEventName();

		int[] intStack = client.getIntStack();
		int intStackSize = client.getIntStackSize();

		switch (eventName)
		{
			case "bankSearchFilter":
			{
				if (!ITEMS.isEmpty())
				{
					int itemId = intStack[intStackSize - 1];

					if (setupContainsItem(itemId, itemManager))
					{
						// return true
						intStack[intStackSize - 2] = 1;
					}
					else
					{
						intStack[intStackSize - 2] = 0;
					}
				}
				break;
			}
			case "getSearchingTagTab":
				if (!ITEMS.isEmpty())
				{
					intStack[intStackSize - 1] = 1;
				}
				else
				{
					intStack[intStackSize - 1] = 0;
				}
				break;
		}
	}

	private static boolean setupContainsItem(int itemID, ItemManager itemManager)
	{
		itemID = ItemVariationMapping.map(itemManager.canonicalize(itemID));

		return checkIfContainerContainsItem(itemID, itemManager);
	}

	private static boolean checkIfContainerContainsItem(int itemID, ItemManager itemManager)
	{
		for (final int item : ITEMS)
		{
			if (itemID == getCorrectID(item, itemManager))
			{
				return true;
			}
		}

		return false;
	}

	private static int getCorrectID(int itemId, ItemManager itemManager)
	{
		return ItemVariationMapping.map(itemManager.canonicalize(itemId));
	}
}
