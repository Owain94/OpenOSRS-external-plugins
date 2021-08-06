package com.owain.automation;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.VarClientInt;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;

public class ContainerUtils
{
	public static int getQuantity(final Integer itemId, Client client)
	{
		if (itemId == null)
		{
			return 0;
		}

		return getQuantity(List.of(itemId), client);
	}

	public static int getQuantity(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return 0;
		}

		Collection<WidgetItem> inventoryItems = getInventoryItems(client);

		if (inventoryItems == null)
		{
			return 0;
		}

		int count = 0;

		for (WidgetItem inventoryItem : inventoryItems)
		{
			if (itemIds.contains(inventoryItem.getId()))
			{
				count += inventoryItem.getQuantity();
			}
		}

		return count;
	}

	public static boolean hasItem(final Integer itemId, Client client)
	{
		if (itemId == null)
		{
			return false;
		}

		Map<Integer, Integer> items = getInventoryItemsMap(List.of(itemId), client);

		return items != null && !items.isEmpty();
	}

	public static boolean hasItems(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return false;
		}

		Map<Integer, Integer> items = getInventoryItemsMap(itemIds, client);

		return items != null && !items.isEmpty() && items.size() == itemIds.size();
	}

	public static boolean hasAnyItem(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return false;
		}

		Map<Integer, Integer> items = getInventoryItemsMap(itemIds, client);

		return items != null && !items.isEmpty();
	}

	public static boolean hasAnyItem(final Integer itemId, Client client)
	{
		if (itemId == null)
		{
			return false;
		}

		return hasAnyItem(List.of(itemId), client);
	}

	public static WidgetItem getInventoryWidgetItemForItemsPos(final int itemPos, Client client)
	{
		if (itemPos == -1)
		{
			return null;
		}

		Collection<WidgetItem> inventoryItems = getInventoryItems(client);

		if (inventoryItems == null)
		{
			return null;
		}

		return new ArrayList<>(inventoryItems).get(itemPos);
	}

	public static int getFirstInventoryItemsPos(final Integer itemId, Client client)
	{
		if (itemId == null)
		{
			return -1;
		}

		return getFirstInventoryItemsPos(List.of(itemId), client);
	}

	public static int getFirstInventoryItemsPos(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return -1;
		}

		Map<Integer, Integer> items = getInventoryItemsMap(itemIds, client);

		if (items == null || items.isEmpty())
		{
			return -1;
		}
		else
		{
			return items.entrySet().stream().findFirst().get().getValue();
		}
	}

	public static Map<Integer, Integer> getInventoryItemsMap(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return null;
		}

		Collection<WidgetItem> inventoryItems = getInventoryItems(client);

		if (inventoryItems == null)
		{
			return null;
		}

		Map<Integer, Integer> items = new HashMap<>();

		for (WidgetItem inventoryItem : inventoryItems)
		{
			if (itemIds.contains(inventoryItem.getId()))
			{
				items.put(inventoryItem.getId(), inventoryItem.getIndex());
			}
		}

		if (items.isEmpty())
		{
			return null;
		}

		return items;
	}

	public static Collection<WidgetItem> getInventoryItems(Client client)
	{
		if (client.getVar(VarClientInt.INVENTORY_TAB) != 3)
		{
			client.runScript(915, 3);
		}

		Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

		if (inventory == null || inventory.isHidden())
		{
			return null;
		}

		return new ArrayList<>(inventory.getWidgetItems());
	}

	public static int getBankInventoryQuantity(final Integer itemId, Client client)
	{
		if (itemId == null)
		{
			return 0;
		}

		return getBankInventoryQuantity(List.of(itemId), client);
	}

	public static int getBankInventoryQuantity(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return 0;
		}

		Collection<WidgetItem> bankInventoryItems = getBankInventoryItems(client);

		if (bankInventoryItems == null)
		{
			return 0;
		}

		int count = 0;

		for (WidgetItem bankInventoryItem : bankInventoryItems)
		{
			if (itemIds.contains(bankInventoryItem.getId()))
			{
				count += bankInventoryItem.getQuantity();
			}
		}

		return count;
	}

	public static boolean hasBankInventoryItem(final Integer itemId, Client client)
	{
		if (itemId == null)
		{
			return false;
		}

		Map<Integer, Integer> items = getBankInventoryItemsMap(List.of(itemId), client);

		return items != null && !items.isEmpty();
	}

	public static boolean hasBankInventoryItems(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return false;
		}

		Map<Integer, Integer> items = getBankInventoryItemsMap(itemIds, client);

		return items != null && !items.isEmpty() && items.size() == itemIds.size();
	}

	public static boolean hasAnyBankInventoryItem(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return false;
		}

		Map<Integer, Integer> items = getBankInventoryItemsMap(itemIds, client);

		return items != null && !items.isEmpty();
	}

	public static boolean hasAnyBankInventoryItem(final Integer itemId, Client client)
	{
		if (itemId == null)
		{
			return false;
		}

		return hasAnyBankInventoryItem(List.of(itemId), client);
	}

	public static WidgetItem getBankInventoryWidgetItemForItemsPos(final int itemPos, Client client)
	{
		if (itemPos == -1)
		{
			return null;
		}

		Collection<WidgetItem> bankInventoryItems = getBankInventoryItems(client);

		if (bankInventoryItems == null)
		{
			return null;
		}

		return new ArrayList<>(bankInventoryItems).get(itemPos);
	}

	public static int getFirstBankInventoryItemsPos(final Integer itemId, Client client)
	{
		if (itemId == null)
		{
			return -1;
		}

		return getFirstBankInventoryItemsPos(List.of(itemId), client);
	}

	public static int getFirstBankInventoryItemsPos(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return -1;
		}

		Map<Integer, Integer> items = getBankInventoryItemsMap(itemIds, client);

		if (items == null || items.isEmpty())
		{
			return -1;
		}
		else
		{
			return items.entrySet().stream().findFirst().get().getValue();
		}
	}

	public static Map<Integer, Integer> getBankInventoryItemsMap(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return null;
		}

		Collection<WidgetItem> bankInventoryItems = getBankInventoryItems(client);

		if (bankInventoryItems == null)
		{
			return null;
		}

		Map<Integer, Integer> items = new HashMap<>();

		for (WidgetItem bankInventoryItem : bankInventoryItems)
		{
			if (itemIds.contains(bankInventoryItem.getId()))
			{
				items.put(bankInventoryItem.getId(), bankInventoryItem.getIndex());
			}
		}

		if (items.isEmpty())
		{
			return null;
		}

		return items;
	}

	public static Collection<WidgetItem> getBankInventoryItems(Client client)
	{
		Collection<WidgetItem> widgetItems = new ArrayList<>();

		Widget inventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
		if (inventory == null || inventory.isHidden())
		{
			return null;
		}

		Widget[] children = inventory.getDynamicChildren();
		for (int i = 0; i < children.length; i++)
		{
			Widget child = children[i];
			boolean isDragged = child.isWidgetItemDragged(child.getItemId());
			int dragOffsetX = 0;
			int dragOffsetY = 0;

			if (isDragged)
			{
				Point p = child.getWidgetItemDragOffsets();
				dragOffsetX = p.getX();
				dragOffsetY = p.getY();
			}
			// set bounds to same size as default inventory
			Rectangle bounds = child.getBounds();
			bounds.setBounds(bounds.x, bounds.y, 32, 32);
			Rectangle dragBounds = child.getBounds();
			dragBounds.setBounds(bounds.x + dragOffsetX, bounds.y + dragOffsetY, 32, 32);
			widgetItems.add(new WidgetItem(child.getItemId(), child.getItemQuantity(), i, bounds, child, dragBounds));
		}

		return widgetItems;
	}


	public static int getBankQuantity(final Integer itemId, Client client)
	{
		if (itemId == null)
		{
			return 0;
		}

		return getBankQuantity(List.of(itemId), client);
	}

	public static int getBankQuantity(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return 0;
		}

		Collection<Widget> bankItems = getBankItems(client);

		if (bankItems == null)
		{
			return 0;
		}

		int count = 0;

		for (Widget bankItem : bankItems)
		{
			if (itemIds.contains(bankItem.getItemId()))
			{
				count += bankItem.getItemQuantity();
			}
		}

		return count;
	}

	public static boolean hasBankItem(final Integer itemId, Client client)
	{
		if (itemId == null)
		{
			return false;
		}

		Map<Integer, Integer> items = getBankItemsMap(List.of(itemId), client);

		return items != null && !items.isEmpty();
	}

	public static boolean hasBankItems(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return false;
		}

		Map<Integer, Integer> items = getBankItemsMap(itemIds, client);

		return items != null && !items.isEmpty() && items.size() == itemIds.size();
	}

	public static boolean hasAnyBankItem(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return false;
		}

		Map<Integer, Integer> items = getBankItemsMap(itemIds, client);

		return items != null && !items.isEmpty();
	}

	public static boolean hasAnyBankItem(final Integer itemId, Client client)
	{
		if (itemId == null)
		{
			return false;
		}

		return hasAnyBankItem(List.of(itemId), client);
	}

	public static WidgetItem getBankWidgetItemForItemsPos(final int itemPos, Client client)
	{
		Collection<Widget> bankItems = getBankItems(client);

		if (bankItems == null)
		{
			return null;
		}

		Map<Integer, Widget> items = new HashMap<>();

		for (Widget bankItem : bankItems)
		{
			items.put(bankItem.getIndex(), bankItem);
		}

		if (items.isEmpty())
		{
			return null;
		}

		if (items.containsKey(itemPos))
		{
			Widget widget = items.get(itemPos);

			boolean isDragged = widget.isWidgetItemDragged(widget.getItemId());
			int dragOffsetX = 0;
			int dragOffsetY = 0;

			if (isDragged)
			{
				Point p = widget.getWidgetItemDragOffsets();
				dragOffsetX = p.getX();
				dragOffsetY = p.getY();
			}
			// set bounds to same size as default inventory
			Rectangle bounds = widget.getBounds();
			bounds.setBounds(bounds.x, bounds.y, 32, 32);
			Rectangle dragBounds = widget.getBounds();
			dragBounds.setBounds(bounds.x + dragOffsetX, bounds.y + dragOffsetY, 32, 32);
			return new WidgetItem(widget.getItemId(), widget.getItemQuantity(), itemPos, bounds, widget, dragBounds);
		}

		return null;
	}

	public static int getFirstBankItemsPos(final Integer itemId, Client client)
	{
		if (itemId == null)
		{
			return -1;
		}

		return getFirstBankItemsPos(List.of(itemId), client);
	}

	public static int getFirstBankItemsPos(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return -1;
		}

		Map<Integer, Integer> items = getBankItemsMap(itemIds, client);

		if (items == null || items.isEmpty())
		{
			return -1;
		}
		else
		{
			return items.entrySet().stream().findFirst().get().getValue();
		}
	}

	private static Map<Integer, Integer> getBankItemsMap(final List<Integer> itemIds, Client client)
	{
		if (itemIds == null)
		{
			return null;
		}

		Collection<Widget> bankItems = getBankItems(client);

		if (bankItems == null)
		{
			return null;
		}

		Map<Integer, Integer> items = new HashMap<>();

		for (Widget bankItem : bankItems)
		{
			if (itemIds.contains(bankItem.getItemId()))
			{
				items.put(bankItem.getItemId(), bankItem.getIndex());
			}
		}

		if (items.isEmpty())
		{
			return null;
		}

		return items;
	}

	private static Collection<Widget> getBankItems(Client client)
	{
		Collection<Widget> widgetItems = new ArrayList<>();
		Widget bank = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

		if (bank == null || bank.isHidden())
		{
			return null;
		}

		Widget[] children = bank.getDynamicChildren();
		for (Widget child : children)
		{
			if (child.getItemId() == 6512 || child.getItemId() == -1 || child.isSelfHidden())
			{
				continue;
			}

			widgetItems.add(child);
		}

		return widgetItems;
	}
}
