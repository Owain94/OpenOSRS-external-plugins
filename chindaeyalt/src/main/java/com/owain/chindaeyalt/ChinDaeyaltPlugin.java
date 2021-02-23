package com.owain.chindaeyalt;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.owain.chinbreakhandler.ChinBreakHandler;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.ObjectID;
import net.runelite.api.Point;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Chin daeyalt",
	description = "Daeyalt essence miner",
	enabledByDefault = false
)
@Slf4j
public class ChinDaeyaltPlugin extends Plugin
{
	static final String CONFIG_GROUP = "chindaeyalt";
	static final String PLUGIN_NAME = "Chin daeyalt";

	static final int DAEYALT_MINE_REGION = 14744;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ChinDaeyaltOverlay overlay;

	@Inject
	private ChinDaeyaltConfig config;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ChinBreakHandler chinBreakHandler;

	@Getter(AccessLevel.PACKAGE)
	private boolean enabled;

	private ExecutorService executorService;
	private Random random;

	private boolean clicked;

	@Provides
	ChinDaeyaltConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(ChinDaeyaltConfig.class);
	}

	protected void startUp()
	{
		executorService = Executors.newSingleThreadExecutor();
		random = new Random();

		stopState();

		overlayManager.add(overlay);
		keyManager.registerKeyListener(hotkeyListener);
		chinBreakHandler.registerPlugin(this);
	}

	protected void shutDown()
	{
		executorService.shutdown();

		overlayManager.remove(overlay);
		keyManager.unregisterKeyListener(hotkeyListener);

		stopState();
		chinBreakHandler.unregisterPlugin(this);
	}

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.hotkeyToggle())
	{
		@Override
		public void hotkeyPressed()
		{
			executorService.submit(() ->
			{
				if (enabled)
				{
					stopState();
				}
				else
				{
					startState();
				}
			});
		}
	};

	@SuppressWarnings("unused")
	@Subscribe
	private void onGameObjectSpawned(final GameObjectSpawned gameObjectSpawned)
	{
		if (!enabled || chinBreakHandler.isBreakActive(this))
		{
			return;
		}

		final GameObject gameObject = gameObjectSpawned.getGameObject();
		final int eventObjectId = gameObject.getId();

		if (eventObjectId == ObjectID.DAEYALT_ESSENCE_39095)
		{
			leftClickRandom();
		}
	}

	@SuppressWarnings("unused")
	@Subscribe
	private void onGameTick(final GameTick event)
	{
		if (!enabled || chinBreakHandler.isBreakActive(this))
		{
			return;
		}

		if (chinBreakHandler.shouldBreak(this))
		{
			chinBreakHandler.startBreak(this);
		}
	}

	@SuppressWarnings("unused")
	@Subscribe
	private void onMenuOptionClicked(final MenuOptionClicked menuOptionClicked)
	{
		if (!enabled || !clicked)
		{
			return;
		}

		clicked = false;

		GameObject daeyaltEssence = new GameObjectQuery()
			.idEquals(ObjectID.DAEYALT_ESSENCE_39095)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (daeyaltEssence == null)
		{
			return;
		}

		menuOptionClicked.setMenuOption("Mine");
		menuOptionClicked.setMenuTarget("<col=ffff>Daeyalt Essence");
		menuOptionClicked.setId(ObjectID.DAEYALT_ESSENCE_39095);
		menuOptionClicked.setMenuAction(MenuAction.GAME_OBJECT_FIRST_OPTION);
		menuOptionClicked.setActionParam(daeyaltEssence.getSceneMinLocation().getX());
		menuOptionClicked.setWidgetId(daeyaltEssence.getSceneMinLocation().getY());
	}

	boolean isInRegion()
	{
		if (client.getLocalPlayer() == null)
		{
			return false;
		}

		return client.getLocalPlayer().getWorldLocation().getRegionID() == DAEYALT_MINE_REGION;
	}

	private void startState()
	{
		chinBreakHandler.startPlugin(this);
		enabled = true;

		leftClickRandom();
	}

	private void stopState()
	{
		chinBreakHandler.stopPlugin(this);
		enabled = false;
	}

	private static int getRandomIntBetweenRange(int min, int max)
	{
		return (int) ((Math.random() * ((max - min) + 1)) + min);
	}

	private static double clamp(double val, int min, int max)
	{
		return Math.max(min, Math.min(max, val));
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

	private static void mouseEvent(int id, Point point, Boolean move, Client client)
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

	private static void click(Point p, Client client)
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
			mouseEvent(MouseEvent.MOUSE_FIRST, point, false, client);

			return;
		}

		mouseEvent(MouseEvent.MOUSE_PRESSED, p, false, client);
		mouseEvent(MouseEvent.MOUSE_RELEASED, p, false, client);
		mouseEvent(MouseEvent.MOUSE_FIRST, p, false, client);
	}

	private void leftClickRandom()
	{
		if (!enabled)
		{
			return;
		}

		clicked = true;

		executorService.submit(() ->
		{
			try
			{
				Canvas canvas = client.getCanvas();

				Point point = new Point(
					getRandomIntBetweenRange(0, canvas.getWidth() / 2),
					getRandomIntBetweenRange(0, canvas.getHeight() / 2)
				);

				randomDelay(config.weightedDistribution(), config.minimumDelay(), config.maximumDelay(), config.target(), config.deviation(), random);
				click(point, client);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		});
	}
}
