package com.owain.chinmanager.utils;

import static com.owain.automation.ContainerUtils.hasBankInventoryItem;
import static com.owain.automation.ContainerUtils.hasBankItem;
import static com.owain.automation.ContainerUtils.hasItem;
import com.owain.chinmanager.Runes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.DecorativeObject;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Locatable;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public class Api
{
	private static final int[] AMOUNT_VARBITS =
		{
			Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3
		};
	private static final int[] RUNE_VARBITS =
		{
			Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3
		};

	@Getter(AccessLevel.PUBLIC)
	private final static Map<TileItem, Tile> tileItems = new HashMap<>();
	@Getter(AccessLevel.PUBLIC)
	public final static Set<TileObject> objects = new HashSet<>();

	private static boolean cached = false;

	@Subscribe(priority = -98)
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		cached = false;
	}

	@Subscribe(priority = -98)
	public void onGameTick(GameTick gameTick)
	{
		cached = false;
	}

	@Subscribe(priority = -98)
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();

		if (Overlays.getHighlightActor() == npc)
		{
			Overlays.setHighlightActor(null);
		}
	}

	@Subscribe(priority = -98)
	public void onPlayerDespawned(PlayerDespawned playerDespawned)
	{
		Player player = playerDespawned.getPlayer();

		if (Overlays.getHighlightActor() == player)
		{
			Overlays.setHighlightActor(null);
		}
	}

	@Subscribe(priority = -98)
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		TileItem item = itemDespawned.getItem();

		if (Overlays.getHighlightItemLayer() == item.getTile().getItemLayer())
		{
			Overlays.setHighlightItemLayer(null);
		}
	}

	@Subscribe(priority = -98)
	public void onWallObjectDespawned(WallObjectDespawned wallObjectDespawned)
	{
		TileObject tileObject = wallObjectDespawned.getWallObject();

		if (Overlays.getHighlightTileObject() == tileObject)
		{
			Overlays.setHighlightTileObject(null);
		}

		Overlays.getDebugTileObjectMap().remove(tileObject);
	}

	@Subscribe(priority = -98)
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		TileObject tileObject = gameObjectDespawned.getGameObject();

		if (Overlays.getHighlightTileObject() == tileObject)
		{
			Overlays.setHighlightTileObject(null);
		}

		Overlays.getDebugTileObjectMap().remove(tileObject);
	}

	@Subscribe(priority = -98)
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned decorativeObjectDespawned)
	{
		TileObject tileObject = decorativeObjectDespawned.getDecorativeObject();

		if (Overlays.getHighlightTileObject() == tileObject)
		{
			Overlays.setHighlightTileObject(null);
		}

		Overlays.getDebugTileObjectMap().remove(tileObject);
	}

	@Subscribe(priority = -98)
	public void onGroundObjectDespawned(GroundObjectDespawned groundObjectDespawned)
	{
		TileObject tileObject = groundObjectDespawned.getGroundObject();

		if (Overlays.getHighlightTileObject() == tileObject)
		{
			Overlays.setHighlightTileObject(null);
		}

		Overlays.getDebugTileObjectMap().remove(tileObject);
	}

	public static NPC getNPC(Client client, int id)
	{
		return getNPC(client, List.of(id));
	}

	public static NPC getNPC(Client client, List<Integer> ids)
	{
		return getNPC(client, ids, client.getLocalPlayer());
	}

	public static NPC getNPC(Client client, List<Integer> ids, Locatable locatable)
	{
		return client.getNpcs()
			.stream()
			.filter(Objects::nonNull)
			.map(npc -> (NPC) npc)
			.filter(npc -> ids.contains(npc.getId()))
			.min(Comparator.comparing(npc ->
				npc
					.getWorldLocation()
					.distanceTo(locatable.getWorldLocation())
			))
			.orElse(null);
	}

	public static TileObject getObject(Client client, int id)
	{
		return getObject(client, List.of(id));
	}

	public static TileObject getObject(Client client, List<Integer> ids)
	{
		return getObject(client, ids, client.getLocalPlayer());
	}

	public static TileObject getObject(Client client, List<Integer> ids, Locatable locatable)
	{
		refreshTileObjects(client);

		return Set.copyOf(
				objects
			)
			.stream()
			.filter(tileObject -> ids.contains(tileObject.getId()))
			.filter(tileObject -> tileObject.getPlane() == client.getPlane())
			.min(Comparator.comparing(tileObject ->
				tileObject
					.getWorldLocation()
					.distanceTo(locatable.getWorldLocation())
			))
			.orElse(null);
	}

	public static TileObject getObject(Client client, int id, int x, int y)
	{
		refreshTileObjects(client);

		WorldPoint wp = WorldPoint.fromScene(client, x, y, client.getPlane());

		return Set.copyOf(
				objects
			)
			.stream()
			.filter(Objects::nonNull)
			.filter(tileObject -> tileObject.getId() == id)
			.filter(tileObject -> tileObject.getPlane() == client.getPlane())
			.filter(tileObject -> {
				if (tileObject instanceof GameObject)
				{
					GameObject gameObject = (GameObject) tileObject;

					Point sceneLocation = gameObject.getSceneMinLocation();

					if (sceneLocation.getX() == x && sceneLocation.getY() == y)
					{
						return true;
					}
				}

				if (tileObject.getWorldLocation().equals(wp))
				{
					return true;
				}

				return false;
			})
			.min(Comparator.comparing(tileObject ->
				tileObject
					.getWorldLocation()
					.distanceTo(
						client
							.getLocalPlayer()
							.getWorldLocation()
					)
			))
			.orElse(null);
	}

	public static TileObject getObject(Client client, WorldPoint wp)
	{
		refreshTileObjects(client);

		return Set.copyOf(
				objects
			)
			.stream()
			.filter(Objects::nonNull)
			.filter(tileObject -> tileObject.getWorldLocation().equals(wp))
			.min(Comparator.comparing(tileObject ->
				tileObject
					.getWorldLocation()
					.distanceTo(
						client
							.getLocalPlayer()
							.getWorldLocation()
					)
			))
			.orElse(null);
	}

	public static TileObject getBankObject(Client client)
	{
		refreshTileObjects(client);

		Locatable locatable = client.getLocalPlayer();

		return Set.copyOf(
				objects
			)
			.stream()
			.filter(Objects::nonNull)
			.filter(tileObject -> {
				List<String> actions = Arrays.asList(
					client.getObjectDefinition(
							tileObject.getId()
						)
						.getActions()
				);

				List<String> imposterActions = new ArrayList<>();

				ObjectComposition objectComposition = client.getObjectDefinition(tileObject.getId());
				int[] ids = objectComposition.getImpostorIds();

				if (ids != null && ids.length > 0)
				{
					ObjectComposition imposter = objectComposition.getImpostor();

					if (imposter != null)
					{
						imposterActions.addAll(Arrays.asList(imposter.getActions()));
					}
				}

				return actions.contains("Bank") || (actions.contains("Collect") && !objectComposition.getName().contains("Grand")) ||
					imposterActions.contains("Bank") || (imposterActions.contains("Collect") && !objectComposition.getImpostor().getName().contains("Grand"));
			})
			.filter(tileObject -> tileObject.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < 16)
			.filter(tileObject -> Reachable.isInteractable(client, tileObject))
			.map(tileObject -> {
				Tile startTile = tile(client, locatable.getWorldLocation());
				Tile endTile = tile(client, tileObject.getWorldLocation());

				if (startTile == null || endTile == null)
				{
					return Pair.of(tileObject, Integer.MAX_VALUE);
				}

				if (locatable.getWorldLocation().dx(1).distanceTo(tileObject.getWorldLocation()) == 0)
				{
					return Pair.of(tileObject, -1);
				}
				else if (locatable.getWorldLocation().dy(1).distanceTo(tileObject.getWorldLocation()) == 0)
				{
					return Pair.of(tileObject, -1);
				}
				else if (locatable.getWorldLocation().dx(-1).distanceTo(tileObject.getWorldLocation()) == 0)
				{
					return Pair.of(tileObject, -1);
				}
				else if (locatable.getWorldLocation().dy(-1).distanceTo(tileObject.getWorldLocation()) == 0)
				{
					return Pair.of(tileObject, -1);
				}

				List<Tile> path = startTile.pathTo(endTile);

				return Pair.of(tileObject, path.size());
			})
			.filter(pair -> pair.getValue() < Integer.MAX_VALUE)
			.peek(pair -> Overlays.getDebugTileObjectMap().put(pair.getKey(), pair.getValue()))
			.min(
				Comparator.comparing(
						(Pair<TileObject, Integer> pair) ->
							pair.getKey().getWorldLocation().distanceTo(locatable.getWorldLocation())
					)
					.thenComparing(Pair::getValue))
			.map(Pair::getKey)
			.orElse(
				null
			);
	}

	public static NPC getBankNpc(Client client)
	{
		Locatable locatable = client.getLocalPlayer();

		return client.getNpcs()
			.stream()
			.filter(Objects::nonNull)
			.map(npc -> (NPC) npc)
			.filter(npc -> {
				List<String> actions = Arrays.asList(
					client.getNpcDefinition(
							npc.getId()
						)
						.getActions()
				);

				return actions.contains("Bank");
			})
			.filter(npc -> npc.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < 16)
			.map(tileObject -> {
				Tile startTile = tile(client, locatable.getWorldLocation());
				Tile endTile = tile(client, tileObject.getWorldLocation());

				if (startTile == null || endTile == null)
				{
					return Pair.of(tileObject, Integer.MAX_VALUE);
				}

				if (locatable.getWorldLocation().dx(1).distanceTo(tileObject.getWorldLocation()) == 0)
				{
					return Pair.of(tileObject, -1);
				}
				else if (locatable.getWorldLocation().dy(1).distanceTo(tileObject.getWorldLocation()) == 0)
				{
					return Pair.of(tileObject, -1);
				}
				else if (locatable.getWorldLocation().dx(-1).distanceTo(tileObject.getWorldLocation()) == 0)
				{
					return Pair.of(tileObject, -1);
				}
				else if (locatable.getWorldLocation().dy(-1).distanceTo(tileObject.getWorldLocation()) == 0)
				{
					return Pair.of(tileObject, -1);
				}

				List<Tile> path = startTile.pathTo(endTile);

				return Pair.of(tileObject, path.size());
			})
			.filter(pair -> pair.getValue() < Integer.MAX_VALUE)
			.min(
				Comparator.comparing(
						(Pair<NPC, Integer> pair) ->
							pair.getKey().getWorldLocation().distanceTo(locatable.getWorldLocation())
					)
					.thenComparing(Pair::getValue))
			.map(Pair::getKey)
			.orElse(null);
	}

	public static boolean isAtBank(Client client)
	{
		return getBankNpc(client) != null || getBankObject(client) != null;
	}

	public static Point getLocation(TileObject tileObject)
	{
		if (tileObject instanceof GameObject)
		{
			return ((GameObject) tileObject).getSceneMinLocation();
		}
		else
		{
			return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
		}
	}

	public static TileObject getReachableObject(Client client, int id, int limit)
	{
		return getReachableObject(client, List.of(id), limit);
	}

	public static TileObject getReachableObject(Client client, List<Integer> ids, int limit)
	{
		return getReachableObject(client, ids, limit, client.getLocalPlayer());
	}

	public static TileObject getReachableObject(Client client, List<Integer> ids, int limit, Locatable locatable)
	{
		refreshTileObjects(client);

		Overlays.getDebugReachableWorldAreas().clear();
		Overlays.getDebugReachableTiles().clear();
		Overlays.getDebugTileObjectMap().clear();

		return Set.copyOf(
				objects
			)
			.stream()
			.filter(Objects::nonNull)
			.filter(tileObject -> ids.contains(tileObject.getId()))
			.filter(tileObject -> tileObject.getPlane() == client.getPlane())
			.sorted(Comparator.comparing(tileObject -> locatable.getWorldLocation().distanceTo(tileObject.getWorldLocation())))
			.limit(limit)
			.filter(tileObject -> tileObject.getWorldLocation().distanceTo(new WorldPoint(1787, 3589, 0)) != 0)
			.filter(tileObject -> tileObject.getWorldLocation().distanceTo(new WorldPoint(1787, 3599, 0)) != 0)
			.filter(tileObject -> tileObject.getWorldLocation().distanceTo(new WorldPoint(3255, 3463, 0)) != 0)
			.filter(tileObject -> Reachable.isInteractable(client, tileObject))
			.map(tileObject -> {
				Tile startTile = tile(client, locatable.getWorldLocation());
				Tile endTile = tile(client, tileObject.getWorldLocation());

				if (startTile == null || endTile == null)
				{
					return Pair.of(tileObject, Integer.MAX_VALUE);
				}

				if (locatable.getWorldLocation().dx(1).distanceTo(tileObject.getWorldLocation()) == 0)
				{
					return Pair.of(tileObject, -1);
				}
				else if (locatable.getWorldLocation().dy(1).distanceTo(tileObject.getWorldLocation()) == 0)
				{
					return Pair.of(tileObject, -1);
				}
				else if (locatable.getWorldLocation().dx(-1).distanceTo(tileObject.getWorldLocation()) == 0)
				{
					return Pair.of(tileObject, -1);
				}
				else if (locatable.getWorldLocation().dy(-1).distanceTo(tileObject.getWorldLocation()) == 0)
				{
					return Pair.of(tileObject, -1);
				}

				List<Tile> path = startTile.pathTo(endTile);

				return Pair.of(tileObject, path.size());
			})
			.filter(pair -> pair.getValue() < Integer.MAX_VALUE)
			.peek(pair -> Overlays.getDebugTileObjectMap().put(pair.getKey(), pair.getValue()))
			.min(
				Comparator.comparing(
						(Pair<TileObject, Integer> pair) ->
							pair.getKey().getWorldLocation().distanceTo(locatable.getWorldLocation())
					)
					.thenComparing(Pair::getValue))
			.map(Pair::getKey)
			.orElse(null);
	}

	public static boolean canReachWorldPointOrSurrounding(Client client, WorldPoint worldPoint)
	{
		Overlays.getDebugReachableWorldAreas().clear();
		Overlays.getDebugReachableTiles().clear();
		Overlays.getDebugTileObjectMap().clear();

		TileObject object = getObject(client, worldPoint);

		if (object == null)
		{
			Tile start = tile(client, client.getLocalPlayer().getWorldLocation());
			Tile target = tile(client, worldPoint);

			if (start == null || target == null)
			{
				return false;
			}

			List<Tile> path = start.pathTo(target);

			if (path == null)
			{
				return false;
			}
			else if (path.get(path.size() - 1).getWorldLocation().distanceTo(worldPoint) == 0)
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		int width = 1;
		int height = 1;

		if (object instanceof GameObject)
		{
			width = ((GameObject) object).sizeX();
			height = ((GameObject) object).sizeY();
		}

		WorldPoint objectWorldPoint;
		objectWorldPoint = object.getWorldLocation();

		if (width == 3 || height == 3)
		{
			objectWorldPoint = new WorldPoint(width == 3 ? objectWorldPoint.getX() - 1 : objectWorldPoint.getX(), height == 3 ? objectWorldPoint.getY() - 1 : objectWorldPoint.getY(), objectWorldPoint.getPlane());
		}

		List<WorldPoint> area = new WorldArea(objectWorldPoint.getX() - 1, objectWorldPoint.getY() - 1, width + 2, height + 2, objectWorldPoint.getPlane()).toWorldPointList();

		Overlays.getDebugReachableWorldAreas().addAll(area);

		for (WorldPoint wp : area)
		{
			if ((getObject(client, wp) instanceof GameObject) ||
				wp.getX() > objectWorldPoint.getX() && wp.getY() > objectWorldPoint.getY() ||
				wp.getX() > objectWorldPoint.getX() && wp.getY() < objectWorldPoint.getY() ||
				wp.getX() < objectWorldPoint.getX() && wp.getY() > objectWorldPoint.getY() ||
				wp.getX() < objectWorldPoint.getX() && wp.getY() < objectWorldPoint.getY())
			{
				continue;
			}

			Tile startTile = tile(client, client.getLocalPlayer().getWorldLocation());
			Tile endTile = tile(client, wp);

			if (startTile == null || endTile == null)
			{
				continue;
			}

			List<Tile> path = startTile.pathTo(endTile);

			if (path != null && path.get(path.size() - 1).getWorldLocation().distanceTo(wp) == 0)
			{

				Overlays.getDebugTileObjectMap().put(object, path.size());
				return true;
			}
		}

		return false;
	}

	private static List<Tile> getTiles(Client client)
	{
		List<Tile> tilesList = new ArrayList<>();
		Scene scene = client.getScene();
		Tile[][] tiles = scene.getTiles()[client.getPlane()];

		for (int x = 0; x < Constants.SCENE_SIZE; ++x)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; ++y)
			{
				Tile tile = tiles[x][y];
				if (tile == null)
				{
					continue;
				}
				tilesList.add(tile);
			}
		}

		return tilesList;
	}

	private static Tile tile(Client client, WorldPoint position)
	{
		int plane = position.getPlane();
		int x = position.getX() - client.getBaseX();
		int y = position.getY() - client.getBaseY();

		if (plane < 0 || plane >= 4)
		{
			return null;
		}
		if (x < 0 || x >= 104)
		{
			return null;
		}
		if (y < 0 || y >= 104)
		{
			return null;
		}

		return client.getScene().getTiles()[plane][x][y];
	}

	public static int getLowestItemMatch(List<Integer> items, Client client)
	{
		ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		List<Integer> equipmentItems = new ArrayList<>();

		if (itemContainer != null)
		{
			for (final EquipmentInventorySlot slot : EquipmentInventorySlot.values())
			{
				int i = slot.getSlotIdx();
				Item item = itemContainer.getItem(i);

				if (item != null)
				{
					equipmentItems.add(item.getId());
				}
			}
		}

		for (int item : items)
		{
			if (client.getItemContainer(InventoryID.BANK) != null && hasBankItem(item, client))
			{
				return item;
			}
			else if (client.getItemContainer(InventoryID.BANK) == null && hasItem(item, client))
			{
				return item;
			}
			else if (client.getItemContainer(InventoryID.BANK) == null && equipmentItems.contains(item))
			{
				return item;
			}
		}

		return -1;
	}

	public static int runeOrRunepouch(Runes runes, Client client)
	{
		if (!hasItem(ItemID.RUNE_POUCH, client) && !hasBankInventoryItem(ItemID.RUNE_POUCH, client) && !hasBankItem(ItemID.RUNE_POUCH, client))
		{
			return runes.getItemId();
		}

		for (int i = 0; i < AMOUNT_VARBITS.length; i++)
		{
			int amountVarbit = AMOUNT_VARBITS[i];

			int amount = client.getVarbitValue(amountVarbit);
			if (amount <= 0)
			{
				continue;
			}

			int runeVarbit = RUNE_VARBITS[i];
			int runeId = client.getVarbitValue(runeVarbit);
			Runes rune = Runes.getRune(runeId);
			if (rune == null)
			{
				continue;
			}

			if (rune.getItemId() == runes.getItemId())
			{
				return ItemID.RUNE_POUCH;
			}
			else
			{
				switch (rune)
				{
					case MIST:
						if (runes == Runes.AIR || runes == Runes.WATER)
						{
							return ItemID.RUNE_POUCH;
						}
						break;
					case DUST:
						if (runes == Runes.AIR || runes == Runes.EARTH)
						{
							return ItemID.RUNE_POUCH;
						}
						break;
					case MUD:
						if (runes == Runes.EARTH || runes == Runes.WATER)
						{
							return ItemID.RUNE_POUCH;
						}
						break;
					case SMOKE:
						if (runes == Runes.AIR || runes == Runes.FIRE)
						{
							return ItemID.RUNE_POUCH;
						}
						break;
					case STEAM:
						if (runes == Runes.FIRE || runes == Runes.WATER)
						{
							return ItemID.RUNE_POUCH;
						}
						break;
					case LAVA:
						if (runes == Runes.EARTH || runes == Runes.FIRE)
						{
							return ItemID.RUNE_POUCH;
						}
						break;
				}
			}
		}

		return runes.getItemId();
	}

	public static void refreshTileObjects(Client client)
	{
		Player localPlayer = client.getLocalPlayer();

		if (localPlayer == null)
		{
			return;
		}

		if (!cached)
		{
			tileItems.clear();
			objects.clear();

			tileObjects(client);

			cached = true;
		}
	}

	private static void tileObjects(Client client)
	{
		Player player = client.getLocalPlayer();

		if (player == null)
		{
			return;
		}

		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();

		int z = client.getPlane();

		for (int x = 0; x < Constants.SCENE_SIZE; ++x)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; ++y)
			{
				Tile tile = tiles[z][x][y];

				if (tile == null)
				{
					continue;
				}

				groundItems(tile);
				groundObjects(tile);
				gameObjects(tile);
				wallObjects(tile);
				decorationObjects(tile);
			}
		}
	}

	private static void groundItems(Tile tile)
	{
		List<TileItem> groundItems = tile.getGroundItems();
		if (groundItems != null && !groundItems.isEmpty())
		{
			for (TileItem tileItem : groundItems)
			{
				tileItems.put(tileItem, tile);
			}
		}
	}

	private static void groundObjects(Tile tile)
	{
		TileObject groundObject = tile.getGroundObject();
		if (groundObject != null)
		{
			objects.add(groundObject);
		}
	}

	private static void gameObjects(Tile tile)
	{
		GameObject[] gameObjects = tile.getGameObjects();
		if (gameObjects != null)
		{
			for (GameObject gameObject : gameObjects)
			{
				if (gameObject != null && gameObject.getSceneMinLocation().equals(tile.getSceneLocation()))
				{
					objects.add(gameObject);
				}
			}
		}
	}

	private static void wallObjects(Tile tile)
	{
		TileObject wallObject = tile.getWallObject();
		if (wallObject != null)
		{
			objects.add(wallObject);
		}
	}

	private static void decorationObjects(Tile tile)
	{
		DecorativeObject decorObject = tile.getDecorativeObject();
		if (decorObject != null)
		{
			objects.add(decorObject);
		}
	}
}
