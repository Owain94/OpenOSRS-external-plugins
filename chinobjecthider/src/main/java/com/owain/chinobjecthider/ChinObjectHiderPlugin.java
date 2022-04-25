package com.owain.chinobjecthider;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.WallObject;
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
import net.runelite.api.events.WallObjectChanged;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Chin object hider",
	description = "Object goes poof",
	enabledByDefault = false
)
@Slf4j
public class ChinObjectHiderPlugin extends Plugin
{
	static final String CONFIG_GROUP = "chinobjecthider";

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChinObjectHiderConfig chinObjectHiderConfig;

	private List<Integer> objectIds = List.of();
	private List<String> objectNames = List.of();

	@Provides
	ChinObjectHiderConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(ChinObjectHiderConfig.class);
	}

	protected void startUp()
	{
		if (!chinObjectHiderConfig.objectIds().equals(""))
		{
			try
			{
				objectIds = Arrays.stream(chinObjectHiderConfig.objectIds().split(","))
					.map(s -> Integer.parseInt(s.trim()))
					.collect(Collectors.toList());
			}
			catch (Exception ex)
			{
				// Don't care
			}
		}

		try
		{
			objectNames = Arrays.stream(chinObjectHiderConfig.objectNames().split(","))
				.map(String::trim)
				.map(String::toLowerCase)
				.collect(Collectors.toList());
		}
		catch (Exception ex)
		{
			// Don't care
		}

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invoke(this::hide);
		}
	}

	protected void shutDown()
	{
		objectIds = List.of();
		objectNames = List.of();

		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.setGameState(GameState.LOADING);
			}
		});
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			hide();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!configChanged.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}

		if (configChanged.getKey().equals("objectIds"))
		{
			if (!chinObjectHiderConfig.objectIds().equals(""))
			{
				try
				{
					objectIds = Arrays.stream(chinObjectHiderConfig.objectIds().split(","))
						.map(s -> Integer.parseInt(s.trim()))
						.collect(Collectors.toList());
				}
				catch (Exception ex)
				{
					log.debug("Config changed ids: ", ex);
				}
			}
		}
		else if (configChanged.getKey().equals("objectNames"))
		{
			try
			{
				objectNames = Arrays.stream(chinObjectHiderConfig.objectNames().split(","))
					.map(String::trim)
					.map(String::toLowerCase)
					.collect(Collectors.toList());
			}
			catch (Exception ex)
			{
				log.debug("Config changed names: ", ex);
			}
		}

		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.setGameState(GameState.LOADING);
			}
		});
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		if (client.getGameState() == GameState.LOGGED_IN && chinObjectHiderConfig.hideChanged())
		{
			clientThread.invokeLater(this::hide);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		if (client.getGameState() == GameState.LOGGED_IN && chinObjectHiderConfig.hideChanged())
		{
			clientThread.invokeLater(this::hide);
		}
	}

	@Subscribe
	public void onGameObjectChanged(GameObjectChanged gameObjectChanged)
	{
		if (client.getGameState() == GameState.LOGGED_IN && chinObjectHiderConfig.hideChanged())
		{
			clientThread.invokeLater(this::hide);
		}
	}


	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned decorativeObjectSpawned)
	{
		if (client.getGameState() == GameState.LOGGED_IN && chinObjectHiderConfig.hideChanged())
		{
			clientThread.invokeLater(this::hide);
		}
	}

	@Subscribe
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned decorativeObjectDespawned)
	{
		if (client.getGameState() == GameState.LOGGED_IN && chinObjectHiderConfig.hideChanged())
		{
			clientThread.invokeLater(this::hide);
		}
	}

	@Subscribe
	public void onDecorativeObjectChanged(DecorativeObjectChanged decorativeObjectChanged)
	{
		if (client.getGameState() == GameState.LOGGED_IN && chinObjectHiderConfig.hideChanged())
		{
			clientThread.invokeLater(this::hide);
		}
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned wallObjectSpawned)
	{
		if (client.getGameState() == GameState.LOGGED_IN && chinObjectHiderConfig.hideChanged())
		{
			clientThread.invokeLater(this::hide);
		}
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned wallObjectDespawned)
	{
		if (client.getGameState() == GameState.LOGGED_IN && chinObjectHiderConfig.hideChanged())
		{
			clientThread.invokeLater(this::hide);
		}
	}

	@Subscribe
	public void onWallObjectChanged(WallObjectChanged wallObjectChanged)
	{
		if (client.getGameState() == GameState.LOGGED_IN && chinObjectHiderConfig.hideChanged())
		{
			clientThread.invokeLater(this::hide);
		}
	}


	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned)
	{
		if (client.getGameState() == GameState.LOGGED_IN && chinObjectHiderConfig.hideChanged())
		{
			clientThread.invokeLater(this::hide);
		}
	}

	@Subscribe
	public void onGroundObjectDespawned(GroundObjectDespawned groundObjectDespawned)
	{
		if (client.getGameState() == GameState.LOGGED_IN && chinObjectHiderConfig.hideChanged())
		{
			clientThread.invokeLater(this::hide);
		}
	}

	@Subscribe
	public void onGroundObjectChanged(GroundObjectChanged groundObjectChanged)
	{
		if (client.getGameState() == GameState.LOGGED_IN && chinObjectHiderConfig.hideChanged())
		{
			clientThread.invokeLater(this::hide);
		}
	}

	private void hide()
	{
		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();

		for (int z = 0; z < 3; ++z)
		{
			for (int x = 0; x < Constants.SCENE_SIZE; ++x)
			{
				for (int y = 0; y < Constants.SCENE_SIZE; ++y)
				{
					Tile tile = tiles[z][x][y];
					if (tile == null)
					{
						continue;
					}

					GameObject[] gameObjects = tile.getGameObjects();
					DecorativeObject decorativeObject = tile.getDecorativeObject();
					WallObject wallObject = tile.getWallObject();
					GroundObject groundObject = tile.getGroundObject();

					for (GameObject gameObject : gameObjects)
					{
						if (gameObject != null)
						{
							if (chinObjectHiderConfig.hideAllGameObjects() || objectIds.contains(gameObject.getId()) ||
								(!objectNames.isEmpty() && objectNames.contains(client.getObjectDefinition(gameObject.getId()).getName().toLowerCase()))
							)
							{
								scene.removeGameObject(gameObject);
							}
						}
					}

					if (decorativeObject != null)
					{
						if (chinObjectHiderConfig.hideAllDecorativeObjects() || objectIds.contains(decorativeObject.getId()) ||
							(!objectNames.isEmpty() && objectNames.contains(client.getObjectDefinition(decorativeObject.getId()).getName().toLowerCase()))
						)
						{
							scene.removeDecorativeObject(client.getPlane(), x, y);
						}
					}

					if (wallObject != null)
					{
						if (chinObjectHiderConfig.hideAllWallObjects() || objectIds.contains(wallObject.getId()) ||
							(!objectNames.isEmpty() && objectNames.contains(client.getObjectDefinition(wallObject.getId()).getName().toLowerCase()))
						)
						{
							scene.removeWallObject(client.getPlane(), x, y);
						}
					}

					if (groundObject != null && chinObjectHiderConfig.hideAllGroundObjects())
					{
						if (chinObjectHiderConfig.hideAllGroundObjects() || objectIds.contains(groundObject.getId()) ||
							(!objectNames.isEmpty() && objectNames.contains(client.getObjectDefinition(groundObject.getId()).getName().toLowerCase()))
						)
						{
							scene.removeGroundObject(client.getPlane(), x, y);
						}
					}
				}
			}
		}
	}
}
