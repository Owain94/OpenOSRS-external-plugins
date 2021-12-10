package com.owain.chinmanager;

import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import com.owain.automation.Banking;
import static com.owain.automation.ContainerUtils.getBankInventoryWidgetItemForItemsPos;
import static com.owain.automation.ContainerUtils.getBankWidgetItemForItemsPos;
import static com.owain.automation.ContainerUtils.getInventoryWidgetItemForItemsPos;
import static com.owain.automation.ContainerUtils.hasBankInventoryItem;
import static com.owain.automation.ContainerUtils.hasBankItem;
import static com.owain.automation.ContainerUtils.hasItem;
import static com.owain.chinmanager.ChinManagerState.stateMachine;
import com.owain.chinmanager.api.NotificationsApi;
import com.owain.chinmanager.cookies.PersistentCookieJar;
import com.owain.chinmanager.cookies.cache.SetCookieCache;
import com.owain.chinmanager.cookies.persistence.OpenOSRSCookiePersistor;
import com.owain.chinmanager.magicnumbers.MagicNumberScripts;
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
import com.owain.chinmanager.utils.IntRandomNumberGenerator;
import static com.owain.chinmanager.utils.Integers.isNumeric;
import com.owain.chinmanager.utils.Plugins;
import static com.owain.chinmanager.utils.Plugins.sanitizedName;
import com.owain.chinmanager.websockets.WebsocketManager;
import com.owain.chintasks.TaskExecutor;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.DecorativeObject;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.ItemLayer;
import net.runelite.api.Locatable;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.VarClientInt;
import net.runelite.api.Varbits;
import net.runelite.api.WallObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.DecorativeObjectChanged;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectChanged;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.WallObjectChanged;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.queries.DecorativeObjectQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.GroundObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.queries.WallObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import static net.runelite.api.widgets.WidgetInfo.BANK_PIN_INSTRUCTION_TEXT;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import net.runelite.client.util.WorldUtil;
import static net.runelite.http.api.RuneLiteAPI.GSON;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.tuple.Pair;
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

	private static final Pattern LEVEL_UP_PATTERN = Pattern.compile(".*Your ([a-zA-Z]+) (?:level is|are)? now (\\d+)\\.");
	private static final String COLLECTION_LOG_TEXT = "New item added to your collection log: ";
	private static final List<String> PET_MESSAGES = List.of("You have a funny feeling like you're being followed",
	"You feel something weird sneaking into your backpack",
	"You have a funny feeling like you would have been followed");

	public static final CompositeDisposable DISPOSABLES = new CompositeDisposable();
	public static final Map<Plugin, CompositeDisposable> PLUGIN_DISPOSABLE_MAP = new HashMap<>();

	private static final Varbits[] AMOUNT_VARBITS =
		{
			Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3
		};
	private static final Varbits[] RUNE_VARBITS =
		{
			Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3
		};

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

	@Getter(AccessLevel.PUBLIC)
	public static final List<WidgetItem> highlightWidgetItem = new ArrayList<>();

	// Debug
	@Getter(AccessLevel.PUBLIC)
	public static final Map<TileObject, Integer> debugTileObjectMap = new HashMap<>();

	@Getter(AccessLevel.PUBLIC)
	public static final Set<WorldPoint> debugReachableWorldAreas = new HashSet<>();

	@Getter(AccessLevel.PUBLIC)
	public static final Map<WorldPoint, Integer> debugReachableTiles = new HashMap<>();

	private static final int DISPLAY_SWITCHER_MAX_ATTEMPTS = 3;

	@Getter(AccessLevel.PUBLIC)
	private final static Map<TileItem, Tile> tileItems = new HashMap<>();

	@Getter(AccessLevel.PUBLIC)
	private final static Set<TileObject> objects = new HashSet<>();

	@Getter(AccessLevel.PUBLIC)
	private final static Set<Actor> actors = new HashSet<>();

	public static boolean logout;
	public static boolean shouldSetup;

	@Getter(AccessLevel.PUBLIC)
	private static List<Equipment> equipmentList;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private static String profileData;

	@Getter(AccessLevel.PUBLIC)
	private static Actor highlightActor = null;

	@Getter(AccessLevel.PUBLIC)
	private static ItemLayer highlightItemLayer = null;

	@Getter(AccessLevel.PUBLIC)
	private static TileObject highlightTileObject = null;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private static List<WorldPoint> highlightDaxPath = null;

	@Getter(AccessLevel.PUBLIC)
	private static Widget highlightWidget = null;

	@Getter(AccessLevel.PUBLIC)
	public OkHttpClient okHttpClient;

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
	private ItemManager itemManager;

	@Inject
	private WorldService worldService;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private TaskExecutor taskExecutor;

	@Inject
	private OptionsConfig optionsConfig;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private ExecutorService executorService;

	@Inject
	@SuppressWarnings("unused")
	private ChinManagerState chinManagerState;

	@Inject
	private ChatMessageManager chatMessageManager;

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

	private NavigationButton navButton;

	@Getter(AccessLevel.PUBLIC)
	private Random random;

	private int delay = -1;
	private net.runelite.api.World quickHopTargetWorld;
	private int displaySwitcherAttempts = 0;

	private Set<String> notifyLevels = new HashSet<>();

	public static void resetHighlight()
	{
		highlightActor = null;
		highlightItemLayer = null;
		highlightTileObject = null;
		debugTileObjectMap.clear();
		highlightWidgetItem.clear();
		debugReachableWorldAreas.clear();
		debugReachableTiles.clear();
		highlightDaxPath = null;
		highlightWidget = null;
	}

	public static void highlight(Client client, MenuOptionClicked menuOptionClicked)
	{
		resetHighlight();

		switch (menuOptionClicked.getMenuAction())
		{
			case GAME_OBJECT_FIRST_OPTION:
			case GAME_OBJECT_SECOND_OPTION:
			case GAME_OBJECT_THIRD_OPTION:
			case GAME_OBJECT_FOURTH_OPTION:
			case GAME_OBJECT_FIFTH_OPTION:
			case ITEM_USE_ON_GAME_OBJECT:
			case SPELL_CAST_ON_GAME_OBJECT:
			{
				TileObject tileObject = getObject(client, menuOptionClicked.getId(), menuOptionClicked.getParam0(), menuOptionClicked.getParam1());

				if (tileObject != null)
				{
					highlightTileObject = tileObject;
				}

				break;
			}
			case NPC_FIRST_OPTION:
			case NPC_SECOND_OPTION:
			case NPC_THIRD_OPTION:
			case NPC_FOURTH_OPTION:
			case NPC_FIFTH_OPTION:
			case ITEM_USE_ON_NPC:
			case SPELL_CAST_ON_NPC:
			{
				client.getNpcs().stream().filter((npc) -> npc.getIndex() == menuOptionClicked.getId()).findFirst().ifPresent(value -> highlightActor = value);

				break;
			}
			case GROUND_ITEM_FIRST_OPTION:
			case GROUND_ITEM_SECOND_OPTION:
			case GROUND_ITEM_THIRD_OPTION:
			case GROUND_ITEM_FOURTH_OPTION:
			case GROUND_ITEM_FIFTH_OPTION:
			{
				LocalPoint localPoint = LocalPoint.fromScene(menuOptionClicked.getParam0(), menuOptionClicked.getParam1());

				Map.copyOf(ChinManagerPlugin.getTileItems())
					.values()
					.stream()
					.filter(Objects::nonNull)
					.filter(nonNullTile -> nonNullTile.getLocalLocation().equals(localPoint))
					.findFirst()
					.flatMap(tile -> tile
						.getGroundItems()
						.stream()
						.filter((tileItem) -> tileItem.getId() == menuOptionClicked.getId())
						.findFirst()
					)
					.ifPresent(value -> highlightItemLayer = value.getTile().getItemLayer());

				break;
			}
			case CC_OP:
			case CC_OP_LOW_PRIORITY:
			{
				if (menuOptionClicked.getParam0() == -1 && !menuOptionClicked.getMenuOption().equals("Toggle Run"))
				{
					Widget widget = client.getWidget(menuOptionClicked.getParam1());

					if (widget != null)
					{
						highlightWidget = widget;

						return;
					}
				}

				Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
				Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
				Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

				if (bankContainer != null && bankContainer.getId() == menuOptionClicked.getParam1())
				{
					highlightWidgetItem.add(getBankWidgetItemForItemsPos(menuOptionClicked.getParam0(), client));
				}
				else if (inventory != null && inventory.getId() == menuOptionClicked.getParam1())
				{
					highlightWidgetItem.add(getInventoryWidgetItemForItemsPos(menuOptionClicked.getParam0(), client));
				}
				else if (bankInventory != null && bankInventory.getId() == menuOptionClicked.getParam1())
				{
					highlightWidgetItem.add(getBankInventoryWidgetItemForItemsPos(menuOptionClicked.getParam0(), client));
				}

				break;
			}
			case ITEM_USE_ON_WIDGET_ITEM:
			{
				Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

				if (inventory != null && inventory.getId() == menuOptionClicked.getParam1())
				{
					highlightWidgetItem.add(getInventoryWidgetItemForItemsPos(menuOptionClicked.getParam0(), client));
				}
			}
		}
	}

	public static NPC getNPC(Client client, int id)
	{
		return getNPC(client, List.of(id));
	}

	public static NPC getNPC(Client client, List<Integer> ids)
	{
		return getNPC(client, ids, client.getLocalPlayer());
	}

	public static NPC getNPC(Client client, List<Integer> ids, Locatable locatable)
	{
		return Set.copyOf(
				ChinManagerPlugin.getActors()
			)
			.stream()
			.filter(Objects::nonNull)
			.filter(npc -> npc instanceof NPC)
			.map(npc -> (NPC) npc)
			.filter(npc -> ids.contains(npc.getId()))
			.min(Comparator.comparing(npc ->
				npc
					.getWorldLocation()
					.distanceTo(locatable.getWorldLocation())
			))
			.orElse(
				new NPCQuery()
					.idEquals(ids)
					.result(client)
					.nearestTo(client.getLocalPlayer())
			);
	}

	public static TileObject getObject(Client client, int id)
	{
		return getObject(client, List.of(id));
	}

	public static TileObject getObject(Client client, List<Integer> ids)
	{
		return getObject(client, ids, client.getLocalPlayer());
	}

	public static TileObject getObject(Client client, List<Integer> ids, Locatable locatable)
	{
		return Set.copyOf(
				ChinManagerPlugin.getObjects()
			)
			.stream()
			.filter(tileObject -> ids.contains(tileObject.getId()))
			.filter(tileObject -> tileObject.getPlane() == client.getPlane())
			.min(Comparator.comparing(tileObject ->
				tileObject
					.getWorldLocation()
					.distanceTo(locatable.getWorldLocation())
			))
			.orElse(getObjectAlt(client, ids, locatable));
	}

	public static TileObject getObject(Client client, int id, int x, int y)
	{
		WorldPoint wp = WorldPoint.fromScene(client, x, y, client.getPlane());

		return Set.copyOf(
				ChinManagerPlugin.getObjects()
			)
			.stream()
			.filter(Objects::nonNull)
			.filter(tileObject -> tileObject.getId() == id)
			.filter(tileObject -> tileObject.getPlane() == client.getPlane())
			.filter(tileObject -> {
				if (tileObject instanceof GameObject)
				{
					GameObject gameObject = (GameObject) tileObject;

					Point sceneLocation = gameObject.getSceneMinLocation();

					if (sceneLocation.getX() == x && sceneLocation.getY() == y)
					{
						return true;
					}
				}

				if (tileObject.getWorldLocation().equals(wp))
				{
					return true;
				}

				return false;
			})
			.min(Comparator.comparing(tileObject ->
				tileObject
					.getWorldLocation()
					.distanceTo(
						client
							.getLocalPlayer()
							.getWorldLocation()
					)
			))
			.orElse(getObjectAlt(client, id, wp));
	}

	public static TileObject getObject(Client client, WorldPoint wp)
	{
		return Set.copyOf(
				ChinManagerPlugin.getObjects()
			)
			.stream()
			.filter(Objects::nonNull)
			.filter(tileObject -> tileObject.getWorldLocation().equals(wp))
			.min(Comparator.comparing(tileObject ->
				tileObject
					.getWorldLocation()
					.distanceTo(
						client
							.getLocalPlayer()
							.getWorldLocation()
					)
			))
			.orElse(getObjectAlt(client, wp));
	}

	public static TileObject getObjectAlt(Client client, List<Integer> ids, Locatable locatable)
	{
		GameObject gameObject = new GameObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(locatable);

		if (gameObject != null)
		{
			return gameObject;
		}

		DecorativeObject decorativeObject = new DecorativeObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(locatable);

		if (decorativeObject != null)
		{
			return decorativeObject;
		}

		GroundObject groundObject = new GroundObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(locatable);

		if (groundObject != null)
		{
			return groundObject;
		}

		WallObject wallObject = new WallObjectQuery()
			.idEquals(ids)
			.result(client)
			.nearestTo(locatable);

		if (wallObject != null)
		{
			return wallObject;
		}

		return null;
	}

	public static TileObject getObjectAlt(Client client, int id, WorldPoint wp)
	{
		GameObject gameObject = new GameObjectQuery()
			.idEquals(id)
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (gameObject != null)
		{
			return gameObject;
		}

		DecorativeObject decorativeObject = new DecorativeObjectQuery()
			.idEquals(id)
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (decorativeObject != null)
		{
			return decorativeObject;
		}

		GroundObject groundObject = new GroundObjectQuery()
			.idEquals(id)
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (groundObject != null)
		{
			return groundObject;
		}

		WallObject wallObject = new WallObjectQuery()
			.idEquals(id)
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (wallObject != null)
		{
			return wallObject;
		}

		return null;
	}

	public static TileObject getObjectAlt(Client client, WorldPoint wp)
	{
		GameObject gameObject = new GameObjectQuery()
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (gameObject != null)
		{
			return gameObject;
		}

		DecorativeObject decorativeObject = new DecorativeObjectQuery()
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (decorativeObject != null)
		{
			return decorativeObject;
		}

		GroundObject groundObject = new GroundObjectQuery()
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (groundObject != null)
		{
			return groundObject;
		}

		WallObject wallObject = new WallObjectQuery()
			.atWorldLocation(wp)
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (wallObject != null)
		{
			return wallObject;
		}

		return null;
	}

	public static TileObject getBankObject(Client client)
	{
		return Set.copyOf(
				ChinManagerPlugin.getObjects()
			)
			.stream()
			.filter(Objects::nonNull)
			.filter(tileObject -> {
				List<String> actions = Arrays.asList(
					client.getObjectDefinition(
							tileObject.getId()
						)
						.getActions()
				);

				List<String> imposterActions = new ArrayList<>();

				ObjectComposition objectComposition = client.getObjectDefinition(tileObject.getId());
				int[] ids = objectComposition.getImpostorIds();

				if (ids != null && ids.length > 0)
				{
					ObjectComposition imposter = objectComposition.getImpostor();

					if (imposter != null)
					{
						imposterActions.addAll(Arrays.asList(imposter.getActions()));
					}
				}

				return actions.contains("Bank") || actions.contains("Collect") ||
					imposterActions.contains("Bank") || imposterActions.contains("Collect");
			})
			.filter(tileObject -> tileObject.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < 16)
			.min(Comparator.comparing(npc -> {
					List<Tile> path = tile(client, client.getLocalPlayer().getWorldLocation()).pathTo(tile(client, npc.getWorldLocation()));
					if (path == null)
					{
						return Integer.MAX_VALUE;
					}
					else
					{
						return path.size();
					}
				}
			))
			.orElse(
				getBankObjectAlt(client)
			);
	}

	public static TileObject getBankObjectAlt(Client client)
	{
		return getAllObjects(client)
			.stream()
			.filter(Objects::nonNull)
			.filter(tileObject -> {
				List<String> actions = Arrays.asList(
					client.getObjectDefinition(
							tileObject.getId()
						)
						.getActions()
				);

				List<String> imposterActions = new ArrayList<>();

				ObjectComposition objectComposition = client.getObjectDefinition(tileObject.getId());
				int[] ids = objectComposition.getImpostorIds();

				if (ids != null && ids.length > 0)
				{
					ObjectComposition imposter = objectComposition.getImpostor();

					if (imposter != null)
					{
						imposterActions.addAll(Arrays.asList(imposter.getActions()));
					}
				}

				return actions.contains("Bank") || actions.contains("Collect") ||
					imposterActions.contains("Bank") || imposterActions.contains("Collect");
			})
			.filter(tileObject -> tileObject.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < 16)
			.min(Comparator.comparing(npc -> {
					List<Tile> path = tile(client, client.getLocalPlayer().getWorldLocation()).pathTo(tile(client, npc.getWorldLocation()));
					if (path == null)
					{
						return Integer.MAX_VALUE;
					}
					else
					{
						return path.size();
					}
				}
			))
			.orElse(null);
	}

	public static NPC getBankNpc(Client client)
	{
		return Set.copyOf(
				ChinManagerPlugin.getActors()
			)
			.stream()
			.filter(Objects::nonNull)
			.filter(npc -> npc instanceof NPC)
			.map(npc -> (NPC) npc)
			.filter(npc -> {
				List<String> actions = Arrays.asList(
					client.getNpcDefinition(
							npc.getId()
						)
						.getActions()
				);

				return actions.contains("Bank");
			})
			.filter(npc -> npc.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < 16)
			.min(Comparator.comparing(npc -> {
					List<Tile> path = tile(client, client.getLocalPlayer().getWorldLocation()).pathTo(tile(client, npc.getWorldLocation()));
					if (path == null)
					{
						return Integer.MAX_VALUE;
					}
					else
					{
						return path.size();
					}
				}
			))
			.orElse(
				getBankNpcAlt(client)
			);
	}

	public static NPC getBankNpcAlt(Client client)
	{
		return client.getNpcs()
			.stream()
			.filter(Objects::nonNull)
			.filter(npc -> {
				List<String> actions = Arrays.asList(
					client.getNpcDefinition(
							npc.getId()
						)
						.getActions()
				);

				return actions.contains("Bank");
			})
			.filter(npc -> npc.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < 16)
			.min(Comparator.comparing(npc -> {
					List<Tile> path = tile(client, client.getLocalPlayer().getWorldLocation()).pathTo(tile(client, npc.getWorldLocation()));
					if (path == null)
					{
						return Integer.MAX_VALUE;
					}
					else
					{
						return path.size();
					}
				}
			))
			.orElse(null);
	}

	public static boolean isAtBank(Client client)
	{
		return getBankNpc(client) != null || getBankObject(client) != null;
	}

	public static Point getLocation(TileObject tileObject)
	{
		if (tileObject instanceof GameObject)
		{
			return ((GameObject) tileObject).getSceneMinLocation();
		}
		else
		{
			return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
		}
	}

	public static TileObject getReachableObject(Client client, int id, int limit)
	{
		return getReachableObject(client, List.of(id), limit);
	}

	public static TileObject getReachableObject(Client client, List<Integer> ids, int limit)
	{
		return getReachableObject(client, ids, limit, client.getLocalPlayer());
	}

	public static TileObject getReachableObject(Client client, List<Integer> ids, int limit, Locatable locatable)
	{
		debugReachableWorldAreas.clear();
		debugReachableTiles.clear();
		debugTileObjectMap.clear();

		return Set.copyOf(
				ChinManagerPlugin.getObjects()
			)
			.stream()
			.filter(Objects::nonNull)
			.filter(tileObject -> ids.contains(tileObject.getId()))
			.filter(tileObject -> tileObject.getPlane() == client.getPlane())
			.sorted(Comparator.comparing(tileObject -> locatable.getWorldLocation().distanceTo(tileObject.getWorldLocation())))
			.limit(limit)
			.filter(tileObject -> tileObject.getWorldLocation().distanceTo(new WorldPoint(1787, 3589, 0)) != 0)
			.filter(tileObject -> tileObject.getWorldLocation().distanceTo(new WorldPoint(1787, 3599, 0)) != 0)
			.filter(tileObject -> tileObject.getWorldLocation().distanceTo(new WorldPoint(3255, 3463, 0)) != 0)
			.parallel()
			.map(tileObject -> {
				int width = 1;
				int height = 1;

				if (tileObject instanceof GameObject)
				{
					width = ((GameObject) tileObject).sizeX();
					height = ((GameObject) tileObject).sizeY();
				}

				WorldPoint objectWorldPoint;
				objectWorldPoint = tileObject.getWorldLocation();

				if (width == 3 || height == 3)
				{
					objectWorldPoint = new WorldPoint(width == 3 ? objectWorldPoint.getX() - 1 : objectWorldPoint.getX(), height == 3 ? objectWorldPoint.getY() - 1 : objectWorldPoint.getY(), objectWorldPoint.getPlane());
				}

				List<WorldPoint> originalArea = new WorldArea(objectWorldPoint.getX(), objectWorldPoint.getY(), width, height, objectWorldPoint.getPlane()).toWorldPointList();
				Set<WorldPoint> possibleWorldPoints = new HashSet<>(originalArea);

				for (WorldPoint worldPoint : new WorldArea(objectWorldPoint.getX(), objectWorldPoint.getY(), width, height, objectWorldPoint.getPlane()).toWorldPointList())
				{
					possibleWorldPoints.add(new WorldPoint(worldPoint.getX() + 1, worldPoint.getY(), worldPoint.getPlane()));
					possibleWorldPoints.add(new WorldPoint(worldPoint.getX(), worldPoint.getY() + 1, worldPoint.getPlane()));
					possibleWorldPoints.add(new WorldPoint(worldPoint.getX() - 1, worldPoint.getY(), worldPoint.getPlane()));
					possibleWorldPoints.add(new WorldPoint(worldPoint.getX(), worldPoint.getY() - 1, worldPoint.getPlane()));
				}

				originalArea.forEach(possibleWorldPoints::remove);
				debugReachableWorldAreas.addAll(possibleWorldPoints);

				Pair<TileObject, Integer> closest = Pair.of(tileObject, Integer.MAX_VALUE);

				for (WorldPoint wp : possibleWorldPoints)
				{
					if (wp.distanceTo(locatable.getWorldLocation()) == 0)
					{
						return Pair.of(tileObject, 0);
					}

					Tile startTile = tile(client, locatable.getWorldLocation());
					Tile endTile = tile(client, wp);

					if (startTile == null || endTile == null)
					{
						continue;
					}

					List<Tile> path = startTile.pathTo(endTile);

					if (path != null && path.get(path.size() - 1).getWorldLocation().distanceTo(wp) == 0)
					{
						debugReachableTiles.put(wp, path.size());

						if (path.size() < closest.getValue())
						{
							closest = Pair.of(tileObject, path.size());
						}
					}
				}

				return closest;
			})
			.filter(pair -> pair.getValue() < Integer.MAX_VALUE)
			.peek(pair -> debugTileObjectMap.put(pair.getKey(), pair.getValue()))
			.min(
				Comparator.comparing(
						(Pair<TileObject, Integer> pair) ->
							pair.getKey().getWorldLocation().distanceTo(locatable.getWorldLocation())
					)
					.thenComparing(Pair::getValue))
			.map(Pair::getKey)
			.orElse(null);
	}

	public static boolean canReachWorldPointOrSurrounding(Client client, WorldPoint worldPoint)
	{
		debugReachableWorldAreas.clear();
		debugReachableTiles.clear();
		debugTileObjectMap.clear();

		TileObject object = getObject(client, worldPoint);

		if (object == null)
		{
			Tile start = tile(client, client.getLocalPlayer().getWorldLocation());
			Tile target = tile(client, worldPoint);

			if (start == null || target == null)
			{
				return false;
			}

			List<Tile> path = start.pathTo(target);

			if (path == null)
			{
				return false;
			}
			else if (path.get(path.size() - 1).getWorldLocation().distanceTo(worldPoint) == 0)
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		int width = 1;
		int height = 1;

		if (object instanceof GameObject)
		{
			width = ((GameObject) object).sizeX();
			height = ((GameObject) object).sizeY();
		}

		WorldPoint objectWorldPoint;
		objectWorldPoint = object.getWorldLocation();

		if (width == 3 || height == 3)
		{
			objectWorldPoint = new WorldPoint(width == 3 ? objectWorldPoint.getX() - 1 : objectWorldPoint.getX(), height == 3 ? objectWorldPoint.getY() - 1 : objectWorldPoint.getY(), objectWorldPoint.getPlane());
		}

		List<WorldPoint> area = new WorldArea(objectWorldPoint.getX() - 1, objectWorldPoint.getY() - 1, width + 2, height + 2, objectWorldPoint.getPlane()).toWorldPointList();

		debugReachableWorldAreas.addAll(area);

		for (WorldPoint wp : area)
		{
			if ((getObject(client, wp) instanceof GameObject) ||
				wp.getX() > objectWorldPoint.getX() && wp.getY() > objectWorldPoint.getY() ||
				wp.getX() > objectWorldPoint.getX() && wp.getY() < objectWorldPoint.getY() ||
				wp.getX() < objectWorldPoint.getX() && wp.getY() > objectWorldPoint.getY() ||
				wp.getX() < objectWorldPoint.getX() && wp.getY() < objectWorldPoint.getY())
			{
				continue;
			}

			Tile startTile = tile(client, client.getLocalPlayer().getWorldLocation());
			Tile endTile = tile(client, wp);

			if (startTile == null || endTile == null)
			{
				continue;
			}

			List<Tile> path = startTile.pathTo(endTile);

			if (path != null && path.get(path.size() - 1).getWorldLocation().distanceTo(wp) == 0)
			{

				debugTileObjectMap.put(object, path.size());
				return true;
			}
		}

		return false;
	}

	private static Collection<TileObject> getAllObjects(Client client)
	{
		Collection<TileObject> objects = new ArrayList<>();
		for (Tile tile : getTiles(client))
		{
			GameObject[] gameObjects = tile.getGameObjects();
			if (gameObjects != null)
			{
				objects.addAll(Arrays.asList(gameObjects));
			}

			DecorativeObject decorativeObject = tile.getDecorativeObject();
			if (decorativeObject != null)
			{
				objects.add(decorativeObject);
			}

			GroundObject groundobject = tile.getGroundObject();
			if (groundobject != null)
			{
				objects.add(groundobject);
			}

			WallObject wallObject = tile.getWallObject();
			if (wallObject != null)
			{
				objects.add(wallObject);
			}
		}
		return objects;
	}

	private static List<Tile> getTiles(Client client)
	{
		List<Tile> tilesList = new ArrayList<>();
		Scene scene = client.getScene();
		Tile[][] tiles = scene.getTiles()[client.getPlane()];

		for (int x = 0; x < Constants.SCENE_SIZE; ++x)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; ++y)
			{
				Tile tile = tiles[x][y];
				if (tile == null)
				{
					continue;
				}
				tilesList.add(tile);
			}
		}

		return tilesList;
	}

	private static Tile tile(Client client, WorldPoint position)
	{
		int plane = position.getPlane();
		int x = position.getX() - client.getBaseX();
		int y = position.getY() - client.getBaseY();

		if (plane < 0 || plane >= 4)
		{
			return null;
		}
		if (x < 0 || x >= 104)
		{
			return null;
		}
		if (y < 0 || y >= 104)
		{
			return null;
		}

		return client.getScene().getTiles()[plane][x][y];
	}

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

	@Override
	protected void startUp()
	{
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

		configManager.setConfiguration("bank", "bankPinKeyboard", true);
		client.setHideDisconnect(true);

		Banking.ITEMS = Set.of();

		DISPOSABLES.addAll(
			stateMachine.getStateObservable().subscribe((state) -> ChinManagerPlugin.resetHighlight()),

			stateMachine.connect().subscribe(),

			chinManager
				.getActiveObservable()
				.subscribe((ignored) -> {
					if (chinManager.getActivePlugins().isEmpty())
					{
						shouldSetup = true;
						Banking.ITEMS = Set.of();
						delay = -1;
						logout = false;
						transition(ChinManagerStates.IDLE);
					}
					else
					{
						Banking.ITEMS = chinManager.getActivePlugins()
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
							hop();
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
					if (stateMachine.getState() != ChinManagerState.BANKING)
					{
						transition(ChinManagerStates.BANKING);
					}
				}),

			chinManager
				.getTeleportingObservable()
				.subscribe((plugin) -> {
					if (stateMachine.getState() != ChinManagerState.TELEPORTING)
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

		eventBus.unregister("AccountPanel");
		eventBus.unregister("PluginPanel");

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
		Banking.ITEMS = Set.of();
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
		if (!chinManager.getActivePlugins().isEmpty() && chinManager.getActiveBreaks().size() == chinManager.getActivePlugins().size())
		{
			chinManager.addAmountOfBreaks();
			chinManager.setCurrentlyActive(null);
			if (chinManager.getActivePlugins().size() > 1)
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

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		chinManager.gameStateChanged.onNext(gameStateChanged);

		if (gameStateChanged.getGameState() != GameState.LOGGED_IN && gameStateChanged.getGameState() != GameState.CONNECTION_LOST)
		{
			tileItems.clear();
			objects.clear();
			actors.clear();
		}

		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			if (!chinManager.getActivePlugins().isEmpty())
			{
				if (optionsConfig.hopAfterBreak() && (optionsConfig.american() || optionsConfig.unitedKingdom() || optionsConfig.german() || optionsConfig.australian()))
				{
					hop();
				}

				if (chinManager.getActiveBreaks().isEmpty() && stateMachine.getState() == ChinManagerState.IDLE)
				{
					transition(ChinManagerStates.LOGIN);
				}
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (client.getWidget(WidgetInfo.LEVEL_UP_LEVEL) != null)
		{
			parseLevelUpWidget(WidgetInfo.LEVEL_UP_LEVEL);
		}
		else if (client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT) != null)
		{
			String text = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT).getText();
			if (!Text.removeTags(text).contains("High level gamble"))
			{
				parseLevelUpWidget(WidgetInfo.DIALOG_SPRITE_TEXT);
			}
		}

		if (chinManager.getActivePlugins().size() > 0 && stateMachine.getState() != ChinManagerState.BANK_PIN &&
			client.getWidget(WidgetID.BANK_PIN_GROUP_ID, BANK_PIN_INSTRUCTION_TEXT.getChildId()) != null &&
			(client.getWidget(BANK_PIN_INSTRUCTION_TEXT).getText().equals("First click the FIRST digit.") ||
				client.getWidget(BANK_PIN_INSTRUCTION_TEXT).getText().equals("Now click the SECOND digit.") ||
				client.getWidget(BANK_PIN_INSTRUCTION_TEXT).getText().equals("Time for the THIRD digit.") ||
				client.getWidget(BANK_PIN_INSTRUCTION_TEXT).getText().equals("Finally, the FOURTH digit.")))
		{
			transition(ChinManagerStates.BANK_PIN);
		}
		else if (stateMachine.getState() == ChinManagerState.IDLE && logout && delay == 0)
		{
			if (!chinManager.getActivePlugins().isEmpty() && chinManager.getActiveBreaks().size() == chinManager.getActivePlugins().size())
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

	private void resetQuickHopper()
	{
		displaySwitcherAttempts = 0;
		quickHopTargetWorld = null;
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

	@Subscribe
	public void onWidgetLoaded(final WidgetLoaded widgetLoaded)
	{
		if (Banking.ITEMS.isEmpty())
		{
			return;
		}

		Banking.onWidgetLoaded(widgetLoaded, client, clientThread);
	}

	@Subscribe
	public void onScriptPreFired(final ScriptPreFired scriptPreFired)
	{
		if (Banking.ITEMS.isEmpty())
		{
			return;
		}

		Banking.onScriptPreFired(scriptPreFired, client);
	}

	@Subscribe
	public void onScriptCallbackEvent(final ScriptCallbackEvent scriptCallbackEvent)
	{
		if (Banking.ITEMS.isEmpty())
		{
			return;
		}

		Banking.onScriptCallbackEvent(scriptCallbackEvent, client, itemManager);
	}

	@Subscribe
	public void onScriptPostFired(final ScriptPostFired scriptPostFired)
	{
		if (Banking.ITEMS.isEmpty())
		{
			return;
		}

		Banking.onScriptPostFired(scriptPostFired, client);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		chinManager.configChanged.onNext(configChanged);
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

	private void hop()
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
			.append("Hopping away from a player. New world: ")
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
		if (!chinManager.getActivePlugins().isEmpty())
		{
			stateMachine.accept(state);
		}
		else if (stateMachine.getState() != ChinManagerState.IDLE)
		{
			stateMachine.accept(ChinManagerStates.IDLE);
		}
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned wallObjectSpawned)
	{
		objects.add(wallObjectSpawned.getWallObject());
	}

	@Subscribe
	public void onWallObjectChanged(WallObjectChanged wallObjectChanged)
	{
		objects.remove(wallObjectChanged.getPrevious());
		objects.add(wallObjectChanged.getWallObject());
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned wallObjectDespawned)
	{
		TileObject tileObject = wallObjectDespawned.getWallObject();

		objects.remove(tileObject);

		if (highlightTileObject == tileObject)
		{
			highlightTileObject = null;
		}

		debugTileObjectMap.remove(tileObject);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		objects.add(gameObjectSpawned.getGameObject());
	}

	@Subscribe
	public void onGameObjectChanged(GameObjectChanged gameObjectChanged)
	{
		objects.remove(gameObjectChanged.getPrevious());
		objects.add(gameObjectChanged.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		TileObject tileObject = gameObjectDespawned.getGameObject();

		objects.remove(tileObject);

		if (highlightTileObject == tileObject)
		{
			highlightTileObject = null;
		}

		debugTileObjectMap.remove(tileObject);
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned decorativeObjectSpawned)
	{
		objects.add(decorativeObjectSpawned.getDecorativeObject());
	}

	@Subscribe
	public void onDecorativeObjectChanged(DecorativeObjectChanged decorativeObjectChanged)
	{
		objects.remove(decorativeObjectChanged.getPrevious());
		objects.add(decorativeObjectChanged.getDecorativeObject());
	}

	@Subscribe
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned decorativeObjectDespawned)
	{
		TileObject tileObject = decorativeObjectDespawned.getDecorativeObject();

		objects.remove(tileObject);

		if (highlightTileObject == tileObject)
		{
			highlightTileObject = null;
		}

		debugTileObjectMap.remove(tileObject);
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned)
	{
		objects.add(groundObjectSpawned.getGroundObject());
	}

	@Subscribe
	public void onGroundObjectChanged(GroundObjectChanged groundObjectChanged)
	{
		objects.remove(groundObjectChanged.getPrevious());
		objects.add(groundObjectChanged.getGroundObject());
	}

	@Subscribe
	public void onGroundObjectDespawned(GroundObjectDespawned groundObjectDespawned)
	{
		TileObject tileObject = groundObjectDespawned.getGroundObject();

		objects.remove(tileObject);

		if (highlightTileObject == tileObject)
		{
			highlightTileObject = null;
		}

		debugTileObjectMap.remove(tileObject);
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE
			&& chatMessage.getType() != ChatMessageType.SPAM
			&& chatMessage.getType() != ChatMessageType.TRADE
			&& chatMessage.getType() != ChatMessageType.FRIENDSCHATNOTIFICATION)
		{
			return;
		}

		if (chinManager.getCurrentlyActive() == null || chinManager.isCurrentlyActive(this))
		{
			return;
		}

		String message = chatMessage.getMessage();

		if (PET_MESSAGES.stream().anyMatch(message::contains))
		{
			notificationsApi.sendNotification(
				"pet",
				Map.of(
					"plugin", chinManager.getCurrentlyActive().getName()
				)
			);
		}

		if (message.startsWith(COLLECTION_LOG_TEXT) && client.getVar(Varbits.COLLECTION_LOG_NOTIFICATION) == 1)
		{
			String entry = Text.removeTags(message).substring(COLLECTION_LOG_TEXT.length());

			notificationsApi.sendNotification(
				"collectionlog",
				Map.of(
					"plugin", chinManager.getCurrentlyActive().getName(),
					"entry", entry
				)
			);
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		actors.add(npcSpawned.getNpc());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();

		actors.remove(npc);

		if (highlightActor == npc)
		{
			highlightActor = null;
		}
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned playerSpawned)
	{
		actors.add(playerSpawned.getPlayer());
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned playerDespawned)
	{
		Player player = playerDespawned.getPlayer();

		actors.remove(player);

		if (highlightActor == player)
		{
			highlightActor = null;
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		Tile tile = itemSpawned.getTile();
		TileItem item = itemSpawned.getItem();

		tileItems.put(item, tile);
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		TileItem item = itemDespawned.getItem();

		tileItems.remove(item);

		if (highlightItemLayer == item.getTile().getItemLayer())
		{
			highlightItemLayer = null;
		}
	}

	private void parseLevelUpWidget(WidgetInfo levelUpLevel)
	{
		Widget levelChild = client.getWidget(levelUpLevel);
		if (levelChild == null)
		{
			return;
		}

		Matcher m = LEVEL_UP_PATTERN.matcher(levelChild.getText());
		if (!m.matches())
		{
			return;
		}

		String skillName = m.group(1);
		String skillLevel = m.group(2);
		String combined = skillName + " " + skillLevel;

		if (!notifyLevels.contains(combined))
		{
			notificationsApi.sendNotification(
				"level",
				Map.of(
					"skill", skillName,
					"level", skillLevel
				)
			);
			notifyLevels.add(combined);
		}
	}

	public MenuOptionClicked menuAction(MenuOptionClicked menuOptionClicked, String option, String target, int identifier, MenuAction menuAction, int actionParam, int widgetId)
	{
		menuOptionClicked.setMenuOption(option);
		menuOptionClicked.setMenuTarget(target);
		menuOptionClicked.setId(identifier);
		menuOptionClicked.setMenuAction(menuAction);
		menuOptionClicked.setParam0(actionParam);
		menuOptionClicked.setParam1(widgetId);

		log.debug("Chin manager menu action: {}", menuOptionClicked);

		highlight(client, menuOptionClicked);

		return menuOptionClicked;
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
		return getObject(client, wp);
	}

	public int getLowestItemMatch(List<Integer> items)
	{
		ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		List<Integer> equipmentItems = new ArrayList<>();

		if (itemContainer != null)
		{
			for (final EquipmentInventorySlot slot : EquipmentInventorySlot.values())
			{
				int i = slot.getSlotIdx();
				Item item = itemContainer.getItem(i);

				if (item != null)
				{
					equipmentItems.add(item.getId());
				}
			}
		}

		for (int item : items)
		{
			if (client.getItemContainer(InventoryID.BANK) != null && hasBankItem(item, client))
			{
				return item;
			}
			else if (client.getItemContainer(InventoryID.BANK) == null && hasItem(item, client))
			{
				return item;
			}
			else if (client.getItemContainer(InventoryID.BANK) == null && equipmentItems.contains(item))
			{
				return item;
			}
		}

		return -1;
	}

	public static int runeOrRunepouch(Runes runes, Client client)
	{
		if (!hasItem(ItemID.RUNE_POUCH, client) && !hasBankInventoryItem(ItemID.RUNE_POUCH, client) && !hasBankItem(ItemID.RUNE_POUCH, client))
		{
			return runes.getItemId();
		}

		for (int i = 0; i < AMOUNT_VARBITS.length; i++)
		{
			Varbits amountVarbit = AMOUNT_VARBITS[i];

			int amount = client.getVar(amountVarbit);
			if (amount <= 0)
			{
				continue;
			}

			Varbits runeVarbit = RUNE_VARBITS[i];
			int runeId = client.getVar(runeVarbit);
			Runes rune = Runes.getRune(runeId);
			if (rune == null)
			{
				continue;
			}

			if (rune.getItemId() == runes.getItemId())
			{
				return ItemID.RUNE_POUCH;
			}
			else
			{
				switch (rune)
				{
					case MIST:
						if (runes == Runes.AIR || runes == Runes.WATER)
						{
							return ItemID.RUNE_POUCH;
						}
						break;
					case DUST:
						if (runes == Runes.AIR || runes == Runes.EARTH)
						{
							return ItemID.RUNE_POUCH;
						}
						break;
					case MUD:
						if (runes == Runes.EARTH || runes == Runes.WATER)
						{
							return ItemID.RUNE_POUCH;
						}
						break;
					case SMOKE:
						if (runes == Runes.AIR || runes == Runes.FIRE)
						{
							return ItemID.RUNE_POUCH;
						}
						break;
					case STEAM:
						if (runes == Runes.FIRE || runes == Runes.WATER)
						{
							return ItemID.RUNE_POUCH;
						}
						break;
					case LAVA:
						if (runes == Runes.EARTH || runes == Runes.FIRE)
						{
							return ItemID.RUNE_POUCH;
						}
						break;
				}
			}
		}

		return runes.getItemId();
	}
}