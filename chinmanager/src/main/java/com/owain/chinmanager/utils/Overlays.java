package com.owain.chinmanager.utils;

import static com.owain.automation.ContainerUtils.getBankInventoryWidgetItemForItemsPos;
import static com.owain.automation.ContainerUtils.getBankWidgetItemForItemsPos;
import static com.owain.automation.ContainerUtils.getInventoryWidgetItemForItemsPos;
import static com.owain.chinmanager.ChinManagerPlugin.highlightWidgetItem;
import static com.owain.chinmanager.utils.Api.getObject;
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
			case SPELL_CAST_ON_GAME_OBJECT:
			{
				TileObject tileObject = getObject(client, menuOptionClicked.getId(), menuOptionClicked.getParam0(), menuOptionClicked.getParam1());

				if (tileObject != null)
				{
					highlightTileObject = tileObject;
				}

				break;
			}
			case NPC_FIRST_OPTION:
			case NPC_SECOND_OPTION:
			case NPC_THIRD_OPTION:
			case NPC_FOURTH_OPTION:
			case NPC_FIFTH_OPTION:
			case ITEM_USE_ON_NPC:
			case SPELL_CAST_ON_NPC:
			{
				client.getNpcs().stream().filter((npc) -> npc.getIndex() == menuOptionClicked.getId()).findFirst().ifPresent(value -> highlightActor = value);

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
					highlightWidgetItem.add(getBankWidgetItemForItemsPos(menuOptionClicked.getParam0(), client));
				}
				else if (inventory != null && inventory.getId() == menuOptionClicked.getParam1())
				{
					highlightWidgetItem.add(getInventoryWidgetItemForItemsPos(menuOptionClicked.getParam0(), client));
				}
				else if (bankInventory != null && bankInventory.getId() == menuOptionClicked.getParam1())
				{
					highlightWidgetItem.add(getBankInventoryWidgetItemForItemsPos(menuOptionClicked.getParam0(), client));
				}

				break;
			}
			case ITEM_USE_ON_WIDGET_ITEM:
			{
				Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

				if (inventory != null && inventory.getId() == menuOptionClicked.getParam1())
				{
					highlightWidgetItem.add(getInventoryWidgetItemForItemsPos(menuOptionClicked.getParam0(), client));
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
}
