/*
 * Copyright (c) 2020, Runemoro <https://github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.owain.automation.pathfinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.coords.WorldPoint;

public class PathFinder
{
	private final CollisionMap map;
	private final Node start;
	private final WorldPoint target;
	private final List<Node> boundary = new LinkedList<>();
	private final Set<WorldPoint> visited = new HashSet<>();
	private final Map<WorldPoint, List<WorldPoint>> transports;
	private Node nearest;

	public PathFinder(CollisionMap map, Map<WorldPoint, List<WorldPoint>> transports, WorldPoint start, WorldPoint target)
	{
		this.map = map;
		this.transports = transports;
		this.target = target;
		this.start = new Node(start, null);
		nearest = null;
	}

	public List<WorldPoint> find()
	{
		boundary.add(start);

		int bestDistance = Integer.MAX_VALUE;

		while (!boundary.isEmpty())
		{
			Node node = boundary.remove(0);

			if (node.position.equals(target))
			{
				return node.path();
			}

			int distance = Math.max(Math.abs(node.position.getX() - target.getX()), Math.abs(node.position.getY() - target.getY()));
			if (nearest == null || distance < bestDistance)
			{
				nearest = node;
				bestDistance = distance;
			}

			addNeighbors(node);
		}

		if (nearest != null)
		{
			return nearest.path();
		}

		return null;
	}

	private void addNeighbors(Node node)
	{
		if (map.w(node.position.getX(), node.position.getY(), node.position.getPlane()))
		{
			addNeighbor(node, new WorldPoint(node.position.getX() - 1, node.position.getY(), node.position.getPlane()));
		}

		if (map.e(node.position.getX(), node.position.getY(), node.position.getPlane()))
		{
			addNeighbor(node, new WorldPoint(node.position.getX() + 1, node.position.getY(), node.position.getPlane()));
		}

		if (map.s(node.position.getX(), node.position.getY(), node.position.getPlane()))
		{
			addNeighbor(node, new WorldPoint(node.position.getX(), node.position.getY() - 1, node.position.getPlane()));
		}

		if (map.n(node.position.getX(), node.position.getY(), node.position.getPlane()))
		{
			addNeighbor(node, new WorldPoint(node.position.getX(), node.position.getY() + 1, node.position.getPlane()));
		}

		if (map.sw(node.position.getX(), node.position.getY(), node.position.getPlane()))
		{
			addNeighbor(node, new WorldPoint(node.position.getX() - 1, node.position.getY() - 1, node.position.getPlane()));
		}

		if (map.se(node.position.getX(), node.position.getY(), node.position.getPlane()))
		{
			addNeighbor(node, new WorldPoint(node.position.getX() + 1, node.position.getY() - 1, node.position.getPlane()));
		}

		if (map.nw(node.position.getX(), node.position.getY(), node.position.getPlane()))
		{
			addNeighbor(node, new WorldPoint(node.position.getX() - 1, node.position.getY() + 1, node.position.getPlane()));
		}

		if (map.ne(node.position.getX(), node.position.getY(), node.position.getPlane()))
		{
			addNeighbor(node, new WorldPoint(node.position.getX() + 1, node.position.getY() + 1, node.position.getPlane()));
		}

		for (WorldPoint transport : transports.getOrDefault(node.position, new ArrayList<>()))
		{
			addNeighbor(node, transport);
		}
	}

	public List<WorldPoint> currentBest()
	{
		return nearest == null ? null : nearest.path();
	}

	private void addNeighbor(Node node, WorldPoint neighbor)
	{
		if (!visited.add(neighbor))
		{
			return;
		}

		boundary.add(new Node(neighbor, node));
	}

	private static class Node
	{
		public final WorldPoint position;
		public final Node previous;

		public Node(WorldPoint position, Node previous)
		{
			this.position = position;
			this.previous = previous;
		}

		public List<WorldPoint> path()
		{
			List<WorldPoint> path = new LinkedList<>();
			Node node = this;

			while (node != null)
			{
				path.add(0, node.position);
				node = node.previous;
			}

			return new ArrayList<>(path);
		}
	}
}
