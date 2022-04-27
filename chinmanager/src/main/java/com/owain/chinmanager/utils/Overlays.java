package com.owain.chinmanager.utils;

import static com.owain.automation.ContainerUtils.getBankInventoryWidgetItemForItemsPos;
import static com.owain.automation.ContainerUtils.getBankWidgetItemForItemsPos;
import static com.owain.automation.ContainerUtils.getFirstInventoryItemsPos;
import static com.owain.automation.ContainerUtils.getInventoryWidgetItemForItemsPos;
import static com.owain.chinmanager.ChinManagerPlugin.highlightWidgetItem;
import static com.owain.chinmanager.utils.Api.getObject;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.ItemLayer;
import static net.runelite.api.MenuAction.WIDGET_TARGET_ON_GAME_OBJECT;
import static net.runelite.api.MenuAction.WIDGET_TARGET_ON_NPC;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;

public class Overlays
{
//	@Getter(AccessLevel.PUBLIC)
//	public static final List<WidgetItem> highlightWidgetItem = new ArrayList<>();

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private static Actor highlightActor = null;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private static ItemLayer highlightItemLayer = null;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private static TileObject highlightTileObject = null;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private static List<WorldPoint> highlightPath = null;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private static Widget highlightWidget = null;

	// Debug
	@Getter(AccessLevel.PUBLIC)
	public static final Map<TileObject, Integer> debugTileObjectMap = new HashMap<>();

	@Getter(AccessLevel.PUBLIC)
	public static final Set<WorldPoint> debugReachableWorldAreas = new HashSet<>();

	@Getter(AccessLevel.PUBLIC)
	public static final Map<WorldPoint, Integer> debugReachableTiles = new HashMap<>();

	public static void resetHighlight()
	{
		highlightActor = null;
		highlightItemLayer = null;
		highlightTileObject = null;
		debugTileObjectMap.clear();
		highlightWidgetItem.clear();
		debugReachableWorldAreas.clear();
		debugReachableTiles.clear();
		highlightPath = null;
		highlightWidget = null;
	}

	public static void highlight(Client client, MenuOptionClicked menuOptionClicked)
	{
		resetHighlight();

		switch (menuOptionClicked.getMenuAction())
		{
			case GAME_OBJECT_FIRST_OPTION:
			case GAME_OBJECT_SECOND_OPTION:
			case GAME_OBJECT_THIRD_OPTION:
			case GAME_OBJECT_FOURTH_OPTION:
			case GAME_OBJECT_FIFTH_OPTION:
			case ITEM_USE_ON_GAME_OBJECT:
			case WIDGET_TARGET_ON_GAME_OBJECT:
			{
				TileObject tileObject = getObject(client, menuOptionClicked.getId(), menuOptionClicked.getParam0(), menuOptionClicked.getParam1());

				if (tileObject != null)
				{
					highlightTileObject = tileObject;
				}

				if (menuOptionClicked.getMenuAction() == WIDGET_TARGET_ON_GAME_OBJECT && client.getSelectedSpellWidget() == WidgetInfo.INVENTORY.getPackedId())
				{
					int itemPos = getFirstInventoryItemsPos(client.getSelectedSpellItemId(), client);

					if (itemPos == -1)
					{
						break;
					}

					Widget widget = getInventoryWidgetItemForItemsPos(itemPos, client);

					if (widget == null)
					{
						break;
					}

					highlightWidgetItem.add(widgetToWidgetItem(widget, itemPos));
				}

				break;
			}
			case NPC_FIRST_OPTION:
			case NPC_SECOND_OPTION:
			case NPC_THIRD_OPTION:
			case NPC_FOURTH_OPTION:
			case NPC_FIFTH_OPTION:
			case ITEM_USE_ON_NPC:
			case WIDGET_TARGET_ON_NPC:
			{
				client.getNpcs().stream().filter((npc) -> npc.getIndex() == menuOptionClicked.getId()).findFirst().ifPresent(value -> highlightActor = value);

				if (menuOptionClicked.getMenuAction() == WIDGET_TARGET_ON_NPC && client.getSelectedSpellWidget() == WidgetInfo.INVENTORY.getPackedId())
				{
					int itemPos = getFirstInventoryItemsPos(client.getSelectedSpellItemId(), client);

					if (itemPos == -1)
					{
						break;
					}

					Widget widget = getInventoryWidgetItemForItemsPos(itemPos, client);

					if (widget == null)
					{
						break;
					}

					highlightWidgetItem.add(widgetToWidgetItem(widget, itemPos));
				}

				break;
			}
			case GROUND_ITEM_FIRST_OPTION:
			case GROUND_ITEM_SECOND_OPTION:
			case GROUND_ITEM_THIRD_OPTION:
			case GROUND_ITEM_FOURTH_OPTION:
			case GROUND_ITEM_FIFTH_OPTION:
			{
				LocalPoint localPoint = LocalPoint.fromScene(menuOptionClicked.getParam0(), menuOptionClicked.getParam1());

				Api.refreshTileObjects(client);
				Map.copyOf(Api.getTileItems())
					.values()
					.stream()
					.filter(Objects::nonNull)
					.filter(nonNullTile -> nonNullTile.getLocalLocation().equals(localPoint))
					.findFirst()
					.flatMap(tile -> tile
						.getGroundItems()
						.stream()
						.filter((tileItem) -> tileItem.getId() == menuOptionClicked.getId())
						.findFirst()
					)
					.ifPresent(value -> highlightItemLayer = value.getTile().getItemLayer());

				break;
			}
			case CC_OP:
			case CC_OP_LOW_PRIORITY:
			{
				if (menuOptionClicked.getParam0() == -1 && !menuOptionClicked.getMenuOption().equals("Toggle Run"))
				{
					Widget widget = client.getWidget(menuOptionClicked.getParam1());

					if (widget != null)
					{
						highlightWidget = widget;

						return;
					}
				}

				Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
				Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
				Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

				if (bankContainer != null && bankContainer.getId() == menuOptionClicked.getParam1())
				{
					WidgetItem widgetItem = getBankWidgetItemForItemsPos(menuOptionClicked.getParam0(), client);

					if (widgetItem == null)
					{
						break;
					}

					highlightWidgetItem.add(widgetItem);
				}
				else if (inventory != null && inventory.getId() == menuOptionClicked.getParam1())
				{
					Widget widget = getInventoryWidgetItemForItemsPos(menuOptionClicked.getParam0(), client);

					if (widget == null)
					{
						break;
					}

					highlightWidgetItem.add(widgetToWidgetItem(widget, menuOptionClicked.getParam0()));
				}
				else if (bankInventory != null && bankInventory.getId() == menuOptionClicked.getParam1())
				{
					WidgetItem widgetItem = getBankInventoryWidgetItemForItemsPos(menuOptionClicked.getParam0(), client);

					if (widgetItem == null)
					{
						break;
					}

					highlightWidgetItem.add(widgetItem);
				}

				break;
			}
			case ITEM_USE_ON_ITEM:
			{
				Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

				if (inventory != null && inventory.getId() == menuOptionClicked.getParam1())
				{
					highlightWidget = getInventoryWidgetItemForItemsPos(menuOptionClicked.getParam0(), client);
				}
			}
		}
	}

	// TODO: Remove this
	@Deprecated
	public static List<WidgetItem> getHighlightWidgetItem()
	{
		return highlightWidgetItem;
	}

	public static WidgetItem widgetToWidgetItem(Widget child, int i)
	{
		boolean isDragged = child.isWidgetItemDragged(child.getItemId());
		int dragOffsetX = 0;
		int dragOffsetY = 0;

		if (isDragged)
		{
			Point p = child.getWidgetItemDragOffsets();
			dragOffsetX = p.getX();
			dragOffsetY = p.getY();
		}

		Rectangle bounds = child.getBounds();
		bounds.setBounds(bounds.x, bounds.y, 32, 32);

		Rectangle dragBounds = child.getBounds();
		dragBounds.setBounds(bounds.x + dragOffsetX, bounds.y + dragOffsetY, 32, 32);

		return new WidgetItem(child.getItemId(), child.getItemQuantity(), i, bounds, child, dragBounds);
	}
}
