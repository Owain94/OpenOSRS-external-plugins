package com.owain.chinmanager.utils;

import com.google.inject.Inject;
import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ui.plugins.options.OptionsConfig;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;

@Slf4j
public class Hopper
{
	private static final int DISPLAY_SWITCHER_MAX_ATTEMPTS = 3;

	private final ChinManager chinManager;
	private final Client client;
	private final ClientThread clientThread;
	private final ChatMessageManager chatMessageManager;
	private final WorldService worldService;
	private final OptionsConfig optionsConfig;

	private net.runelite.api.World quickHopTargetWorld;
	private int displaySwitcherAttempts = 0;

	@Inject
	public Hopper(
		ChinManager chinManager,
		Client client,
		ClientThread clientThread,
		ChatMessageManager chatMessageManager,
		WorldService worldService,
		OptionsConfig optionsConfig
	)
	{
		this.chinManager = chinManager;
		this.client = client;
		this.clientThread = clientThread;
		this.chatMessageManager = chatMessageManager;
		this.worldService = worldService;
		this.optionsConfig = optionsConfig;
	}

	@Subscribe(priority = -98)
	public void onGameTick(GameTick gameTick)
	{
		if (quickHopTargetWorld == null)
		{
			return;
		}

		if (client.getWidget(WidgetInfo.WORLD_SWITCHER_LIST) == null)
		{
			client.openWorldHopper();

			if (++displaySwitcherAttempts >= DISPLAY_SWITCHER_MAX_ATTEMPTS)
			{
				String chatMessage = new ChatMessageBuilder()
					.append(ChatColorType.NORMAL)
					.append("Failed to quick-hop after ")
					.append(ChatColorType.HIGHLIGHT)
					.append(Integer.toString(displaySwitcherAttempts))
					.append(ChatColorType.NORMAL)
					.append(" attempts.")
					.build();

				chatMessageManager
					.queue(QueuedMessage.builder()
						.type(ChatMessageType.CONSOLE)
						.runeLiteFormattedMessage(chatMessage)
						.build());

				resetQuickHopper();
			}
		}
		else
		{
			client.hopToWorld(quickHopTargetWorld);
			resetQuickHopper();
		}
	}

	@Subscribe(priority = -98)
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			if (!chinManager.getActiveSortedPlugins().isEmpty())
			{
				if (optionsConfig.hopAfterBreak() && (optionsConfig.american() || optionsConfig.unitedKingdom() || optionsConfig.german() || optionsConfig.australian()))
				{
					hop();
				}
			}
		}
	}

	private void resetQuickHopper()
	{
		displaySwitcherAttempts = 0;
		quickHopTargetWorld = null;
	}

	public void hop()
	{
		clientThread.invoke(() -> {
			WorldResult worldResult = worldService.getWorlds();
			if (worldResult == null)
			{
				return;
			}

			World currentWorld = worldResult.findWorld(client.getWorld());

			if (currentWorld == null)
			{
				return;
			}

			EnumSet<WorldType> currentWorldTypes = currentWorld.getTypes().clone();

			currentWorldTypes.remove(WorldType.PVP);
			currentWorldTypes.remove(WorldType.HIGH_RISK);
			currentWorldTypes.remove(WorldType.BOUNTY);
			currentWorldTypes.remove(WorldType.SKILL_TOTAL);
			currentWorldTypes.remove(WorldType.LAST_MAN_STANDING);

			List<World> worlds = worldResult.getWorlds();

			int totalLevel = client.getTotalLevel();

			World world;
			do
			{
				world = findWorld(worlds, currentWorldTypes, totalLevel);
			}
			while (world == null || world == currentWorld);

			hop(world.getId());
		});
	}

	private void hop(int worldId)
	{
		WorldResult worldResult = worldService.getWorlds();
		if (worldResult == null)
		{
			return;
		}

		// Don't try to hop if the world doesn't exist
		World world = worldResult.findWorld(worldId);
		if (world == null)
		{
			return;
		}

		final net.runelite.api.World rsWorld = client.createWorld();
		rsWorld.setActivity(world.getActivity());
		rsWorld.setAddress(world.getAddress());
		rsWorld.setId(world.getId());
		rsWorld.setPlayerCount(world.getPlayers());
		rsWorld.setLocation(world.getLocation());
		rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));

		if (client.getGameState() == GameState.LOGIN_SCREEN)
		{
			client.changeWorld(rsWorld);
			return;
		}

		String chatMessage = new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append("Hopping.... New world: ")
			.append(ChatColorType.HIGHLIGHT)
			.append(Integer.toString(world.getId()))
			.append(ChatColorType.NORMAL)
			.append("..")
			.build();

		chatMessageManager
			.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMessage)
				.build());

		quickHopTargetWorld = rsWorld;
		displaySwitcherAttempts = 0;
	}

	private World findWorld(List<World> worlds, EnumSet<WorldType> currentWorldTypes, int totalLevel)
	{
		World world = worlds.get(new Random().nextInt(worlds.size()));

		EnumSet<WorldType> types = world.getTypes().clone();

		types.remove(WorldType.LAST_MAN_STANDING);

		if (types.contains(WorldType.SKILL_TOTAL))
		{
			try
			{
				int totalRequirement = Integer.parseInt(world.getActivity().substring(0, world.getActivity().indexOf(" ")));

				if (totalLevel >= totalRequirement)
				{
					types.remove(WorldType.SKILL_TOTAL);
				}
			}
			catch (NumberFormatException ex)
			{
				log.warn("Failed to parse total level requirement for target world", ex);
			}
		}

		if (currentWorldTypes.equals(types))
		{
			int worldLocation = world.getLocation();

			if (!optionsConfig.hopAfterBreak())
			{
				WorldResult worldResult = worldService.getWorlds();
				if (worldResult != null)
				{
					World currentWorld = worldResult.findWorld(client.getWorld());
					if (currentWorld.getLocation() == worldLocation)
					{
						return world;
					}
				}
			}
			else if (optionsConfig.american() && worldLocation == 0)
			{
				return world;
			}
			else if (optionsConfig.unitedKingdom() && worldLocation == 1)
			{
				return world;
			}
			else if (optionsConfig.australian() && worldLocation == 3)
			{
				return world;
			}
			else if (optionsConfig.german() && worldLocation == 7)
			{
				return world;
			}
		}

		return null;
	}
}
