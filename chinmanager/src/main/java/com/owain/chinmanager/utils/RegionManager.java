package com.owain.chinmanager.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.owain.chinmanager.api.RegionApi;
import com.owain.chinmanager.models.TileFlag;
import com.owain.chinmanager.models.Transport;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.coords.Direction;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.http.api.xtea.XteaKey;
import net.runelite.http.api.xtea.XteaRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

@Singleton
@Slf4j
public class RegionManager
{
	private static final int VERSION = 3;
	public static final MediaType JSON_MEDIATYPE = MediaType.parse("application/json");
	public static final String API_URL = "https://collisionmap.xyz";
	public static final Gson GSON = new GsonBuilder().create();

	private final Set<Integer> sentRegions = new HashSet<>();
	private int plane = -1;

	private final Client client;
	private final OkHttpClient okHttpClient;
	private final RegionApi regionApi;

	@Inject
	public RegionManager(
		Client client,
		OkHttpClient okHttpClient,
		RegionApi regionApi
	)
	{
		this.client = client;
		this.okHttpClient = okHttpClient;
		this.regionApi = regionApi;
	}

	@Subscribe(priority = -98)
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			sendRegion();
			sendXtea();
		}

		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			plane = -1;
		}
	}

	@Subscribe(priority = -98)
	public void onGameTick(GameTick gameTick)
	{
		Player localPlayer = client.getLocalPlayer();

		if (localPlayer != null)
		{
			if (plane == -1)
			{
				plane = localPlayer.getWorldLocation().getPlane();
			}
			else if (plane != localPlayer.getWorldLocation().getPlane())
			{
				plane = localPlayer.getWorldLocation().getPlane();
				sendRegion();
			}
		}
	}

	public void sendXtea()
	{
		int revision = client.getRevision();
		int[] regions = client.getMapRegions();
		int[][] xteaKeys = client.getXteaKeys();

		XteaRequest xteaRequest = new XteaRequest();
		xteaRequest.setRevision(revision);

		for (int idx = 0; idx < regions.length; ++idx)
		{
			int region = regions[idx];
			int[] keys = xteaKeys[idx];

			if (sentRegions.contains(region))
			{
				continue;
			}

			sentRegions.add(region);

			log.debug("Region {} keys {}, {}, {}, {}", region, keys[0], keys[1], keys[2], keys[3]);

			XteaKey xteaKey = new XteaKey();
			xteaKey.setRegion(region);
			xteaKey.setKeys(keys);
			xteaRequest.addKey(xteaKey);
		}

		if (xteaRequest.getKeys().isEmpty())
		{
			return;
		}

		regionApi.sendXtea(GSON.toJson(xteaRequest))
			.subscribeOn(Schedulers.io())
			.take(1)
			.subscribe();
	}

	public void sendRegion()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		CollisionData[] col = client.getCollisionMaps();
		if (col == null)
		{
			return;
		}

		List<TileFlag> tileFlags = new ArrayList<>();
		Map<WorldPoint, List<Transport>> transportLinks = buildTransportLinks();
		int plane = client.getPlane();
		CollisionData data = col[plane];
		if (data == null)
		{
			return;
		}

		int[][] flags = data.getFlags();
		for (int x = 0; x < flags.length; x++)
		{
			for (int y = 0; y < flags.length; y++)
			{
				LocalPoint localPoint = LocalPoint.fromScene(x, y);
				WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, localPoint);

				int tileX = worldPoint.getX();
				int tileY = worldPoint.getY();

				int flag = flags[x][y];

				// Stop if we reach any tiles which dont have collision data loaded
				// Usually occurs for tiles which are loaded in the 104x104 scene, but are outside the region
				if (flag == 0xFFFFFF)
				{
					continue;
				}

				int regionId = ((tileX >> 6) << 8) | (tileY >> 6);

				// Set the full block flag in case tiles are null (ex. on upper levels)
				TileFlag tileFlag = new TileFlag(tileX, tileY, plane, CollisionDataFlag.BLOCK_MOVEMENT_FULL, regionId);
				Tile tile = Reachable.getAt(client, x + client.getBaseX(), y + client.getBaseY(), plane);
				if (tile == null)
				{
					tileFlags.add(tileFlag);
					continue;
				}

				tileFlag.setFlag(flag);
				WorldPoint tileCoords = tile.getWorldLocation();

				// Check if we are blocked by objects
				// We don't need to parse west/south because they're checked by parsing adjacent tiles for north/east
				// We also skip the current tile if an adjacent tile does not have their flags loaded
				WorldPoint northernTile = tileCoords.dy(1);
				if (Reachable.getCollisionFlag(client, northernTile) == 0xFFFFFF)
				{
					continue;
				}

				if (Reachable.isObstacle(client, northernTile)
					&& !Reachable.isWalled(Direction.NORTH, tileFlag.getFlag())
				)
				{
					tileFlag.setFlag(tileFlag.getFlag() + CollisionDataFlag.BLOCK_MOVEMENT_NORTH);
				}

				WorldPoint easternTile = tileCoords.dx(1);
				if (Reachable.getCollisionFlag(client, easternTile) == 0xFFFFFF)
				{
					continue;
				}

				if (Reachable.isObstacle(client, easternTile)
					&& !Reachable.isWalled(Direction.EAST, tileFlag.getFlag())
				)
				{
					tileFlag.setFlag(tileFlag.getFlag() + CollisionDataFlag.BLOCK_MOVEMENT_EAST);
				}

				List<Transport> transports = transportLinks.get(tileCoords);
				if (plane == client.getPlane())
				{
					for (Direction direction : Direction.values())
					{
						switch (direction)
						{
							case NORTH:
								if ((Reachable.hasDoor(client, tile, direction) || Reachable.hasDoor(client, northernTile, Direction.SOUTH))
									&& notTransport(transports, tileCoords, northernTile))
								{
									tileFlag.setFlag(tileFlag.getFlag() - CollisionDataFlag.BLOCK_MOVEMENT_NORTH);
								}

								break;
							case EAST:
								if ((Reachable.hasDoor(client, tile, direction) || Reachable.hasDoor(client, easternTile, Direction.WEST))
									&& notTransport(transports, tileCoords, easternTile))
								{
									tileFlag.setFlag(tileFlag.getFlag() - CollisionDataFlag.BLOCK_MOVEMENT_EAST);
								}

								break;
						}
					}
				}

				tileFlags.add(tileFlag);
			}
		}

		regionApi.sendCollisionMap(GSON.toJson(tileFlags))
			.subscribeOn(Schedulers.io())
			.take(1)
			.subscribe();
	}

	public static Map<WorldPoint, List<Transport>> buildTransportLinks()
	{
		Map<WorldPoint, List<Transport>> out = new HashMap<>();
		for (Transport transport : TransportLoader.buildTransports())
		{
			out.computeIfAbsent(transport.getSource(), x -> new ArrayList<>()).add(transport);
		}

		return out;
	}

	public boolean notTransport(List<Transport> transports, WorldPoint from, WorldPoint to)
	{
		if (transports == null)
		{
			return true;
		}

		return transports.stream().noneMatch(t -> t.getSource().equals(from) && t.getDestination().equals(to));
	}
}