package com.owain.chinbreakhandler;

import com.owain.chinbreakhandler.ui.ChinBreakHandlerPanel;
import com.owain.chinbreakhandler.ui.utils.IntRandomNumberGenerator;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Point;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Chin break handler",
	description = "Automatically takes breaks for you (?)"
)
public class ChinBreakHandlerPlugin extends Plugin
{
	public final static String CONFIG_GROUP = "chinbreakhandler";

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ClientThread clientThread;

	@Inject
	@Getter
	private ConfigManager configManager;

	@Inject
	private ChinBreakHandler chinBreakHandler;

	public static String data;

	private NavigationButton navButton;
	private ChinBreakHandlerPanel panel;
	private boolean logout;
	private int delay = -1;

	public final Map<Plugin, Disposable> disposables = new HashMap<>();
	public Disposable activeBreaks;
	public Disposable secondsDisposable;
	public Disposable activeDisposable;
	public Disposable logoutDisposable;

	private ChinBreakHandlerState state = ChinBreakHandlerState.NULL;
	private ExecutorService executorService;

	protected void startUp()
	{
		executorService = Executors.newSingleThreadExecutor();

		panel = injector.getInstance(ChinBreakHandlerPanel.class);

		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "chin_special.png");

		navButton = NavigationButton.builder()
			.tooltip("Chin break handler")
			.icon(icon)
			.priority(4)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		activeBreaks = chinBreakHandler
			.getCurrentActiveBreaksObservable()
			.subscribe(this::breakActivated);

		secondsDisposable = Observable
			.interval(1, TimeUnit.SECONDS)
			.subscribe(this::seconds);

		activeDisposable = chinBreakHandler
			.getActiveObservable()
			.subscribe(
				(plugins) ->
				{
					if (!plugins.isEmpty())
					{
						if (!navButton.isSelected())
						{
							navButton.getOnSelect().run();
						}
					}
				}
			);

		logoutDisposable = chinBreakHandler
			.getlogoutActionObservable()
			.subscribe(
				(plugin) ->
				{
					if (plugin != null)
					{
						logout = true;
						state = ChinBreakHandlerState.LOGOUT;
					}
				}
			);
	}

	protected void shutDown()
	{
		executorService.shutdown();

		clientToolbar.removeNavigation(navButton);

		panel.pluginDisposable.dispose();
		panel.activeDisposable.dispose();
		panel.currentDisposable.dispose();
		panel.startDisposable.dispose();
		panel.configDisposable.dispose();

		for (Disposable disposable : disposables.values())
		{
			if (!disposable.isDisposed())
			{
				disposable.dispose();
			}
		}

		if (activeBreaks != null && !activeBreaks.isDisposed())
		{
			activeBreaks.dispose();
		}

		if (secondsDisposable != null && !secondsDisposable.isDisposed())
		{
			secondsDisposable.dispose();
		}

		if (activeDisposable != null && !activeDisposable.isDisposed())
		{
			activeDisposable.dispose();
		}

		if (logoutDisposable != null && !logoutDisposable.isDisposed())
		{
			logoutDisposable.dispose();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		chinBreakHandler.configChanged.onNext(configChanged);
	}

	public void scheduleBreak(Plugin plugin)
	{
		int from = Integer.parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdfrom")) * 60;
		int to = Integer.parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdto")) * 60;

		int random = new IntRandomNumberGenerator(from, to).nextInt();

		chinBreakHandler.planBreak(plugin, Instant.now().plus(random, ChronoUnit.SECONDS));
	}

	private void breakActivated(Pair<Plugin, Instant> pluginInstantPair)
	{
		Plugin plugin = pluginInstantPair.getKey();

		if (!chinBreakHandler.getPlugins().get(plugin) || Boolean.parseBoolean(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-logout")))
		{
			logout = true;
			state = ChinBreakHandlerState.LOGOUT;
		}
	}

	private void seconds(long ignored)
	{
		Map<Plugin, Instant> activeBreaks = chinBreakHandler.getActiveBreaks();

		if (activeBreaks.isEmpty() || client.getGameState() != GameState.LOGIN_SCREEN)
		{
			return;
		}

		boolean finished = true;

		for (Instant duration : activeBreaks.values())
		{
			if (Instant.now().isBefore(duration))
			{
				finished = false;
			}
		}

		if (finished)
		{
			boolean manual = Boolean.parseBoolean(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, "accountselection"));

			String username = null;
			String password = null;

			if (manual)
			{
				username = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, "accountselection-manual-username");
				password = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, "accountselection-manual-password");
			}
			else
			{
				String account = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, "accountselection-profiles-account");

				if (data == null)
				{
					return;
				}

				Optional<String> accountData = Arrays.stream(data.split("\\n"))
					.filter(s -> s.startsWith(account))
					.findFirst();

				if (accountData.isPresent())
				{
					String[] parts = accountData.get().split(":");
					username = parts[1];
					if (parts.length == 3)
					{
						password = parts[2];
					}
				}
			}

			if (username != null && password != null)
			{
				String finalUsername = username;
				String finalPassword = password;

				clientThread.invoke(() ->
					{
						client.setUsername(finalUsername);
						client.setPassword(finalPassword);

						// client.setGameState(GameState.LOGGING_IN);

						sendKey(KeyEvent.VK_ENTER);
						sendKey(KeyEvent.VK_ENTER);
						sendKey(KeyEvent.VK_ENTER);
					}
				);

			}
		}
	}

	public static String sanitizedName(Plugin plugin)
	{
		return plugin.getName().toLowerCase().replace(" ", "");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			state = ChinBreakHandlerState.LOGIN_SCREEN;
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (state == ChinBreakHandlerState.NULL && logout && delay == 0)
		{
			state = ChinBreakHandlerState.LOGOUT;
		}
		else if (state == ChinBreakHandlerState.LOGIN_SCREEN && !chinBreakHandler.getActiveBreaks().isEmpty())
		{
			logout = false;

			Widget loginScreen = client.getWidget(WidgetInfo.LOGIN_CLICK_TO_PLAY_SCREEN);
			Widget playButtonText = client.getWidget(WidgetID.LOGIN_CLICK_TO_PLAY_GROUP_ID, 87);

			if (playButtonText != null && playButtonText.getText().equals("CLICK HERE TO PLAY"))
			{
				click();
			}
			else if (loginScreen == null)
			{
				state = ChinBreakHandlerState.INVENTORY;
			}
		}
		else if (state == ChinBreakHandlerState.LOGOUT)
		{
			sendKey(KeyEvent.VK_ESCAPE);

			state = ChinBreakHandlerState.LOGOUT_TAB;
		}
		else if (state == ChinBreakHandlerState.LOGOUT_TAB)
		{
			// Logout tab
			client.runScript(915, 10);

			Widget logoutButton = client.getWidget(182, 8);
			Widget logoutDoorButton = client.getWidget(69, 23);

			if (logoutButton != null || logoutDoorButton != null)
			{
				state = ChinBreakHandlerState.LOGOUT_BUTTON;
			}
		}
		else if (state == ChinBreakHandlerState.LOGOUT_BUTTON)
		{
			click();
			delay = new IntRandomNumberGenerator(20, 25).nextInt();
		}
		else if (state == ChinBreakHandlerState.INVENTORY)
		{
			// Inventory
			client.runScript(915, 3);
			state = ChinBreakHandlerState.RESUME;
		}
		else if (state == ChinBreakHandlerState.RESUME)
		{
			for (Plugin plugin : chinBreakHandler.getActiveBreaks().keySet())
			{
				chinBreakHandler.stopBreak(plugin);
			}

			state = ChinBreakHandlerState.NULL;
		}
		else if (!chinBreakHandler.getActiveBreaks().isEmpty())
		{
			Map<Plugin, Instant> activeBreaks = chinBreakHandler.getActiveBreaks();

			if (activeBreaks
				.keySet()
				.stream()
				.anyMatch(e ->
					!Boolean.parseBoolean(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(e) + "-logout"))))
			{
				if (client.getKeyboardIdleTicks() > 14900)
				{
					client.setKeyboardIdleTicks(0);
				}
				if (client.getMouseIdleTicks() > 14900)
				{
					client.setMouseIdleTicks(0);
				}

				boolean finished = true;

				for (Instant duration : activeBreaks.values())
				{
					if (Instant.now().isBefore(duration))
					{
						finished = false;
					}
				}

				if (finished)
				{
					state = ChinBreakHandlerState.INVENTORY;
				}
			}
		}

		if (delay > 0)
		{
			delay--;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (state == ChinBreakHandlerState.LOGIN_SCREEN)
		{
			Widget playButton = client.getWidget(WidgetID.LOGIN_CLICK_TO_PLAY_GROUP_ID, 78);

			if (playButton == null)
			{
				return;
			}

			menuAction(
				menuOptionClicked,
				"Play",
				"",
				1,
				MenuAction.CC_OP,
				-1,
				playButton.getId()
			);

			state = ChinBreakHandlerState.INVENTORY;
		}
		else if (state == ChinBreakHandlerState.LOGOUT_BUTTON)
		{
			Widget logoutButton = client.getWidget(182, 8);
			Widget logoutDoorButton = client.getWidget(69, 23);
			int param1 = -1;

			if (logoutButton != null)
			{
				param1 = logoutButton.getId();
			}
			else if (logoutDoorButton != null)
			{
				param1 = logoutDoorButton.getId();
			}

			if (param1 == -1)
			{
				menuOptionClicked.consume();
				return;
			}

			menuAction(
				menuOptionClicked,
				"Logout",
				"",
				1,
				MenuAction.CC_OP,
				-1,
				param1
			);

			state = ChinBreakHandlerState.NULL;
		}
	}

	private void click()
	{
		executorService.submit(() ->
		{
			Point point = new Point(0, 0);

			mouseEvent(MouseEvent.MOUSE_ENTERED, point);
			mouseEvent(MouseEvent.MOUSE_EXITED, point);
			mouseEvent(MouseEvent.MOUSE_MOVED, point);

			mouseEvent(MouseEvent.MOUSE_PRESSED, point);
			mouseEvent(MouseEvent.MOUSE_RELEASED, point);
			mouseEvent(MouseEvent.MOUSE_CLICKED, point);
		});
	}

	private void mouseEvent(int id, Point point)
	{
		MouseEvent mouseEvent = new MouseEvent(
			client.getCanvas(), id,
			System.currentTimeMillis(),
			0, point.getX(), point.getY(),
			1, false, 1
		);

		client.getCanvas().dispatchEvent(mouseEvent);
	}

	@SuppressWarnings("SameParameterValue")
	private void sendKey(int key)
	{
		keyEvent(KeyEvent.KEY_PRESSED, key);
		keyEvent(KeyEvent.KEY_RELEASED, key);
	}

	private void keyEvent(int id, int key)
	{
		KeyEvent e = new KeyEvent(
			client.getCanvas(), id, System.currentTimeMillis(),
			0, key, KeyEvent.CHAR_UNDEFINED
		);

		client.getCanvas().dispatchEvent(e);
	}

	public boolean isValidBreak(Plugin plugin)
	{
		Map<Plugin, Boolean> plugins = chinBreakHandler.getPlugins();

		if (!plugins.containsKey(plugin))
		{
			return false;
		}

		if (!plugins.get(plugin))
		{
			return true;
		}

		String thresholdfrom = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdfrom");
		String thresholdto = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdto");
		String breakfrom = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakfrom");
		String breakto = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakto");

		return isNumeric(thresholdfrom) &&
			isNumeric(thresholdto) &&
			isNumeric(breakfrom) &&
			isNumeric(breakto) &&
			Integer.parseInt(thresholdfrom) <= Integer.parseInt(thresholdto) &&
			Integer.parseInt(breakfrom) <= Integer.parseInt(breakto);
	}

	public static boolean isNumeric(String strNum)
	{
		if (strNum == null)
		{
			return false;
		}
		try
		{
			Double.parseDouble(strNum);
		}
		catch (NumberFormatException nfe)
		{
			return false;
		}
		return true;
	}

	public void menuAction(MenuOptionClicked menuOptionClicked, String option, String target, int identifier, MenuAction menuAction, int param0, int param1)
	{
		menuOptionClicked.setMenuOption(option);
		menuOptionClicked.setMenuTarget(target);
		menuOptionClicked.setId(identifier);
		menuOptionClicked.setMenuAction(menuAction);
		menuOptionClicked.setActionParam(param0);
		menuOptionClicked.setWidgetId(param1);
	}
}