/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package com.owain.automation;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Point;

public final class Automation
{
	public static void sleep(int sleep) throws InterruptedException
	{
		Thread.sleep(sleep);
	}

	public static void randomDelay(boolean weightedDistribution, int min, int max, int target, int deviation, Random random) throws InterruptedException
	{
		Thread.sleep(randomDelayCalculation(weightedDistribution, min, max, target, deviation, random));
	}

	private static long randomDelayCalculation(boolean weightedDistribution, int min, int max, int target, int deviation, Random random)
	{
		if (weightedDistribution)
		{
			return (long) clamp((-Math.log(Math.abs(random.nextGaussian()))) * deviation + target, min, max);
		}
		else
		{
			return (long) clamp(Math.round(random.nextGaussian() * deviation + target), min, max);
		}
	}

	private static double clamp(double val, int min, int max)
	{
		return Math.max(min, Math.min(max, val));
	}

	public static void move(Rectangle rectangle, Client client)
	{
		assert !client.isClientThread();
		Point point = getClickPoint(rectangle);
		move(point, client);
	}

	public static void click(Rectangle rectangle, Client client)
	{
		assert !client.isClientThread();
		Point point = getClickPoint(rectangle);
		click(point, client);
	}

	public static void move(Point p, Client client)
	{
		assert !client.isClientThread();

		if (client.isStretchedEnabled())
		{
			final Dimension stretched = client.getStretchedDimensions();
			final Dimension real = client.getRealDimensions();
			final double width = (stretched.width / real.getWidth());
			final double height = (stretched.height / real.getHeight());
			final Point point = new Point((int) (p.getX() * width), (int) (p.getY() * height));
			mouseEvent(MouseEvent.MOUSE_ENTERED, point, true, client);
			mouseEvent(MouseEvent.MOUSE_EXITED, point, true, client);
			mouseEvent(MouseEvent.MOUSE_MOVED, point, true, client);
			return;
		}
		mouseEvent(MouseEvent.MOUSE_ENTERED, p, true, client);
		mouseEvent(MouseEvent.MOUSE_EXITED, p, true, client);
		mouseEvent(MouseEvent.MOUSE_MOVED, p, true, client);
	}

	public static void click(Point p, Client client)
	{
		assert !client.isClientThread();

		if (client.isStretchedEnabled())
		{
			final Dimension stretched = client.getStretchedDimensions();
			final Dimension real = client.getRealDimensions();
			final double width = (stretched.width / real.getWidth());
			final double height = (stretched.height / real.getHeight());
			final Point point = new Point((int) (p.getX() * width), (int) (p.getY() * height));

			mouseEvent(MouseEvent.MOUSE_PRESSED, point, false, client);
			mouseEvent(MouseEvent.MOUSE_RELEASED, point, false, client);
			mouseEvent(MouseEvent.MOUSE_CLICKED, point, false, client);

			return;
		}

		mouseEvent(MouseEvent.MOUSE_PRESSED, p, false, client);
		mouseEvent(MouseEvent.MOUSE_RELEASED, p, false, client);
		mouseEvent(MouseEvent.MOUSE_CLICKED, p, false, client);
	}

	public static Point getClickPoint(Rectangle rect)
	{
		final int x = (int) (rect.getX() + getRandomIntBetweenRange((int) rect.getWidth() / 6 * -1, (int) rect.getWidth() / 6) + rect.getWidth() / 2);
		final int y = (int) (rect.getY() + getRandomIntBetweenRange((int) rect.getHeight() / 6 * -1, (int) rect.getHeight() / 6) + rect.getHeight() / 2);

		return new Point(x, y);
	}

	public static int getRandomIntBetweenRange(int min, int max)
	{
		return (int) ((Math.random() * ((max - min) + 1)) + min);
	}

	static void mouseEvent(int id, Point point, Boolean move, Client client)
	{
		MouseEvent e = new MouseEvent(
			client.getCanvas(), id,
			System.currentTimeMillis(),
			0, point.getX(), point.getY(),
			move ? 0 : 1, false, 1
		);

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		client.getCanvas().dispatchEvent(e);
	}

	public static void sendKey(int key, Client client, boolean unicode)
	{
		keyEvent(KeyEvent.KEY_PRESSED, key, client);
		if (unicode)
		{
			keyEvent(KeyEvent.KEY_TYPED, key, client);
		}
		keyEvent(KeyEvent.KEY_RELEASED, key, client);
	}

	static void keyEvent(int id, int key, Client client)
	{
		KeyEvent e = new KeyEvent(
			client.getCanvas(), id, System.currentTimeMillis(),
			0, key, KeyEvent.CHAR_UNDEFINED
		);

		if (key != KeyEvent.VK_ENTER && client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		client.getCanvas().dispatchEvent(e);
	}

	public static void sendKey(char key, Client client)
	{
		keyEvent(KeyEvent.KEY_PRESSED, key, client);
		keyEvent(KeyEvent.KEY_TYPED, key, client);
		keyEvent(KeyEvent.KEY_RELEASED, key, client);
	}

	static void keyEvent(int id, char key, Client client)
	{
		KeyEvent e = new KeyEvent(
			client.getCanvas(), id, System.currentTimeMillis(),
			0, KeyEvent.VK_UNDEFINED, key
		);

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		client.getCanvas().dispatchEvent(e);
	}

	public static void pressKey(int key, Client client)
	{
		keyEvent(KeyEvent.KEY_PRESSED, key, client);
	}

	public static void releaseKey(int key, Client client)
	{
		keyEvent(KeyEvent.KEY_RELEASED, key, client);
	}
}
