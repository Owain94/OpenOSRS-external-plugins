/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package com.owain.automation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

public class Pathfinding
{
	public static boolean canReachWorldPoint(Client client, WorldPoint p)
	{
		Set<WorldPoint> points = new HashSet<>();
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return false;
		}
		build(client, points, player.getWorldLocation());
		return points.contains(p);
	}

	private static void build(Client client, Set<WorldPoint> points, WorldPoint start)
	{
		Queue<WorldPoint> visit = new LinkedList<>();
		Set<WorldPoint> closed = new HashSet<>();
		Map<WorldPoint, Integer> scores = new HashMap<>();
		scores.put(start, 0);
		visit.add(start);

		while (!visit.isEmpty())
		{
			WorldPoint next = visit.poll();
			closed.add(next);

			LocalPoint localNext = LocalPoint.fromWorld(client, next);
			LocalPoint[] neighbours = neighbours(client, points, localNext);

			for (LocalPoint neighbour : neighbours)
			{
				if (neighbour == null)
				{
					continue;
				}

				WorldPoint nghbWorld = WorldPoint.fromLocal(client, neighbour);

				if (!nghbWorld.equals(next)
					&& !closed.contains(nghbWorld))
				{
					int score = scores.get(next) + 1;

					if (!scores.containsKey(nghbWorld) || scores.get(nghbWorld) > score)
					{
						scores.put(nghbWorld, score);
						visit.add(nghbWorld);
					}
				}
			}
		}
	}

	private static LocalPoint[] neighbours(Client client, Set<WorldPoint> points, LocalPoint point)
	{
		return new LocalPoint[]
			{
				neighbour(client, points, point, Cardinals.NORTH), neighbour(client, points, point, Cardinals.SOUTH),
				neighbour(client, points, point, Cardinals.EAST), neighbour(client, points, point, Cardinals.WEST),
				neighbour(client, points, point, Cardinals.NORTH_EAST), neighbour(client, points, point, Cardinals.NORTH_WEST),
				neighbour(client, points, point, Cardinals.SOUTH_EAST), neighbour(client, points, point, Cardinals.SOUTH_WEST)
			};
	}

	private static LocalPoint neighbour(Client client, Set<WorldPoint> points, LocalPoint point, Cardinals cardinal)
	{
		WorldPoint worldPoint = WorldPoint.fromLocal(client, point);
		WorldArea area = new WorldArea(worldPoint, 1, 1);

		int dx, dy;

		switch (cardinal)
		{
			case NORTH:
				dx = 0;
				dy = 1;
				break;
			case NORTH_EAST:
				dx = 1;
				dy = 1;
				break;
			case NORTH_WEST:
				dx = -1;
				dy = 1;
				break;
			case SOUTH:
				dx = 0;
				dy = -1;
				break;
			case SOUTH_EAST:
				dx = 1;
				dy = -1;
				break;
			case SOUTH_WEST:
				dx = -1;
				dy = -1;
				break;
			case EAST:
				dx = 1;
				dy = 0;
				break;
			case WEST:
				dx = -1;
				dy = 0;
				break;
			default:
				throw new IllegalStateException();
		}

		while (area.canTravelInDirection(client, dx, dy))
		{
			worldPoint = area.toWorldPoint()
				.dx(dx)
				.dy(dy);
			points.add(worldPoint);
			area = new WorldArea(worldPoint, 1, 1);
		}

		return LocalPoint.fromWorld(client, worldPoint);
	}

	private enum Cardinals
	{
		NORTH,
		NORTH_EAST,
		NORTH_WEST,
		SOUTH,
		SOUTH_EAST,
		SOUTH_WEST,
		EAST,
		WEST
	}
}