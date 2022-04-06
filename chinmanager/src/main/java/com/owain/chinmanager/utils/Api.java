package com.owain.chinmanager.utils;

import static com.owain.automation.ContainerUtils.hasBankInventoryItem;
import static com.owain.automation.ContainerUtils.hasBankItem;
import static com.owain.automation.ContainerUtils.hasItem;
import com.owain.chinmanager.Runes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.DecorativeObject;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
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
import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.DecorativeObjectChanged;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GroundObjectChanged;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.WallObjectChanged;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.queries.DecorativeObjectQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.GroundObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.queries.WallObjectQuery;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.tuple.Pair;

public class Api
{
	private static final Varbits[] AMOUNT_VARBITS =
		{
			Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3
		};
	private static final Varbits[] RUNE_VARBITS =
		{
			Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3
		};

	@Getter(AccessLevel.PUBLIC)
	private final static Map<TileItem, Tile> tileItems = new HashMap<>();

	@Getter(AccessLevel.PUBLIC)
	private final static Set<TileObject> objects = new HashSet<>();

	@Getter(AccessLevel.PUBLIC)
	private final static Set<Actor> actors = new HashSet<>();

	@Subscribe(priority = -98)
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() != GameState.LOGGED_IN && gameStateChanged.getGameState() != GameState.CONNECTION_LOST)
		{
			getTileItems().clear();
			getObjects().clear();
			getActors().clear();
		}
	}

	@Subscribe(priority = -98)
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		actors.add(npcSpawned.getNpc());
	}

	@Subscribe(priority = -98)
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();

		actors.remove(npc);

		if (Overlays.getHighlightActor() == npc)
		{
			Overlays.setHighlightActor(null);
		}
	}

	@Subscribe(priority = -98)
	public void onPlayerSpawned(PlayerSpawned playerSpawned)
	{
		actors.add(playerSpawned.getPlayer());
	}

	@Subscribe(priority = -98)
	public void onPlayerDespawned(PlayerDespawned playerDespawned)
	{
		Player player = playerDespawned.getPlayer();

		actors.remove(player);

		if (Overlays.getHighlightActor() == player)
		{
			Overlays.setHighlightActor(null);
		}
	}

	@Subscribe(priority = -98)
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		Tile tile = itemSpawned.getTile();
		TileItem item = itemSpawned.getItem();

		tileItems.put(item, tile);
	}

	@Subscribe(priority = -98)
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		TileItem item = itemDespawned.getItem();

		tileItems.remove(item);

		if (Overlays.getHighlightItemLayer() == item.getTile().getItemLayer())
		{
			Overlays.setHighlightItemLayer(null);
		}
	}

	@Subscribe(priority = -98)
	public void onWallObjectSpawned(WallObjectSpawned wallObjectSpawned)
	{
		objects.add(wallObjectSpawned.getWallObject());
	}

	@Subscribe(priority = -98)
	public void onWallObjectChanged(WallObjectChanged wallObjectChanged)
	{
		objects.remove(wallObjectChanged.getPrevious());
		objects.add(wallObjectChanged.getWallObject());
	}

	@Subscribe(priority = -98)
	public void onWallObjectDespawned(WallObjectDespawned wallObjectDespawned)
	{
		TileObject tileObject = wallObjectDespawned.getWallObject();

		objects.remove(tileObject);

		if (Overlays.getHighlightTileObject() == tileObject)
		{
			Overlays.setHighlightTileObject(null);
		}

		Overlays.getDebugTileObjectMap().remove(tileObject);
	}

	@Subscribe(priority = -98)
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		objects.add(gameObjectSpawned.getGameObject());
	}

	@Subscribe(priority = -98)
	public void onGameObjectChanged(GameObjectChanged gameObjectChanged)
	{
		objects.remove(gameObjectChanged.getPrevious());
		objects.add(gameObjectChanged.getGameObject());
	}

	@Subscribe(priority = -98)
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		TileObject tileObject = gameObjectDespawned.getGameObject();

		objects.remove(tileObject);

		if (Overlays.getHighlightTileObject() == tileObject)
		{
			Overlays.setHighlightTileObject(null);
		}

		Overlays.getDebugTileObjectMap().remove(tileObject);
	}

	@Subscribe(priority = -98)
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned decorativeObjectSpawned)
	{
		objects.add(decorativeObjectSpawned.getDecorativeObject());
	}

	@Subscribe(priority = -98)
	public void onDecorativeObjectChanged(DecorativeObjectChanged decorativeObjectChanged)
	{
		objects.remove(decorativeObjectChanged.getPrevious());
		objects.add(decorativeObjectChanged.getDecorativeObject());
	}

	@Subscribe(priority = -98)
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned decorativeObjectDespawned)
	{
		TileObject tileObject = decorativeObjectDespawned.getDecorativeObject();

		objects.remove(tileObject);

		if (Overlays.getHighlightTileObject() == tileObject)
		{
			Overlays.setHighlightTileObject(null);
		}

		Overlays.getDebugTileObjectMap().remove(tileObject);
	}

	@Subscribe(priority = -98)
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned)
	{
		objects.add(groundObjectSpawned.getGroundObject());
	}

	@Subscribe(priority = -98)
	public void onGroundObjectChanged(GroundObjectChanged groundObjectChanged)
	{
		objects.remove(groundObjectChanged.getPrevious());
		objects.add(groundObjectChanged.getGroundObject());
	}

	@Subscribe(priority = -98)
	public void onGroundObjectDespawned(GroundObjectDespawned groundObjectDespawned)
	{
		TileObject tileObject = groundObjectDespawned.getGroundObject();

		objects.remove(tileObject);

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
		return Set.copyOf(
				getActors()
			)
			.stream()
			.filter(Objects::nonNull)
			.filter(npc -> npc instanceof NPC)
			.map(npc -> (NPC) npc)
			.filter(npc -> ids.contains(npc.getId()))
			.min(Comparator.comparing(npc ->
				npc
					.getWorldLocation()
					.distanceTo(locatable.getWorldLocation())
			))
			.orElse(
				new NPCQuery()
					.idEquals(ids)
					.result(client)
					.nearestTo(client.getLocalPlayer())
			);
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
		return Set.copyOf(
				getObjects()
			)
			.stream()
			.filter(tileObject -> ids.contains(tileObject.getId()))
			.filter(tileObject -> tileObject.getPlane() == client.getPlane())
			.min(Comparator.comparing(tileObject ->
				tileObject
					.getWorldLocation()
					.distanceTo(locatable.getWorldLocation())
			))
			.orElse(getObjectAlt(client, ids, locatable));
	}

	public static TileObject getObject(Client client, int id, int x, int y)
	{
		WorldPoint wp = WorldPoint.fromScene(client, x, y, client.getPlane());

		return Set.copyOf(
				getObjects()
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
			.orElse(getObjectAlt(client, id, wp));
	}

	public static TileObject getObject(Client client, WorldPoint wp)
	{
		return Set.copyOf(
				getObjects()
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
			.orElse(getObjectAlt(client, wp));
	}

	public static TileObject getObjectAlt(Client client, List<Integer> ids, Locatable locatable)
	{
		GameObject gameObject = new GameObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(locatable);

		if (gameObject != null)
		{
			return gameObject;
		}

		DecorativeObject decorativeObject = new DecorativeObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(locatable);

		if (decorativeObject != null)
		{
			return decorativeObject;
		}

		GroundObject groundObject = new GroundObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(locatable);

		if (groundObject != null)
		{
			return groundObject;
		}

		WallObject wallObject = new WallObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(locatable);

		if (wallObject != null)
		{
			return wallObject;
		}

		return null;
	}

	public static TileObject getObjectAlt(Client client, int id, WorldPoint wp)
	{
		GameObject gameObject = new GameObjectQuery()
			.idEquals(id)
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (gameObject != null)
		{
			return gameObject;
		}

		DecorativeObject decorativeObject = new DecorativeObjectQuery()
			.idEquals(id)
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (decorativeObject != null)
		{
			return decorativeObject;
		}

		GroundObject groundObject = new GroundObjectQuery()
			.idEquals(id)
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (groundObject != null)
		{
			return groundObject;
		}

		WallObject wallObject = new WallObjectQuery()
			.idEquals(id)
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (wallObject != null)
		{
			return wallObject;
		}

		return null;
	}

	public static TileObject getObjectAlt(Client client, WorldPoint wp)
	{
		GameObject gameObject = new GameObjectQuery()
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (gameObject != null)
		{
			return gameObject;
		}

		DecorativeObject decorativeObject = new DecorativeObjectQuery()
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (decorativeObject != null)
		{
			return decorativeObject;
		}

		GroundObject groundObject = new GroundObjectQuery()
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (groundObject != null)
		{
			return groundObject;
		}

		WallObject wallObject = new WallObjectQuery()
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (wallObject != null)
		{
			return wallObject;
		}

		return null;
	}

	public static TileObject getBankObject(Client client)
	{
		Locatable locatable = client.getLocalPlayer();

		return Set.copyOf(
				getObjects()
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
				getBankObjectAlt(client, locatable)
			);
	}

	public static TileObject getBankObjectAlt(Client client, Locatable locatable)
	{
		return getAllObjects(client)
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

				return actions.contains("Bank") || actions.contains("Collect") ||
					imposterActions.contains("Bank") || imposterActions.contains("Collect");
			})
			.filter(tileObject -> tileObject.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < 16)
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

	public static NPC getBankNpc(Client client)
	{
		Locatable locatable = client.getLocalPlayer();

		return Set.copyOf(
				getActors()
			)
			.stream()
			.filter(Objects::nonNull)
			.filter(npc -> npc instanceof NPC)
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
			.orElse(
				getBankNpcAlt(client, locatable)
			);
	}

	public static NPC getBankNpcAlt(Client client, Locatable locatable)
	{
		return client.getNpcs()
			.stream()
			.filter(Objects::nonNull)
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
		Overlays.getDebugReachableWorldAreas().clear();
		Overlays.getDebugReachableTiles().clear();
		Overlays.getDebugTileObjectMap().clear();

		return Set.copyOf(
				getObjects()
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

	private static Collection<TileObject> getAllObjects(Client client)
	{
		Collection<TileObject> objects = new ArrayList<>();
		for (Tile tile : getTiles(client))
		{
			GameObject[] gameObjects = tile.getGameObjects();
			if (gameObjects != null)
			{
				objects.addAll(Arrays.asList(gameObjects));
			}

			DecorativeObject decorativeObject = tile.getDecorativeObject();
			if (decorativeObject != null)
			{
				objects.add(decorativeObject);
			}

			GroundObject groundobject = tile.getGroundObject();
			if (groundobject != null)
			{
				objects.add(groundobject);
			}

			WallObject wallObject = tile.getWallObject();
			if (wallObject != null)
			{
				objects.add(wallObject);
			}
		}
		return objects;
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
			Varbits amountVarbit = AMOUNT_VARBITS[i];

			int amount = client.getVar(amountVarbit);
			if (amount <= 0)
			{
				continue;
			}

			Varbits runeVarbit = RUNE_VARBITS[i];
			int runeId = client.getVar(runeVarbit);
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
}
