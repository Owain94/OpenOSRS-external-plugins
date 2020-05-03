package com.owain.autohop;

import com.google.inject.Provides;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.util.PvPUtil;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Chin auto hop",
	description = "Hop away from people in the wilderness",
	type = PluginType.MISCELLANEOUS
)
@Slf4j
public class AutoHopPlugin extends Plugin
{
	private static final int DISPLAY_SWITCHER_MAX_ATTEMPTS = 3;

	@Inject
	private Client client;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private WorldService worldService;

	@Inject
	private AutoHopConfig config;

	private net.runelite.api.World quickHopTargetWorld;
	private int displaySwitcherAttempts = 0;

	@Provides
	AutoHopConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AutoHopConfig.class);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		final Player local = client.getLocalPlayer();

		if (event.getGameState() != GameState.LOGGED_IN || local == null)
		{
			return;
		}


		for (Player player : client.getPlayers())
		{
			if (player == null ||
				player.equals(local) ||
				!PvPUtil.isAttackable(client, player))
			{
				continue;
			}

			if (config.alwaysHop())
			{
				shouldHop(player);
			}
			else if (config.underHop() && local.getWorldLocation() == player.getWorldLocation())
			{
				shouldHop(player);
			}
			else if (config.skulledHop() && player.getSkullIcon() != null)
			{
				shouldHop(player);
			}
		}
	}


	@Subscribe
	private void onPlayerSpawned(PlayerSpawned event)
	{
		final Player local = client.getLocalPlayer();
		final Player player = event.getPlayer();

		if (local == null ||
			player == null ||
			player.equals(local) ||
			!PvPUtil.isAttackable(client, player))
		{
			return;
		}

		if (config.alwaysHop())
		{
			shouldHop(player);
		}
		else if (config.underHop() && local.getWorldLocation() == player.getWorldLocation())
		{
			shouldHop(player);
		}
		else if (config.skulledHop() && player.getSkullIcon() != null)
		{
			shouldHop(player);
		}
	}

	private void shouldHop(Player player)
	{
		if ((config.friends() && player.isFriend()) ||
			(config.clanmember() && player.isClanMember()))
		{
			return;
		}

		hop();
	}

	private void hop()
	{
		WorldResult worldResult = worldService.getWorlds();
		if (worldResult == null || client.getGameState() != GameState.LOGGED_IN)
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
		currentWorldTypes.remove(WorldType.DEADMAN_TOURNAMENT);

		List<World> worlds = worldResult.getWorlds();

		int totalLevel = client.getTotalLevel();

		World world;
		do
		{
			world = worlds.get(new Random().nextInt(worlds.size()));

			EnumSet<WorldType> types = world.getTypes().clone();

			types.remove(WorldType.BOUNTY);
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

				if (config.american() && worldLocation == 0)
				{
					break;
				}
				else if (config.unitedkingdom() && worldLocation == 1)
				{
					break;
				}
				else if (config.australia() && worldLocation == 3)
				{
					break;
				}
				else if (config.germany() && worldLocation == 7)
				{
					break;
				}
			}
		}
		while (world != currentWorld);

		if (world == currentWorld)
		{
			String chatMessage = new ChatMessageBuilder()
				.append(ChatColorType.NORMAL)
				.append("Couldn't find a world to quick-hop to.")
				.build();

			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMessage)
				.build());
		}
		else
		{
			hop(world.getId());
		}
	}

	private void hop(int worldId)
	{
		WorldResult worldResult = worldService.getWorlds();
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
			.append("Hopping away from a player in the wilderness. New world: ")
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

	@Subscribe
	private void onGameTick(GameTick event)
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

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		if (event.getMessage().equals("Please finish what you're doing before using the World Switcher."))
		{
			resetQuickHopper();
		}
	}

	private void resetQuickHopper()
	{
		displaySwitcherAttempts = 0;
		quickHopTargetWorld = null;
	}
}
