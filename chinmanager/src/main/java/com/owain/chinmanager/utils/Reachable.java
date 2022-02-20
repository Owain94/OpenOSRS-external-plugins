package com.owain.chinmanager.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.GameObject;
import net.runelite.api.Locatable;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.WallObject;
import net.runelite.api.coords.Direction;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

public class Reachable
{
	private static final int MAX_ATTEMPTED_TILES = 1000;

	public static boolean check(int flag, int checkFlag)
	{
		return (flag & checkFlag) != 0;
	}

	public static boolean isObstacle(int endFlag)
	{
		return check(endFlag, 0x100 | 0x20000 | 0x200000 | 0x1000000);
	}

	public static boolean isObstacle(Client client, WorldPoint worldPoint)
	{
		return isObstacle(getCollisionFlag(client, worldPoint));
	}

	public static int getCollisionFlag(Client client, WorldPoint point)
	{
		CollisionData[] collisionMaps = client.getCollisionMaps();
		if (collisionMaps == null)
		{
			return 0xFFFFFF;
		}

		CollisionData collisionData = collisionMaps[client.getPlane()];
		if (collisionData == null)
		{
			return 0xFFFFFF;
		}

		LocalPoint localPoint = LocalPoint.fromWorld(client, point);
		if (localPoint == null)
		{
			return 0xFFFFFF;
		}

		return collisionData.getFlags()[localPoint.getSceneX()][localPoint.getSceneY()];
	}

	public static boolean isWalled(Direction direction, int startFlag)
	{
		switch (direction)
		{
			case NORTH:
				return check(startFlag, 0x2);
			case EAST:
				return check(startFlag, 0x8);
			case SOUTH:
				return check(startFlag, 0x20);
			case WEST:
				return check(startFlag, 0x80);
		}

		return false;
	}

	public static boolean isWalled(Client client, WorldPoint source, WorldPoint destination)
	{
		return isWalled(getAt(client, source), getAt(client, destination));
	}

	public static boolean isWalled(Tile source, Tile destination)
	{
		WallObject wall = source.getWallObject();
		if (wall == null)
		{
			return false;
		}

		WorldPoint a = source.getWorldLocation();
		WorldPoint b = destination.getWorldLocation();

		switch (wall.getOrientationA())
		{
			case 1:
				return a.dx(-1).equals(b) || a.dx(-1).dy(1).equals(b) || a.dx(-1).dy(-1).equals(b);
			case 2:
				return a.dy(1).equals(b) || a.dx(-1).dy(1).equals(b) || a.dx(1).dy(1).equals(b);
			case 4:
				return a.dx(1).equals(b) || a.dx(1).dy(1).equals(b) || a.dx(1).dy(-1).equals(b);
			case 8:
				return a.dy(-1).equals(b) || a.dx(-1).dy(-1).equals(b) || a.dx(-1).dy(1).equals(b);
		}

		return false;
	}

	public static boolean hasDoor(Client client, WorldPoint source, Direction direction)
	{
		Tile tile = getAt(client, source);
		if (tile == null)
		{
			return false;
		}

		return hasDoor(client, tile, direction);
	}

	public static boolean hasDoor(Client client, Tile source, Direction direction)
	{
		WallObject wall = source.getWallObject();
		if (wall == null)
		{
			return false;
		}

		ObjectComposition objectComposition = getObjectDefinition(client, wall.getId());

		if (objectComposition == null)
		{
			return false;
		}

		List<String> actions = Arrays.asList(objectComposition.getActions());

		return isWalled(direction, getCollisionFlag(client, source.getWorldLocation())) && (actions.contains("Open") || actions.contains("Close"));
	}

	public static boolean isDoored(Client client, Tile source, Tile destination)
	{
		WallObject wall = source.getWallObject();
		if (wall == null)
		{
			return false;
		}

		ObjectComposition objectComposition = getObjectDefinition(client, wall.getId());

		if (objectComposition == null)
		{
			return false;
		}

		return isWalled(source, destination) && Arrays.asList(objectComposition.getActions()).contains("Open");
	}

	public static boolean canWalk(Direction direction, int startFlag, int endFlag)
	{
		if (isObstacle(endFlag))
		{
			return false;
		}

		return !isWalled(direction, startFlag);
	}

	public static WorldPoint getNeighbour(Direction direction, WorldPoint source)
	{
		switch (direction)
		{
			case NORTH:
				return source.dy(1);
			case EAST:
				return source.dx(1);
			case SOUTH:
				return source.dy(-1);
			case WEST:
				return source.dx(-1);
		}

		return source;
	}

	public static List<WorldPoint> getNeighbours(Client client, WorldPoint current, Locatable targetObject)
	{
		List<WorldPoint> out = new ArrayList<>();
		for (Direction dir : Direction.values())
		{
			WorldPoint neighbour = getNeighbour(dir, current);
			if (!neighbour.isInScene(client))
			{
				continue;
			}

			if (targetObject != null)
			{
				boolean containsPoint;
				if (targetObject instanceof GameObject)
				{
					int width = ((GameObject) targetObject).sizeX();
					int height = ((GameObject) targetObject).sizeY();

					WorldPoint objectWorldPoint;
					objectWorldPoint = targetObject.getWorldLocation();

					if (width == 3 || height == 3)
					{
						objectWorldPoint = new WorldPoint(width == 3 ? objectWorldPoint.getX() - 1 : objectWorldPoint.getX(), height == 3 ? objectWorldPoint.getY() - 1 : objectWorldPoint.getY(), objectWorldPoint.getPlane());
					}

					List<WorldPoint> area = new WorldArea(objectWorldPoint.getX() - 1, objectWorldPoint.getY() - 1, width + 2, height + 2, objectWorldPoint.getPlane()).toWorldPointList();
					containsPoint = area.contains(neighbour);
				}
				else
				{
					containsPoint = targetObject.getWorldLocation().equals(neighbour);
				}

				if (containsPoint
					&& (!isWalled(dir, getCollisionFlag(client, current)) || targetObject instanceof WallObject))
				{
					out.add(neighbour);
					continue;
				}
			}

			if (!canWalk(dir, getCollisionFlag(client, current), getCollisionFlag(client, neighbour)))
			{
				continue;
			}

			out.add(neighbour);
		}

		return out;
	}

	public static List<WorldPoint> getVisitedTiles(Client client, WorldPoint destination, Locatable targetObject)
	{
		Player local = client.getLocalPlayer();
		// Don't check if too far away
		if (local == null || destination.distanceTo(local.getWorldLocation()) > 35)
		{
			return Collections.emptyList();
		}

		List<WorldPoint> visitedTiles = new ArrayList<>();
		LinkedList<WorldPoint> queue = new LinkedList<>();

		if (local.getWorldLocation().getPlane() != destination.getPlane())
		{
			return visitedTiles;
		}

		queue.add(local.getWorldLocation());

		while (!queue.isEmpty())
		{
			// Stop if too many attempts, for performance
			if (visitedTiles.size() > MAX_ATTEMPTED_TILES)
			{
				return visitedTiles;
			}

			WorldPoint current = queue.pop();
			visitedTiles.add(current);

			if (current.equals(destination))
			{
				return visitedTiles;
			}

			List<WorldPoint> neighbours = getNeighbours(client, current, targetObject)
				.stream().filter(x -> !visitedTiles.contains(x) && !queue.contains(x))
				.collect(Collectors.toList());
			queue.addAll(neighbours);
		}

		return visitedTiles;
	}

	public static boolean isInteractable(Client client, Locatable locatable)
	{
		return getVisitedTiles(client, locatable.getWorldLocation(), locatable).contains(locatable.getWorldLocation());
	}

	public static boolean isWalkable(Client client, WorldPoint worldPoint)
	{
		return getVisitedTiles(client, worldPoint, null).contains(worldPoint);
	}

	public static Tile getAt(Client client, WorldPoint worldPoint)
	{
		return getAt(client, worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane());
	}

	public static Tile getAt(Client client, int worldX, int worldY, int plane)
	{
		if (!WorldPoint.isInScene(client, worldX, worldY))
		{
			return null;
		}

		int x = worldX - client.getBaseX();
		int y = worldY - client.getBaseY();

		return client.getScene().getTiles()[plane][x][y];
	}

	public static ObjectComposition getObjectDefinition(Client client, int id)
	{
		ObjectComposition objectDefinition = client.getObjectDefinition(id);

		if (objectDefinition == null)
		{
			return null;
		}

		if (objectDefinition.getImpostorIds() != null && objectDefinition.getImpostor() != null)
		{
			return objectDefinition.getImpostor();
		}

		return objectDefinition;
	}
}