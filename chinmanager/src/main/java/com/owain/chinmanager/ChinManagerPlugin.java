package com.owain.chinmanager;

import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import static com.owain.chinmanager.ChinManagerState.stateMachine;
import com.owain.chinmanager.api.NotificationsApi;
import com.owain.chinmanager.cookies.PersistentCookieJar;
import com.owain.chinmanager.cookies.cache.SetCookieCache;
import com.owain.chinmanager.cookies.persistence.OpenOSRSCookiePersistor;
import com.owain.chinmanager.magicnumbers.MagicNumberScripts;
import com.owain.chinmanager.magicnumbers.MagicNumberWidgets;
import com.owain.chinmanager.overlay.ManagerClickboxDebugOverlay;
import com.owain.chinmanager.overlay.ManagerClickboxOverlay;
import com.owain.chinmanager.overlay.ManagerTileIndicatorsOverlay;
import com.owain.chinmanager.overlay.ManagerWidgetOverlay;
import com.owain.chinmanager.ui.ChinManagerPanel;
import com.owain.chinmanager.ui.account.OsrsAccountPanel;
import com.owain.chinmanager.ui.account.WebAccountPanel;
import com.owain.chinmanager.ui.gear.Equipment;
import com.owain.chinmanager.ui.plugins.PluginPanel;
import com.owain.chinmanager.ui.plugins.StatusPanel;
import com.owain.chinmanager.ui.plugins.breaks.BreakOptionsPanel;
import com.owain.chinmanager.ui.plugins.options.OptionsConfig;
import com.owain.chinmanager.ui.plugins.status.InfoPanel;
import com.owain.chinmanager.ui.teleports.TeleportsConfig;
import com.owain.chinmanager.utils.Api;
import com.owain.chinmanager.utils.Banking;
import com.owain.chinmanager.utils.Hopper;
import com.owain.chinmanager.utils.IntRandomNumberGenerator;
import static com.owain.chinmanager.utils.Integers.isNumeric;
import com.owain.chinmanager.utils.Notifications;
import com.owain.chinmanager.utils.Overlays;
import com.owain.chinmanager.utils.Plugins;
import static com.owain.chinmanager.utils.Plugins.sanitizedName;
import com.owain.chinmanager.utils.RegionManager;
import com.owain.chinmanager.websockets.WebsocketManager;
import com.owain.chinstatemachine.StateMachine;
import com.owain.chintasks.TaskExecutor;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import static net.runelite.api.ItemID.*;
import net.runelite.api.Locatable;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.VarClientInt;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import static net.runelite.http.api.RuneLiteAPI.GSON;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Chin manager",
	description = "Configure and manage chin plugins"
)
@Slf4j
public class ChinManagerPlugin extends Plugin
{
	public static final String PLUGIN_NAME = "Chin manager";
	public static final String CONFIG_GROUP = "chinmanager";
	public final static String CONFIG_GROUP_BREAKHANDLER = "chinbreakhandler";

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	public static final CompositeDisposable DISPOSABLES = new CompositeDisposable();
	public static final Map<Plugin, CompositeDisposable> PLUGIN_DISPOSABLE_MAP = new HashMap<>();

	public static final List<Integer> DIGSIDE_PENDANTS = List.of(
		ItemID.DIGSITE_PENDANT_1,
		ItemID.DIGSITE_PENDANT_2,
		ItemID.DIGSITE_PENDANT_3,
		ItemID.DIGSITE_PENDANT_4,
		ItemID.DIGSITE_PENDANT_5
	);

	public static final List<Integer> RINGS_OF_DUELING = List.of(
		ItemID.RING_OF_DUELING1,
		ItemID.RING_OF_DUELING2,
		ItemID.RING_OF_DUELING3,
		ItemID.RING_OF_DUELING4,
		ItemID.RING_OF_DUELING5,
		ItemID.RING_OF_DUELING6,
		ItemID.RING_OF_DUELING7,
		ItemID.RING_OF_DUELING8
	);

	public static final List<Integer> GAMES_NECKLACES = List.of(
		ItemID.GAMES_NECKLACE1,
		ItemID.GAMES_NECKLACE2,
		ItemID.GAMES_NECKLACE3,
		ItemID.GAMES_NECKLACE4,
		ItemID.GAMES_NECKLACE5,
		ItemID.GAMES_NECKLACE6,
		ItemID.GAMES_NECKLACE7,
		ItemID.GAMES_NECKLACE8
	);

	public static final List<Integer> COMBAT_BRACELETS = List.of(
		ItemID.COMBAT_BRACELET1,
		ItemID.COMBAT_BRACELET2,
		ItemID.COMBAT_BRACELET3,
		ItemID.COMBAT_BRACELET4,
		ItemID.COMBAT_BRACELET5,
		ItemID.COMBAT_BRACELET6
	);

	public static final List<Integer> SKILLS_NECKLACES = List.of(
		ItemID.SKILLS_NECKLACE1,
		ItemID.SKILLS_NECKLACE2,
		ItemID.SKILLS_NECKLACE3,
		ItemID.SKILLS_NECKLACE4,
		ItemID.SKILLS_NECKLACE5,
		ItemID.SKILLS_NECKLACE6
	);

	public static final List<Integer> RINGS_OF_WEALTH = List.of(
		ItemID.RING_OF_WEALTH_1,
		ItemID.RING_OF_WEALTH_2,
		ItemID.RING_OF_WEALTH_3,
		ItemID.RING_OF_WEALTH_4,
		ItemID.RING_OF_WEALTH_5,
		ItemID.RING_OF_WEALTH_I1,
		ItemID.RING_OF_WEALTH_I2,
		ItemID.RING_OF_WEALTH_I3,
		ItemID.RING_OF_WEALTH_I4,
		ItemID.RING_OF_WEALTH_I5
	);

	public static final List<Integer> AMULETS_OF_GLORY = List.of(
		ItemID.AMULET_OF_ETERNAL_GLORY,
		ItemID.AMULET_OF_GLORY1,
		ItemID.AMULET_OF_GLORY2,
		ItemID.AMULET_OF_GLORY3,
		ItemID.AMULET_OF_GLORY4,
		ItemID.AMULET_OF_GLORY5,
		ItemID.AMULET_OF_GLORY6,
		ItemID.AMULET_OF_GLORY_T1,
		ItemID.AMULET_OF_GLORY_T2,
		ItemID.AMULET_OF_GLORY_T3,
		ItemID.AMULET_OF_GLORY_T4,
		ItemID.AMULET_OF_GLORY_T5,
		ItemID.AMULET_OF_GLORY_T6
	);

	public static final List<Integer> XERICS_TALISMAN = List.of(
		ItemID.XERICS_TALISMAN
	);

	public static final List<Integer> CONSTRUCT_CAPE = List.of(
		ItemID.CONSTRUCT_CAPE,
		ItemID.CONSTRUCT_CAPET
	);

	public static final List<Integer> RUNE_POUCHES = List.of(
		ItemID.RUNE_POUCH,
		ItemID.RUNE_POUCH_L
	);

	public static final List<Integer> GRACEFUL_HOODS = List.of(
		GRACEFUL_HOOD, GRACEFUL_HOOD_11851, GRACEFUL_HOOD_13579, GRACEFUL_HOOD_13580, GRACEFUL_HOOD_13591, GRACEFUL_HOOD_13592,
		GRACEFUL_HOOD_13603, GRACEFUL_HOOD_13604, GRACEFUL_HOOD_13615, GRACEFUL_HOOD_13616, GRACEFUL_HOOD_13627,
		GRACEFUL_HOOD_13628, GRACEFUL_HOOD_13667, GRACEFUL_HOOD_13668, GRACEFUL_HOOD_21061, GRACEFUL_HOOD_21063,
		GRACEFUL_HOOD_24743, GRACEFUL_HOOD_24745, GRACEFUL_HOOD_25069, GRACEFUL_HOOD_25071
	);

	public static final List<Integer> GRACEFUL_TOPS = List.of(
		GRACEFUL_TOP, GRACEFUL_TOP_11855, GRACEFUL_TOP_13583, GRACEFUL_TOP_13584, GRACEFUL_TOP_13595, GRACEFUL_TOP_13596,
		GRACEFUL_TOP_13607, GRACEFUL_TOP_13608, GRACEFUL_TOP_13619, GRACEFUL_TOP_13620, GRACEFUL_TOP_13631,
		GRACEFUL_TOP_13632, GRACEFUL_TOP_13671, GRACEFUL_TOP_13672, GRACEFUL_TOP_21067, GRACEFUL_TOP_21069,
		GRACEFUL_TOP_24749, GRACEFUL_TOP_24751, GRACEFUL_TOP_25075, GRACEFUL_TOP_25077
	);

	public static final List<Integer> GRACEFUL_LEGS = List.of(
		ItemID.GRACEFUL_LEGS, GRACEFUL_LEGS_11857, GRACEFUL_LEGS_13585, GRACEFUL_LEGS_13586, GRACEFUL_LEGS_13597, GRACEFUL_LEGS_13598,
		GRACEFUL_LEGS_13609, GRACEFUL_LEGS_13610, GRACEFUL_LEGS_13621, GRACEFUL_LEGS_13622, GRACEFUL_LEGS_13633,
		GRACEFUL_LEGS_13634, GRACEFUL_LEGS_13673, GRACEFUL_LEGS_13674, GRACEFUL_LEGS_21070, GRACEFUL_LEGS_21072,
		GRACEFUL_LEGS_24752, GRACEFUL_LEGS_24754, GRACEFUL_LEGS_25078, GRACEFUL_LEGS_25080
	);

	public static final List<Integer> GRACEFUL_GLOVES = List.of(
		ItemID.GRACEFUL_GLOVES, GRACEFUL_GLOVES_11859, GRACEFUL_GLOVES_13587, GRACEFUL_GLOVES_13588, GRACEFUL_GLOVES_13599, GRACEFUL_GLOVES_13600,
		GRACEFUL_GLOVES_13611, GRACEFUL_GLOVES_13612, GRACEFUL_GLOVES_13623, GRACEFUL_GLOVES_13624, GRACEFUL_GLOVES_13635,
		GRACEFUL_GLOVES_13636, GRACEFUL_GLOVES_13675, GRACEFUL_GLOVES_13676, GRACEFUL_GLOVES_21073, GRACEFUL_GLOVES_21075,
		GRACEFUL_GLOVES_24755, GRACEFUL_GLOVES_24757, GRACEFUL_GLOVES_25081, GRACEFUL_GLOVES_25083
	);

	public static final List<Integer> GRACEFUL_BOOTS = List.of(
		ItemID.GRACEFUL_BOOTS, GRACEFUL_BOOTS_11861, GRACEFUL_BOOTS_13589, GRACEFUL_BOOTS_13590, GRACEFUL_BOOTS_13601, GRACEFUL_BOOTS_13602,
		GRACEFUL_BOOTS_13613, GRACEFUL_BOOTS_13614, GRACEFUL_BOOTS_13625, GRACEFUL_BOOTS_13626, GRACEFUL_BOOTS_13637,
		GRACEFUL_BOOTS_13638, GRACEFUL_BOOTS_13677, GRACEFUL_BOOTS_13678, GRACEFUL_BOOTS_21076, GRACEFUL_BOOTS_21078,
		GRACEFUL_BOOTS_24758, GRACEFUL_BOOTS_24760, GRACEFUL_BOOTS_25084, GRACEFUL_BOOTS_25086
	);

	public static final List<Integer> GRACEFUL_CAPES = List.of(
		GRACEFUL_CAPE, GRACEFUL_CAPE_11853, GRACEFUL_CAPE_13581, GRACEFUL_CAPE_13582, GRACEFUL_CAPE_13593, GRACEFUL_CAPE_13594,
		GRACEFUL_CAPE_13605, GRACEFUL_CAPE_13606, GRACEFUL_CAPE_13617, GRACEFUL_CAPE_13618, GRACEFUL_CAPE_13629,
		GRACEFUL_CAPE_13630, GRACEFUL_CAPE_13669, GRACEFUL_CAPE_13670, GRACEFUL_CAPE_21064, GRACEFUL_CAPE_21066,
		GRACEFUL_CAPE_24746, GRACEFUL_CAPE_24748, GRACEFUL_CAPE_25072, GRACEFUL_CAPE_25074,
		AGILITY_CAPE, AGILITY_CAPET
	);

	// TODO: Legacy... Move this to Overlays.java
	@Getter(AccessLevel.PUBLIC)
	public static final List<WidgetItem> highlightWidgetItem = new ArrayList<>();

	@Setter(AccessLevel.PUBLIC)
	private static boolean logout;
	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private static boolean shouldSetup;

	@Getter(AccessLevel.PUBLIC)
	private static List<Equipment> equipmentList;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private static String profileData;

	@Getter(AccessLevel.PUBLIC)
	private OkHttpClient okHttpClient;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private ConfigManager configManager;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private EventBus eventBus;

	@Inject
	private ChinManager chinManager;

	@Inject
	private OpenOSRSCookiePersistor openOSRSCookiePersistor;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private Client client;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private ClientThread clientThread;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private TaskExecutor taskExecutor;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private ExecutorService executorService;

	@Inject
	@SuppressWarnings("unused")
	private ChinManagerState chinManagerState;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ManagerClickboxOverlay managerClickboxOverlay;

	@Inject
	private ManagerClickboxDebugOverlay managerClickboxDebugOverlay;

	@Inject
	private ManagerWidgetOverlay managerWidgetOverlay;

	@Inject
	private ManagerTileIndicatorsOverlay managerTileIndicatorsOverlay;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private NotificationsApi notificationsApi;

	@Inject
	private RegionManager regionManager;

	@Inject
	private Api api;

	@Inject
	private Banking banking;

	@Inject
	private Hopper hopper;

	@Inject
	private Notifications notifications;

	private NavigationButton navButton;

	@Getter(AccessLevel.PUBLIC)
	private Random random;

	private int delay = -1;

	@Provides
	public NullConfig getConfig()
	{
		return configManager.getConfig(NullConfig.class);
	}

	@Provides
	public TeleportsConfig getTeleportsConfig()
	{
		return configManager.getConfig(TeleportsConfig.class);
	}

	@Provides
	public OptionsConfig getOptionsConfig()
	{
		return configManager.getConfig(OptionsConfig.class);
	}

	private final Map<Integer, ItemComposition> itemCompositionMap = new HashMap<>();

	@Override
	protected void startUp()
	{
		Api.getObjects().clear();
		Api.getTileItems().clear();

		eventBus.register(api);
		eventBus.register(banking);
		eventBus.register(hopper);
		eventBus.register(regionManager);
		eventBus.register(notifications);

		executorService = Executors.newSingleThreadExecutor();

		random = new Random();

		okHttpClient = new OkHttpClient.Builder()
			.cookieJar(
				new PersistentCookieJar(new SetCookieCache(), openOSRSCookiePersistor)
			)
			.connectTimeout(20, TimeUnit.SECONDS)
			.readTimeout(20, TimeUnit.SECONDS)
			.writeTimeout(20, TimeUnit.SECONDS)
			.build();

		taskExecutor.start();

		ChinManagerPanel panel = injector.getInstance(ChinManagerPanel.class);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "chin.png");

		navButton = NavigationButton.builder()
			.tooltip("Chin manager")
			.icon(icon)
			.priority(3)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		overlayManager.add(managerClickboxOverlay);
		overlayManager.add(managerClickboxDebugOverlay);
		overlayManager.add(managerWidgetOverlay);
		overlayManager.add(managerTileIndicatorsOverlay);

		client.setHideDisconnect(true);

		com.owain.automation.Banking.ITEMS = Set.of();

		DISPOSABLES.addAll(
			stateMachine.getStateObservable().subscribe((state) -> Overlays.resetHighlight()),

			stateMachine.connect().subscribe(),

			chinManager
				.getActiveObservable()
				.subscribe((ignored) -> {
					if (chinManager.getActiveSortedPlugins().isEmpty())
					{
						shouldSetup = true;
						com.owain.automation.Banking.ITEMS = Set.of();
						delay = -1;
						logout = false;
						transition(ChinManagerStates.IDLE);
					}
					else
					{
						com.owain.automation.Banking.ITEMS = chinManager.getActiveSortedPlugins()
							.stream()
							.map((plugin) -> {
								Set<Integer> items = new HashSet<>();

								equipmentList
									.stream()
									.filter((equip) -> equip.getName().equals(sanitizedName(plugin)))
									.findFirst().ifPresent(equipment -> equipment.getEquipment().forEach((item) -> items.add(item.getId())));

								items.addAll(
									Stream.of(Set.of(
												ItemID.TELEPORT_TO_HOUSE,
												ItemID.LAW_RUNE,
												ItemID.AIR_RUNE,
												ItemID.EARTH_RUNE
											),
											DIGSIDE_PENDANTS,
											RINGS_OF_DUELING,
											GAMES_NECKLACES,
											COMBAT_BRACELETS,
											SKILLS_NECKLACES,
											RINGS_OF_WEALTH,
											AMULETS_OF_GLORY,
											XERICS_TALISMAN,
											CONSTRUCT_CAPE,
											RUNE_POUCHES
										)
										.flatMap(Collection::stream)
										.collect(Collectors.toSet())
								);

								if (chinManager.getBankItems().containsKey(plugin))
								{
									items.addAll(chinManager.getBankItems().get(plugin));
								}

								return items;
							})
							.flatMap(Set::stream)
							.collect(Collectors.toSet());
					}
				}),

			chinManager
				.getlogoutActionObservable()
				.subscribe(
					(plugin) ->
					{
						if (plugin != null)
						{
							transition(ChinManagerStates.LOGOUT);
						}
					}
				),

			chinManager
				.hopNowObservable()
				.subscribe(
					(plugin) ->
					{
						if (plugin != null)
						{
							hopper.hop();
						}
					}
				),

			chinManager
				.getActiveBreaksObservable()
				.subscribe((ignored) -> breakActivated()),

			chinManager
				.getCurrentlyActiveObservable()
				.subscribe((ignored) -> currentlyActive()),

			chinManager
				.getBankingObservable()
				.subscribe((plugin) -> {
					if (stateMachine.getState() != ChinManagerState.BANKING && stateMachine.getState() != ChinManagerState.BANK_PIN && stateMachine.getState() != ChinManagerState.BANK_PIN_CONFIRM)
					{
						transition(ChinManagerStates.BANKING);
					}
				}),

			chinManager
				.getTeleportingObservable()
				.subscribe((plugin) -> {
					if (stateMachine.getState() != ChinManagerState.TELEPORTING && stateMachine.getState() != ChinManagerState.BANK_PIN && stateMachine.getState() != ChinManagerState.BANK_PIN_CONFIRM)
					{
						transition(ChinManagerStates.TELEPORTING);
					}
				}),

			Observable
				.interval(1, TimeUnit.SECONDS)
				.subscribe(this::seconds),

			chinManager
				.configChanged
				.observeOn(Schedulers.from(clientThread))
				.subscribe(
					(configChanged) -> {
						if (configChanged.getGroup().equals("mock") && configChanged.getKey().equals("mock"))
						{
							if (chinManager.isCurrentlyActive(this) && stateMachine.getState() == ChinManagerState.IDLE)
							{
								if (client.getGameState() == GameState.LOGIN_SCREEN)
								{
									shouldSetup = true;
									transition(ChinManagerStates.LOGIN);
								}
							}
						}
					})
		);
	}

	@Override
	protected void shutDown()
	{

		executorService.shutdown();

		logout = false;
		shouldSetup = false;
		delay = -1;

		overlayManager.remove(managerClickboxOverlay);
		overlayManager.remove(managerClickboxDebugOverlay);
		overlayManager.remove(managerWidgetOverlay);
		overlayManager.remove(managerTileIndicatorsOverlay);

		eventBus.unregister(api);
		eventBus.unregister(banking);
		eventBus.unregister(hopper);
		eventBus.unregister(regionManager);
		eventBus.unregister(notifications);
		eventBus.unregister("AccountPanel");
		eventBus.unregister("PluginPanel");

		Api.getObjects().clear();
		Api.getTileItems().clear();

		try
		{
			if (taskExecutor.isRunning())
			{
				taskExecutor.stop();
			}
		}
		catch (Exception ignored)
		{
		}

		Set<CompositeDisposable> compositeDisposableSet = Stream.of(
				WebsocketManager.DISPOSABLES,
				ChinManagerPanel.DISPOSABLES,
				PluginPanel.DISPOSABLES,
				StatusPanel.DISPOSABLES,
				InfoPanel.DISPOSABLES,
				BreakOptionsPanel.DISPOSABLES,
				BreakOptionsPanel.BREAK_OPTIONS_DISPOSABLES,
				OsrsAccountPanel.DISPOSABLES,
				WebAccountPanel.DISPOSABLES,
				DISPOSABLES
			)
			.collect(Collectors.toSet());
		compositeDisposableSet.addAll(PLUGIN_DISPOSABLE_MAP.values());

		compositeDisposableSet.forEach(compositeDisposable -> {
			if (compositeDisposable != null && !compositeDisposable.isDisposed())
			{
				compositeDisposable.clear();
			}
		});

		Set.copyOf(chinManager.getManagerPlugins()).forEach((plugin) ->
			chinManager.unregisterManagerPlugin(plugin)
		);

		clientToolbar.removeNavigation(navButton);
		com.owain.automation.Banking.ITEMS = Set.of();
	}

	private void currentlyActive()
	{
		String plugin = Plugins.sanitizedName(chinManager.getCurrentlyActive());

		if (chinManager.isCurrentlyActive(this) && stateMachine.getState() == ChinManagerState.IDLE)
		{
			if (client.getGameState() == GameState.LOGIN_SCREEN)
			{
				shouldSetup = true;
				transition(ChinManagerStates.LOGIN);
			}
			else if (shouldSetup)
			{
				transition(ChinManagerStates.SETUP);
			}
			else if (chinManager.getHandover().contains(chinManager.getPlugin(plugin)))
			{
				transition(ChinManagerStates.IDLE);
			}
			else
			{
				transition(ChinManagerStates.RESUME);
			}
		}
		else if (chinManager.getCurrentlyActive() != null && !chinManager.isCurrentlyActive(this))
		{
			String current = chinManager.getCurrentlyActive().getName();

			if (!notificationsApi.cachedPlugin.equals(current))
			{
				notificationsApi.sendNotification(
					"plugin",
					Map.of(
						"previousPlugin", notificationsApi.cachedPlugin,
						"nextPlugin", current
					)
				);

				notificationsApi.previousPlugin = notificationsApi.cachedPlugin;
				notificationsApi.cachedPlugin = current;
			}
		}
	}

	public void scheduleBreak(Plugin plugin)
	{
		int thresholdFrom = Integer.parseInt(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(plugin) + "-thresholdfrom")) * 60;
		int thresholdTo = Integer.parseInt(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(plugin) + "-thresholdto")) * 60;

		int thresholdRandom = new IntRandomNumberGenerator(thresholdFrom, thresholdTo).nextInt();

		int breakFrom = Integer.parseInt(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(plugin) + "-breakfrom")) * 60;
		int breakTo = Integer.parseInt(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(plugin) + "-breakto")) * 60;

		int breakRandom = new IntRandomNumberGenerator(breakFrom, breakTo).nextInt();

		Instant thresholdInstant = Instant.now().plus(thresholdRandom, ChronoUnit.SECONDS);

		chinManager.planBreak(plugin, thresholdInstant, breakRandom);
	}

	private void breakActivated()
	{
		if (!chinManager.getActiveSortedPlugins().isEmpty() && chinManager.getActiveBreaks().size() == chinManager.getActiveSortedPlugins().size())
		{
			chinManager.addAmountOfBreaks();
			chinManager.setCurrentlyActive(null);
			if (chinManager.getActiveSortedPlugins().size() > 1)
			{
				logout = true;
				delay = 0;
			}
			else
			{
				Plugin plugin = chinManager.getActiveBreaks().keySet().stream().findFirst().orElse(null);

				if (plugin == null)
				{
					return;
				}

				Map<Plugin, Boolean> plugins = chinManager.getPlugins();

				if (plugins.containsKey(plugin) && plugins.get(plugin))
				{
					if (Boolean.parseBoolean(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(plugin) + "-logout")))
					{
						logout = true;
						delay = 0;
					}
				}
				else
				{
					logout = true;
					delay = 0;
				}
			}
		}
		else
		{
			chinManager.setCurrentlyActive(null);
		}
	}

	private void seconds(long ignored)
	{
		if (stateMachine.getState() != ChinManagerState.IDLE)
		{
			return;
		}

		Plugin next = chinManager.getNextActive();

		if (chinManager.getActiveBreaks().containsKey(next))
		{
			Instant duration = chinManager.getActiveBreaks().get(next);
			if (Instant.now().isAfter(duration))
			{
				if (chinManager.getCurrentlyActive() != null)
				{
					chinManager.planHandover(next);
				}
				else
				{
					clientThread.invoke(() -> {
						if (client.getGameState() == GameState.LOGIN_SCREEN)
						{
							transition(ChinManagerStates.LOGIN);
						}
						else
						{
							chinManager.setCurrentlyActive(next);
						}
					});
				}
			}
		}
		else if (chinManager.getCurrentlyActive() == null)
		{
			chinManager.setCurrentlyActive(next);
		}
	}

	@Subscribe(priority = -98)
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		chinManager.gameStateChanged.onNext(gameStateChanged);

		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			if (!chinManager.getActiveSortedPlugins().isEmpty())
			{
				if (chinManager.getActiveBreaks().isEmpty() && stateMachine.getState() == ChinManagerState.IDLE)
				{
					transition(ChinManagerStates.LOGIN);
				}
			}
		}
	}

	@Subscribe(priority = -98)
	public void onGameTick(GameTick gameTick)
	{
		Widget bankPinConfirm = client.getWidget(MagicNumberWidgets.BANK_PIN_CONFIRM_BUTTON.getGroupId(), MagicNumberWidgets.BANK_PIN_CONFIRM_BUTTON.getChildId());

		if (chinManager.getActiveSortedPlugins().size() > 0 && stateMachine.getState() != ChinManagerState.BANK_PIN &&
			client.getWidget(WidgetInfo.BANK_PIN_CONTAINER) != null)
		{
			transition(ChinManagerStates.BANK_PIN);
		}
		else if (chinManager.getActiveSortedPlugins().size() > 0 && stateMachine.getState() != ChinManagerState.BANK_PIN_CONFIRM &&
			bankPinConfirm != null && bankPinConfirm.getText().contains("I want this PIN"))
		{
			transition(ChinManagerStates.BANK_PIN_CONFIRM);
		}
		else if (stateMachine.getState() == ChinManagerState.IDLE && logout && delay == 0)
		{
			if (!chinManager.getActiveSortedPlugins().isEmpty() && chinManager.getActiveBreaks().size() == chinManager.getActiveSortedPlugins().size())
			{
				transition(ChinManagerStates.LOGOUT);
			}
			else
			{
				logout = false;
			}
		}
		else if (!chinManager.getActiveBreaks().isEmpty())
		{
			Map<Plugin, Instant> activeBreaks = chinManager.getActiveBreaks();

			if (activeBreaks
				.keySet()
				.stream()
				.anyMatch(e ->
					!Boolean.parseBoolean(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(e) + "-logout"))))
			{
				if (client.getKeyboardIdleTicks() > 14900)
				{
					client.setKeyboardIdleTicks(0);
				}
				if (client.getMouseIdleTicks() > 14900)
				{
					client.setMouseIdleTicks(0);
				}

				boolean finished = false;

				for (Map.Entry<Plugin, Instant> pluginInstantEntry : activeBreaks.entrySet())
				{
					if (!Boolean.parseBoolean(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(pluginInstantEntry.getKey()) + "-logout")))
					{
						continue;
					}

					if (Instant.now().isBefore(pluginInstantEntry.getValue()))
					{
						finished = true;
					}
				}

				if (finished && stateMachine.getState() == ChinManagerState.IDLE)
				{
					if (client.getVar(VarClientInt.INVENTORY_TAB) != 3)
					{
						client.runScript(MagicNumberScripts.ACTIVE_TAB.getId(), 3);
					}

					transition(ChinManagerStates.RESUME);
				}
			}
		}

		if (delay > 0)
		{
			delay--;
		}
	}

	public boolean isValidBreak(Plugin plugin)
	{
		Map<Plugin, Boolean> plugins = chinManager.getPlugins();

		if (!plugins.containsKey(plugin))
		{
			return false;
		}

		if (!plugins.get(plugin))
		{
			return true;
		}

		String thresholdfrom = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(plugin) + "-thresholdfrom");
		String thresholdto = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(plugin) + "-thresholdto");
		String breakfrom = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(plugin) + "-breakfrom");
		String breakto = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(plugin) + "-breakto");

		return isNumeric(thresholdfrom) &&
			isNumeric(thresholdto) &&
			isNumeric(breakfrom) &&
			isNumeric(breakto) &&
			Integer.parseInt(thresholdfrom) <= Integer.parseInt(thresholdto) &&
			Integer.parseInt(breakfrom) <= Integer.parseInt(breakto);
	}

	@Subscribe(priority = -98)
	public void onConfigChanged(ConfigChanged configChanged)
	{
		chinManager.configChanged.onNext(configChanged);
	}

	public void loadEquipmentConfig()
	{
		final String storedSetups = configManager.getConfiguration(CONFIG_GROUP, "gsongear");
		if (Strings.isNullOrEmpty(storedSetups))
		{
			equipmentList = new ArrayList<>();
		}
		else
		{
			try
			{
				Type type = new TypeToken<ArrayList<Equipment>>()
				{

				}.getType();

				equipmentList = GSON.fromJson(storedSetups, type);
			}
			catch (Exception e)
			{
				equipmentList = new ArrayList<>();
			}
		}
	}

	public void updateConfig()
	{
		final String json = GSON.toJson(equipmentList);
		configManager.setConfiguration(CONFIG_GROUP, "gsongear", json);
	}

	public void transition(ChinManagerStates state)
	{
		StateMachine.State<ChinManagerContext, ChinManagerStates> currentState = stateMachine.getState();

		if (currentState == ChinManagerState.LOGOUT && client.getGameState() != GameState.LOGIN_SCREEN)
		{
			return;
		}

		if (!chinManager.getActiveSortedPlugins().isEmpty())
		{
			stateMachine.accept(state);
		}
		else if (stateMachine.getState() != ChinManagerState.IDLE)
		{
			stateMachine.accept(ChinManagerStates.IDLE);
		}
	}

	public MenuOptionClicked menuAction(MenuOptionClicked menuOptionClicked, String option, String target, int identifier, MenuAction menuAction, int actionParam, int widgetId)
	{
		log.debug("Before -- MenuOption={} MenuTarget={} Id={} Opcode={} Param0={} Param1={} isItemOp={} ItemOp={} ItemId={} Widget={}", menuOptionClicked.getMenuOption(), menuOptionClicked.getMenuTarget(), menuOptionClicked.getId(), menuOptionClicked.getMenuAction(), menuOptionClicked.getParam0(), menuOptionClicked.getParam1(), menuOptionClicked.isItemOp(), menuOptionClicked.getItemOp(), menuOptionClicked.getItemId(), menuOptionClicked.getWidget());

		menuOptionClicked.setMenuOption(option);
		menuOptionClicked.setMenuTarget(target);
		menuOptionClicked.setId(identifier);
		menuOptionClicked.setMenuAction(menuAction);
		menuOptionClicked.setParam0(actionParam);
		menuOptionClicked.setParam1(widgetId);

		log.debug("After -- MenuOption={} MenuTarget={} Id={} Opcode={} Param0={} Param1={} isItemOp={} ItemOp={} ItemId={} Widget={}", menuOptionClicked.getMenuOption(), menuOptionClicked.getMenuTarget(), menuOptionClicked.getId(), menuOptionClicked.getMenuAction(), menuOptionClicked.getParam0(), menuOptionClicked.getParam1(), menuOptionClicked.isItemOp(), menuOptionClicked.getItemOp(), menuOptionClicked.getItemId(), menuOptionClicked.getWidget());

		highlight(client, menuOptionClicked);

		return menuOptionClicked;
	}

//	public static Set<Actor> getActors()
//	{
//		return Api.actors;
//	}

	public static Set<TileObject> getObjects()
	{
		return Api.objects;
	}

	public NPC getNPC(int id)
	{
		return getNPC(client, List.of(id));
	}

	public NPC getNPC(List<Integer> ids)
	{
		return getNPC(client, ids, client.getLocalPlayer());
	}

	public NPC getNPC(List<Integer> ids, Locatable locatable)
	{
		return getNPC(client, ids, locatable);
	}

	public static NPC getNPC(Client client, int id)
	{
		return Api.getNPC(client, List.of(id));
	}

	public static NPC getNPC(Client client, List<Integer> ids)
	{
		return Api.getNPC(client, ids, client.getLocalPlayer());
	}

	public static NPC getNPC(Client client, List<Integer> ids, Locatable locatable)
	{
		return Api.getNPC(client, ids, locatable);
	}

	public TileObject getObject(int id)
	{
		return getObject(client, List.of(id));
	}

	public TileObject getObject(List<Integer> ids)
	{
		return getObject(client, ids, client.getLocalPlayer());
	}

	public TileObject getObject(List<Integer> ids, Locatable locatable)
	{
		return getObject(client, ids, locatable);
	}

	public TileObject getObject(int id, int x, int y)
	{
		return getObject(client, id, x, y);
	}

	public TileObject getObject(WorldPoint wp)
	{
		return Api.getObject(client, wp);
	}

	public static TileObject getObject(Client client, int id)
	{
		return Api.getObject(client, List.of(id));
	}

	public static TileObject getObject(Client client, List<Integer> ids)
	{
		return Api.getObject(client, ids, client.getLocalPlayer());
	}

	public static TileObject getObject(Client client, List<Integer> ids, Locatable locatable)
	{
		return Api.getObject(client, ids, locatable);
	}

	public static TileObject getObject(Client client, int id, int x, int y)
	{
		return Api.getObject(client, id, x, y);
	}

	public static TileObject getObject(Client client, WorldPoint wp)
	{
		return Api.getObject(client, wp);
	}

	public static TileObject getBankObject(Client client)
	{
		return Api.getBankObject(client);
	}

	public static TileObject getReachableObject(Client client, int id, int limit)
	{
		return Api.getReachableObject(client, List.of(id), limit);
	}

	public static TileObject getReachableObject(Client client, List<Integer> ids, int limit)
	{
		return Api.getReachableObject(client, ids, limit, client.getLocalPlayer());
	}

	public static TileObject getReachableObject(Client client, List<Integer> ids, int limit, Locatable locatable)
	{
		return Api.getReachableObject(client, ids, limit, locatable);
	}

	public static boolean canReachWorldPointOrSurrounding(Client client, WorldPoint worldPoint)
	{
		return Api.canReachWorldPointOrSurrounding(client, worldPoint);
	}

	public static NPC getBankNpc(Client client)
	{
		return Api.getBankNpc(client);
	}

	public static boolean isAtBank(Client client)
	{
		return getBankNpc(client) != null || getBankObject(client) != null;
	}

	public static Point getLocation(TileObject tileObject)
	{
		return Api.getLocation(tileObject);
	}

	public int getLowestItemMatch(List<Integer> items)
	{
		return Api.getLowestItemMatch(items, client);
	}

	public static int runeOrRunepouch(Runes runes, Client client)
	{
		return Api.runeOrRunepouch(runes, client);
	}

	public static void highlight(Client client, MenuOptionClicked menuOptionClicked)
	{
		Overlays.highlight(client, menuOptionClicked);
	}

	public static void resetHighlight()
	{
		Overlays.resetHighlight();
	}

	@Deprecated
	public static List<WorldPoint> getHighlightDaxPath()
	{
		return Overlays.getHighlightPath();
	}

	@Deprecated
	public static void setHighlightDaxPath(List<WorldPoint> worldPoints)
	{
		Overlays.setHighlightPath(worldPoints);
	}

	public int itemOptionToId(int itemId, String match)
	{
		return itemOptionToId(itemId, List.of(match));
	}

	public ItemComposition getItemDefinition(int id)
	{
		if (itemCompositionMap.containsKey(id))
		{
			return itemCompositionMap.get(id);
		}
		else
		{
			ItemComposition def = client.getItemDefinition(id);
			itemCompositionMap.put(id, def);

			return def;
		}
	}

	public int itemOptionToId(int itemId, List<String> match)
	{
		ItemComposition itemDefinition = getItemDefinition(itemId);

		int index = 0;
		for (String action : itemDefinition.getInventoryActions())
		{
			if (action != null && match.stream().anyMatch(action::equalsIgnoreCase))
			{
				if (index <= 2)
				{
					return index + 2;
				}
				else
				{
					return index + 3;
				}
			}

			index++;
		}

		return -1;
	}

	public MenuAction idToMenuAction(int id)
	{
		if (id <= 5)
		{
			return MenuAction.CC_OP;
		}
		else
		{
			return MenuAction.CC_OP_LOW_PRIORITY;
		}
	}
}