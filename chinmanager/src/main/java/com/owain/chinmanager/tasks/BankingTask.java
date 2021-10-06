package com.owain.chinmanager.tasks;

import static com.owain.automation.ContainerUtils.getBankInventoryItems;
import static com.owain.automation.ContainerUtils.getFirstBankInventoryItemsPos;
import static com.owain.automation.ContainerUtils.getFirstBankItemsPos;
import static com.owain.automation.ContainerUtils.hasAnyBankInventoryItem;
import static com.owain.automation.ContainerUtils.hasAnyBankItem;
import static com.owain.automation.ContainerUtils.hasBankInventoryItem;
import static com.owain.automation.ContainerUtils.hasBankInventoryItems;
import static com.owain.automation.ContainerUtils.hasBankItems;
import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import static com.owain.chinmanager.ChinManagerPlugin.AMULETS_OF_GLORY;
import static com.owain.chinmanager.ChinManagerPlugin.COMBAT_BRACELETS;
import static com.owain.chinmanager.ChinManagerPlugin.DIGSIDE_PENDANTS;
import static com.owain.chinmanager.ChinManagerPlugin.GAMES_NECKLACES;
import static com.owain.chinmanager.ChinManagerPlugin.RINGS_OF_DUELING;
import static com.owain.chinmanager.ChinManagerPlugin.RINGS_OF_WEALTH;
import static com.owain.chinmanager.ChinManagerPlugin.SKILLS_NECKLACES;
import static com.owain.chinmanager.ChinManagerState.stateMachine;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinmanager.Location;
import com.owain.chinmanager.ui.gear.Equipment;
import com.owain.chinmanager.ui.gear.EquipmentItem;
import com.owain.chinmanager.ui.teleports.TeleportsConfig;
import com.owain.chinmanager.ui.teleports.config.AmuletOfGlory;
import com.owain.chinmanager.ui.teleports.config.CombatBracelet;
import com.owain.chinmanager.ui.teleports.config.DigsitePendant;
import com.owain.chinmanager.ui.teleports.config.GamesNecklace;
import com.owain.chinmanager.ui.teleports.config.Poh;
import com.owain.chinmanager.ui.teleports.config.RingOfDueling;
import com.owain.chinmanager.ui.teleports.config.RingOfWealth;
import com.owain.chinmanager.ui.teleports.config.SkillsNecklace;
import com.owain.chinmanager.ui.teleports.config.XericsTalisman;
import static com.owain.chinmanager.utils.Plugins.sanitizedName;
import com.owain.chintasks.Task;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import static net.runelite.api.ItemID.*;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

public class BankingTask implements Task<Void>
{
	private static final Map<Integer, Integer> WORN_ITEMS = new HashMap<>()
	{{
		put(BOOTS_OF_LIGHTNESS_89, BOOTS_OF_LIGHTNESS);
		put(PENANCE_GLOVES_10554, PENANCE_GLOVES);

		put(GRACEFUL_HOOD_11851, GRACEFUL_HOOD);
		put(GRACEFUL_CAPE_11853, GRACEFUL_CAPE);
		put(GRACEFUL_TOP_11855, GRACEFUL_TOP);
		put(GRACEFUL_LEGS_11857, GRACEFUL_LEGS);
		put(GRACEFUL_GLOVES_11859, GRACEFUL_GLOVES);
		put(GRACEFUL_BOOTS_11861, GRACEFUL_BOOTS);
		put(GRACEFUL_HOOD_13580, GRACEFUL_HOOD_13579);
		put(GRACEFUL_CAPE_13582, GRACEFUL_CAPE_13581);
		put(GRACEFUL_TOP_13584, GRACEFUL_TOP_13583);
		put(GRACEFUL_LEGS_13586, GRACEFUL_LEGS_13585);
		put(GRACEFUL_GLOVES_13588, GRACEFUL_GLOVES_13587);
		put(GRACEFUL_BOOTS_13590, GRACEFUL_BOOTS_13589);
		put(GRACEFUL_HOOD_13592, GRACEFUL_HOOD_13591);
		put(GRACEFUL_CAPE_13594, GRACEFUL_CAPE_13593);
		put(GRACEFUL_TOP_13596, GRACEFUL_TOP_13595);
		put(GRACEFUL_LEGS_13598, GRACEFUL_LEGS_13597);
		put(GRACEFUL_GLOVES_13600, GRACEFUL_GLOVES_13599);
		put(GRACEFUL_BOOTS_13602, GRACEFUL_BOOTS_13601);
		put(GRACEFUL_HOOD_13604, GRACEFUL_HOOD_13603);
		put(GRACEFUL_CAPE_13606, GRACEFUL_CAPE_13605);
		put(GRACEFUL_TOP_13608, GRACEFUL_TOP_13607);
		put(GRACEFUL_LEGS_13610, GRACEFUL_LEGS_13609);
		put(GRACEFUL_GLOVES_13612, GRACEFUL_GLOVES_13611);
		put(GRACEFUL_BOOTS_13614, GRACEFUL_BOOTS_13613);
		put(GRACEFUL_HOOD_13616, GRACEFUL_HOOD_13615);
		put(GRACEFUL_CAPE_13618, GRACEFUL_CAPE_13617);
		put(GRACEFUL_TOP_13620, GRACEFUL_TOP_13619);
		put(GRACEFUL_LEGS_13622, GRACEFUL_LEGS_13621);
		put(GRACEFUL_GLOVES_13624, GRACEFUL_GLOVES_13623);
		put(GRACEFUL_BOOTS_13626, GRACEFUL_BOOTS_13625);
		put(GRACEFUL_HOOD_13628, GRACEFUL_HOOD_13627);
		put(GRACEFUL_CAPE_13630, GRACEFUL_CAPE_13629);
		put(GRACEFUL_TOP_13632, GRACEFUL_TOP_13631);
		put(GRACEFUL_LEGS_13634, GRACEFUL_LEGS_13633);
		put(GRACEFUL_GLOVES_13636, GRACEFUL_GLOVES_13635);
		put(GRACEFUL_BOOTS_13638, GRACEFUL_BOOTS_13637);
		put(GRACEFUL_HOOD_13668, GRACEFUL_HOOD_13667);
		put(GRACEFUL_CAPE_13670, GRACEFUL_CAPE_13669);
		put(GRACEFUL_TOP_13672, GRACEFUL_TOP_13671);
		put(GRACEFUL_LEGS_13674, GRACEFUL_LEGS_13673);
		put(GRACEFUL_GLOVES_13676, GRACEFUL_GLOVES_13675);
		put(GRACEFUL_BOOTS_13678, GRACEFUL_BOOTS_13677);
		put(GRACEFUL_HOOD_21063, GRACEFUL_HOOD_21061);
		put(GRACEFUL_CAPE_21066, GRACEFUL_CAPE_21064);
		put(GRACEFUL_TOP_21069, GRACEFUL_TOP_21067);
		put(GRACEFUL_LEGS_21072, GRACEFUL_LEGS_21070);
		put(GRACEFUL_GLOVES_21075, GRACEFUL_GLOVES_21073);
		put(GRACEFUL_BOOTS_21078, GRACEFUL_BOOTS_21076);
		put(GRACEFUL_HOOD_24745, GRACEFUL_HOOD_24743);
		put(GRACEFUL_CAPE_24748, GRACEFUL_CAPE_24746);
		put(GRACEFUL_TOP_24751, GRACEFUL_TOP_24749);
		put(GRACEFUL_LEGS_24754, GRACEFUL_LEGS_24752);
		put(GRACEFUL_GLOVES_24757, GRACEFUL_GLOVES_24755);
		put(GRACEFUL_BOOTS_24760, GRACEFUL_BOOTS_24758);

		put(MAX_CAPE_13342, MAX_CAPE);

		put(SPOTTED_CAPE_10073, SPOTTED_CAPE);
		put(SPOTTIER_CAPE_10074, SPOTTIER_CAPE);

		put(AGILITY_CAPET_13341, AGILITY_CAPET);
		put(AGILITY_CAPE_13340, AGILITY_CAPE);
	}};
	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	private final TeleportsConfig teleportsConfig;
	private final Client client;
	private final EventBus eventBus;
	private final List<Disposable> disposables = new ArrayList<>();
	Equipment equipmentSetup;
	private BankingState bankingState;
	private boolean gearDone = false;

	@Inject
	BankingTask(ChinManager chinManager, ChinManagerPlugin chinManagerPlugin, EventBus eventBus, ConfigManager configManager)
	{
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;
		this.teleportsConfig = configManager.getConfig(TeleportsConfig.class);
		this.client = chinManagerPlugin.getClient();
		this.eventBus = eventBus;
	}

	@Override
	public void routine(ObservableEmitter<Void> emitter)
	{
		bankingState = BankingState.NONE;
		gearDone = false;

		equipmentSetup = ChinManagerPlugin
			.getEquipmentList()
			.stream()
			.filter((setup) -> setup.getName().equals(sanitizedName(chinManager.bankingPlugin())))
			.findFirst()
			.orElse(null);

		if (equipmentSetup == null)
		{
			bankingState = BankingState.DONE;
		}

		eventBus.register(this);
	}

	public void unsubscribe()
	{
		eventBus.unregister(this);
		gearDone = false;

		bankingState = BankingState.NONE;

		equipmentSetup = null;

		chinManager.bankingDone();

		for (Disposable disposable : disposables)
		{
			if (!disposable.isDisposed())
			{
				disposable.dispose();
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		Widget bankTitleBar = client.getWidget(WidgetInfo.BANK_TITLE_BAR);

		if (bankTitleBar == null && client.getItemContainer(InventoryID.BANK) == null && bankingState != BankingState.WAIT_BANK)
		{
			if (ChinManagerPlugin.isAtBank(client))
			{
				bankingState = BankingState.CLICK_BANK;
				disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
			}
		}
		else if (client.getVarbitValue(6590) != 0)
		{
			bankingState = BankingState.QUANTITY;
			disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
		}
		else if (client.getVar(Varbits.CURRENT_BANK_TAB) != 0)
		{
			bankingState = BankingState.BANK_TAB;
			disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
		}
		else if (client.getVarbitValue(10079) != 0)
		{
			if (bankTitleBar != null)
			{
				if (!bankTitleBar.getText().equals("Bank settings menu"))
				{
					bankingState = BankingState.SETTINGS;
				}
				else
				{
					bankingState = BankingState.ITEM_OPTIONS;
				}

				disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
			}
		}
		else if (bankTitleBar != null && bankTitleBar.getText().equals("Bank settings menu"))
		{
			bankingState = BankingState.SETTINGS;
			disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
		}
		else if (bankingState == BankingState.NONE)
		{
			Collection<WidgetItem> bankInventoryItems = getBankInventoryItems(client);

			if (bankInventoryItems == null)
			{
				bankingState = BankingState.DONE;
				return;
			}

			if (bankInventoryItems
				.stream()
				.anyMatch((item) -> item.getId() != 6512))
			{
				bankingState = BankingState.DEPOSIT_ALL;
			}
			else
			{
				bankingState = BankingState.DETERMINE_STATE;
			}
		}
		else if (bankingState == BankingState.DETERMINE_STATE)
		{
			determineStates();
		}
		else if (bankingState == BankingState.DEPOSIT_ALL)
		{
			bankingState = BankingState.DEPOSIT_ALL_CLICK;
			disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
		}
		else if (bankingState == BankingState.SHOW_WORN_ITEMS ||
			bankingState == BankingState.HIDE_WORN_ITEMS)
		{
			disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
		}
		else if (bankingState == BankingState.DONE)
		{
			stateMachine.accept(ChinManagerStates.IDLE);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (bankingState == BankingState.CLICK_BANK)
		{
			NPC banker = ChinManagerPlugin.getBankNpc(client);
			TileObject bank = ChinManagerPlugin.getBankObject(client);

			if (bank != null)
			{
				int index = 0;
				for (String action : client.getObjectDefinition(bank.getId()).getActions())
				{
					if (action != null && (action.equalsIgnoreCase("bank") || action.equalsIgnoreCase("use")))
					{
						break;
					}

					index++;
				}

				bankingState = BankingState.WAIT_BANK;

				menuOptionClicked = chinManagerPlugin.menuAction(
					menuOptionClicked,
					"Bank",
					"",
					bank.getId(),
					MenuAction.of(MenuAction.GAME_OBJECT_FIRST_OPTION.getId() + index),
					ChinManagerPlugin.getLocation(bank).getX(),
					ChinManagerPlugin.getLocation(bank).getY()
				);
			}
			else if (banker != null)
			{
				int index = 0;
				for (String action : client.getNpcDefinition(banker.getId()).getActions())
				{
					if (action != null && action.equalsIgnoreCase("bank"))
					{
						break;
					}

					index++;
				}

				bankingState = BankingState.WAIT_BANK;

				menuOptionClicked = chinManagerPlugin.menuAction(
					menuOptionClicked,
					"Bank",
					"",
					banker.getIndex(),
					MenuAction.of(MenuAction.NPC_FIRST_OPTION.getId() + index),
					0,
					0
				);
			}
		}
		else if (bankingState == BankingState.QUANTITY)
		{
			Widget withdrawOne = client.getWidget(12, 27);

			if (withdrawOne == null)
			{
				menuOptionClicked.consume();
				bankingState = BankingState.NONE;
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Default quantity: 1",
				"",
				1,
				MenuAction.CC_OP,
				-1,
				withdrawOne.getId()
			);

			bankingState = BankingState.NONE;
		}
		else if (bankingState == BankingState.BANK_TAB)
		{
			Widget bankTabContainer = client.getWidget(WidgetInfo.BANK_TAB_CONTAINER);

			if (bankTabContainer == null)
			{
				menuOptionClicked.consume();
				bankingState = BankingState.NONE;
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"View all items",
				"",
				1,
				MenuAction.CC_OP,
				10,
				bankTabContainer.getId()
			);

			bankingState = BankingState.NONE;
		}
		else if (bankingState == BankingState.SETTINGS)
		{
			Widget settingsButton = client.getWidget(WidgetInfo.BANK_SETTINGS_BUTTON);

			if (settingsButton == null)
			{
				menuOptionClicked.consume();
				bankingState = BankingState.NONE;
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"menu",
				"",
				1,
				MenuAction.CC_OP,
				-1,
				settingsButton.getId()
			);

			bankingState = BankingState.NONE;
		}
		else if (bankingState == BankingState.ITEM_OPTIONS)
		{
			Widget inventoryOptions = client.getWidget(12, 50);

			if (inventoryOptions == null)
			{
				menuOptionClicked.consume();
				bankingState = BankingState.NONE;
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Show",
				"<col=ff9040>Inventory item options</col>",
				1,
				MenuAction.CC_OP,
				-1,
				inventoryOptions.getId()
			);

			bankingState = BankingState.NONE;
		}
		else if (bankingState == BankingState.DEPOSIT_ALL_CLICK)
		{
			Widget deposit = client.getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);

			bankingState = BankingState.DETERMINE_STATE;

			if (deposit == null)
			{
				menuOptionClicked.consume();
				bankingState = BankingState.NONE;
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Deposit inventory",
				"",
				1,
				MenuAction.CC_OP,
				-1,
				deposit.getId()
			);
		}
		else if (bankingState == BankingState.SHOW_WORN_ITEMS)
		{
			Widget equipmentButton = client.getWidget(WidgetInfo.BANK_EQUIPMENT_BUTTON);

			bankingState = BankingState.DETERMINE_STATE;

			if (equipmentButton == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Show worn items",
				"",
				1,
				MenuAction.CC_OP,
				-1,
				equipmentButton.getId()
			);
		}
		else if (bankingState == BankingState.BANK_HEAD)
		{
			Widget bank = client.getWidget(12, 75);

			bankingState = BankingState.DETERMINE_STATE;

			if (bank == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Bank",
				bank.getName(),
				2,
				MenuAction.CC_OP,
				-1,
				bank.getId()
			);
		}
		else if (bankingState == BankingState.BANK_CAPE)
		{
			Widget bank = client.getWidget(12, 76);

			bankingState = BankingState.DETERMINE_STATE;

			if (bank == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Bank",
				bank.getName(),
				2,
				MenuAction.CC_OP,
				-1,
				bank.getId()
			);
		}
		else if (bankingState == BankingState.BANK_AMULET)
		{
			Widget bank = client.getWidget(12, 77);

			bankingState = BankingState.DETERMINE_STATE;

			if (bank == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Bank",
				bank.getName(),
				2,
				MenuAction.CC_OP,
				-1,
				bank.getId()
			);
		}
		else if (bankingState == BankingState.BANK_AMMO)
		{
			Widget bank = client.getWidget(12, 85);

			bankingState = BankingState.DETERMINE_STATE;

			if (bank == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Bank",
				bank.getName(),
				2,
				MenuAction.CC_OP,
				-1,
				bank.getId()
			);
		}
		else if (bankingState == BankingState.BANK_WEAPON)
		{
			Widget bank = client.getWidget(12, 78);

			bankingState = BankingState.DETERMINE_STATE;

			if (bank == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Bank",
				bank.getName(),
				2,
				MenuAction.CC_OP,
				-1,
				bank.getId()
			);
		}
		else if (bankingState == BankingState.BANK_BODY)
		{
			Widget bank = client.getWidget(12, 79);

			bankingState = BankingState.DETERMINE_STATE;

			if (bank == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Bank",
				bank.getName(),
				2,
				MenuAction.CC_OP,
				-1,
				bank.getId()
			);
		}
		else if (bankingState == BankingState.BANK_SHIELD)
		{
			Widget bank = client.getWidget(12, 80);

			bankingState = BankingState.DETERMINE_STATE;

			if (bank == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Bank",
				bank.getName(),
				2,
				MenuAction.CC_OP,
				-1,
				bank.getId()
			);
		}
		else if (bankingState == BankingState.BANK_LEGS)
		{
			Widget bank = client.getWidget(12, 81);

			bankingState = BankingState.DETERMINE_STATE;

			if (bank == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Bank",
				bank.getName(),
				2,
				MenuAction.CC_OP,
				-1,
				bank.getId()
			);
		}
		else if (bankingState == BankingState.BANK_GLOVES)
		{
			Widget bank = client.getWidget(12, 82);

			bankingState = BankingState.DETERMINE_STATE;

			if (bank == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Bank",
				bank.getName(),
				2,
				MenuAction.CC_OP,
				-1,
				bank.getId()
			);
		}
		else if (bankingState == BankingState.BANK_BOOTS)
		{
			Widget bank = client.getWidget(12, 83);

			bankingState = BankingState.DETERMINE_STATE;

			if (bank == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Bank",
				bank.getName(),
				2,
				MenuAction.CC_OP,
				-1,
				bank.getId()
			);
		}
		else if (bankingState == BankingState.BANK_RING)
		{
			Widget bank = client.getWidget(12, 84);

			bankingState = BankingState.DETERMINE_STATE;

			if (bank == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Bank",
				bank.getName(),
				2,
				MenuAction.CC_OP,
				-1,
				bank.getId()
			);
		}
		else if (bankingState == BankingState.HIDE_WORN_ITEMS)
		{
			Widget equipmentButton = client.getWidget(WidgetInfo.BANK_EQUIPMENT_BUTTON);

			bankingState = BankingState.DETERMINE_STATE;

			if (equipmentButton == null)
			{
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Hide worn items",
				"",
				1,
				MenuAction.CC_OP,
				-1,
				equipmentButton.getId()
			);
		}
		else if (bankingState == BankingState.GET_HEAD)
		{
			Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(0);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Withdraw-1",
				"Withdraw-1",
				1,
				MenuAction.CC_OP,
				getFirstBankItemsPos(savedItemId, client),
				bankContainer.getId()
			);
		}
		else if (bankingState == BankingState.GET_CAPE)
		{
			Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(1);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Withdraw-1",
				"Withdraw-1",
				1,
				MenuAction.CC_OP,
				getFirstBankItemsPos(savedItemId, client),
				bankContainer.getId()
			);
		}
		else if (bankingState == BankingState.GET_AMULET)
		{
			Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(2);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Withdraw-1",
				"Withdraw-1",
				1,
				MenuAction.CC_OP,
				getFirstBankItemsPos(savedItemId, client),
				bankContainer.getId()
			);
		}
		else if (bankingState == BankingState.GET_WEAPON)
		{
			Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(3);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Withdraw-1",
				"Withdraw-1",
				1,
				MenuAction.CC_OP,
				getFirstBankItemsPos(savedItemId, client),
				bankContainer.getId()
			);
		}
		else if (bankingState == BankingState.GET_BODY)
		{
			Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(4);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Withdraw-1",
				"Withdraw-1",
				1,
				MenuAction.CC_OP,
				getFirstBankItemsPos(savedItemId, client),
				bankContainer.getId()
			);
		}
		else if (bankingState == BankingState.GET_SHIELD)
		{
			Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(5);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Withdraw-1",
				"Withdraw-1",
				1,
				MenuAction.CC_OP,
				getFirstBankItemsPos(savedItemId, client),
				bankContainer.getId()
			);
		}
		else if (bankingState == BankingState.GET_LEGS)
		{
			Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(7);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Withdraw-1",
				"Withdraw-1",
				1,
				MenuAction.CC_OP,
				getFirstBankItemsPos(savedItemId, client),
				bankContainer.getId()
			);
		}
		else if (bankingState == BankingState.GET_GLOVES)
		{
			Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(9);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Withdraw-1",
				"Withdraw-1",
				1,
				MenuAction.CC_OP,
				getFirstBankItemsPos(savedItemId, client),
				bankContainer.getId()
			);
		}
		else if (bankingState == BankingState.GET_BOOTS)
		{
			Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(10);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Withdraw-1",
				"Withdraw-1",
				1,
				MenuAction.CC_OP,
				getFirstBankItemsPos(savedItemId, client),
				bankContainer.getId()
			);
		}
		else if (bankingState == BankingState.GET_RING)
		{
			Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(12);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Withdraw-1",
				"Withdraw-1",
				1,
				MenuAction.CC_OP,
				getFirstBankItemsPos(savedItemId, client),
				bankContainer.getId()
			);
		}
		else if (bankingState == BankingState.GET_AMMO)
		{
			Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(13);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Withdraw-1",
				"Withdraw-1",
				1,
				MenuAction.CC_OP,
				getFirstBankItemsPos(savedItemId, client),
				bankContainer.getId()
			);
		}
		else if (bankingState == BankingState.EQUIP_HEAD)
		{
			Widget bankInventoryContainer = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankInventoryContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(0);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Wear",
				"<col=ff9040>" + savedItem.getName() + "</col>",
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getFirstBankInventoryItemsPos(savedItemId, client),
				bankInventoryContainer.getId()
			);
		}
		else if (bankingState == BankingState.EQUIP_CAPE)
		{
			Widget bankInventoryContainer = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankInventoryContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(1);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Wear",
				"<col=ff9040>" + savedItem.getName() + "</col>",
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getFirstBankInventoryItemsPos(savedItemId, client),
				bankInventoryContainer.getId()
			);
		}
		else if (bankingState == BankingState.EQUIP_AMULET)
		{
			Widget bankInventoryContainer = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankInventoryContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(2);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Wear",
				"<col=ff9040>" + savedItem.getName() + "</col>",
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getFirstBankInventoryItemsPos(savedItemId, client),
				bankInventoryContainer.getId()
			);
		}
		else if (bankingState == BankingState.EQUIP_WEAPON)
		{
			Widget bankInventoryContainer = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankInventoryContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(3);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Wear",
				"<col=ff9040>" + savedItem.getName() + "</col>",
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getFirstBankInventoryItemsPos(savedItemId, client),
				bankInventoryContainer.getId()
			);
		}
		else if (bankingState == BankingState.EQUIP_BODY)
		{
			Widget bankInventoryContainer = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankInventoryContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(4);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Wear",
				"<col=ff9040>" + savedItem.getName() + "</col>",
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getFirstBankInventoryItemsPos(savedItemId, client),
				bankInventoryContainer.getId()
			);
		}
		else if (bankingState == BankingState.EQUIP_SHIELD)
		{
			Widget bankInventoryContainer = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankInventoryContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(5);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Wear",
				"<col=ff9040>" + savedItem.getName() + "</col>",
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getFirstBankInventoryItemsPos(savedItemId, client),
				bankInventoryContainer.getId()
			);
		}
		else if (bankingState == BankingState.EQUIP_LEGS)
		{
			Widget bankInventoryContainer = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankInventoryContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(7);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Wear",
				"<col=ff9040>" + savedItem.getName() + "</col>",
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getFirstBankInventoryItemsPos(savedItemId, client),
				bankInventoryContainer.getId()
			);
		}
		else if (bankingState == BankingState.EQUIP_GLOVES)
		{
			Widget bankInventoryContainer = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankInventoryContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(9);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Wear",
				"<col=ff9040>" + savedItem.getName() + "</col>",
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getFirstBankInventoryItemsPos(savedItemId, client),
				bankInventoryContainer.getId()
			);
		}
		else if (bankingState == BankingState.EQUIP_BOOTS)
		{
			Widget bankInventoryContainer = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankInventoryContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(10);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Wear",
				"<col=ff9040>" + savedItem.getName() + "</col>",
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getFirstBankInventoryItemsPos(savedItemId, client),
				bankInventoryContainer.getId()
			);
		}
		else if (bankingState == BankingState.EQUIP_RING)
		{
			Widget bankInventoryContainer = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankInventoryContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(12);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Wear",
				"<col=ff9040>" + savedItem.getName() + "</col>",
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getFirstBankInventoryItemsPos(savedItemId, client),
				bankInventoryContainer.getId()
			);
		}
		else if (bankingState == BankingState.EQUIP_AMMO)
		{
			Widget bankInventoryContainer = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);

			bankingState = BankingState.DETERMINE_STATE;

			if (bankInventoryContainer == null)
			{
				menuOptionClicked.consume();
				return;
			}

			EquipmentItem savedItem = equipmentSetup.getEquipment().get(13);
			int savedItemId = WORN_ITEMS.getOrDefault(savedItem.getId(), savedItem.getId());

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Wear",
				"<col=ff9040>" + savedItem.getName() + "</col>",
				9,
				MenuAction.CC_OP_LOW_PRIORITY,
				getFirstBankInventoryItemsPos(savedItemId, client),
				bankInventoryContainer.getId()
			);
		}
		else if (bankingState == BankingState.FETCH_EXTRA)
		{
			bankingState = BankingState.DETERMINE_STATE;

			Location location = chinManager.getStartLocation(chinManager.bankingPlugin());

			if (needRingOfDueling(location) ||
				needGamesNecklace(location) ||
				needCombatBracelet(location) ||
				needSkillsNecklace(location) ||
				needRingOfWealth(location) ||
				needAmuletOfGlory(location) ||
				needXericsTalisman(location) ||
				needDigsitePendant(location))
			{
				Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

				bankingState = BankingState.DETERMINE_STATE;

				if (bankContainer == null)
				{
					menuOptionClicked.consume();
					return;
				}

				menuOptionClicked = chinManagerPlugin.menuAction(
					menuOptionClicked,
					"Withdraw-1",
					"Withdraw-1",
					1,
					MenuAction.CC_OP,
					getFirstBankItemsPos(chinManagerPlugin.getLowestItemMatch(getTeleportJewellery(location)), client),
					bankContainer.getId()
				);
			}
			else if (needPohTeleport(location))
			{
				List<Integer> items = new ArrayList<>();

				if (teleportsConfig.pohTeleport() == Poh.TELEPORT_TABLET)
				{
					items.add(ItemID.TELEPORT_TO_HOUSE);
				}
				else if (teleportsConfig.pohTeleport() == Poh.RUNES)
				{
					items.add(ItemID.LAW_RUNE);
					items.add(ItemID.AIR_RUNE);
					items.add(ItemID.EARTH_RUNE);
				}
				else if (teleportsConfig.pohTeleport() == Poh.CONSTRUCTION_CAPE)
				{
					items.add(
						chinManagerPlugin.getLowestItemMatch(
							ChinManagerPlugin.CONSTRUCT_CAPE
						)
					);
				}

				for (int item : items)
				{
					if (hasBankInventoryItem(item, client))
					{
						continue;
					}

					Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

					bankingState = BankingState.DETERMINE_STATE;

					if (bankContainer == null)
					{
						menuOptionClicked.consume();
						return;
					}

					chinManagerPlugin.menuAction(
						menuOptionClicked,
						"Withdraw-1",
						"Withdraw-1",
						1,
						MenuAction.CC_OP,
						getFirstBankItemsPos(item, client),
						bankContainer.getId()
					);

					return;
				}

				menuOptionClicked.consume();
			}
		}
		else
		{
			menuOptionClicked.consume();
		}

		if (!menuOptionClicked.isConsumed() && menuOptionClicked.getMenuAction() == MenuAction.WALK && menuOptionClicked.getParam0() == 0 && menuOptionClicked.getParam1() == 0)
		{
			menuOptionClicked.consume();
		}
	}

	private void determineStates()
	{
		List<EquipmentItem> equipmentItems = equipmentSetup.getEquipment();
		ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);

		boolean geartab = false;

		if (equipmentContainer == null)
		{
			geartab = true;
		}
		else
		{
			for (final EquipmentInventorySlot slot : EquipmentInventorySlot.values())
			{
				int i = slot.getSlotIdx();

				if (equipmentItems
					.get(i)
					.getId() == -2 && equipmentContainer.getItem(i) != null)
				{
					geartab = true;
				}
			}
		}

		if (!gearDone && geartab && client.getWidget(WidgetInfo.BANK_TITLE_BAR) != null && !client.getWidget(WidgetInfo.BANK_TITLE_BAR).getText().contains("character"))
		{
			bankingState = BankingState.SHOW_WORN_ITEMS;
			return;
		}
		else if (!gearDone && geartab)
		{
			for (final EquipmentInventorySlot slot : EquipmentInventorySlot.values())
			{
				int i = slot.getSlotIdx();
				Item item;

				if (equipmentContainer == null)
				{
					item = null;
				}
				else
				{
					item = equipmentContainer.getItem(i);
				}

				if (equipmentItems
					.get(i)
					.getId() == -2 && item != null)
				{
					switch (i)
					{
						case 0:
							bankingState = BankingState.BANK_HEAD;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
							return;
						case 1:
							bankingState = BankingState.BANK_CAPE;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
							return;
						case 2:
							bankingState = BankingState.BANK_AMULET;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
							return;
						case 3:
							bankingState = BankingState.BANK_WEAPON;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
							return;
						case 4:
							bankingState = BankingState.BANK_BODY;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
							return;
						case 5:
							bankingState = BankingState.BANK_SHIELD;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
							return;
						case 7:
							bankingState = BankingState.BANK_LEGS;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
							return;
						case 9:
							bankingState = BankingState.BANK_GLOVES;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
							return;
						case 10:
							bankingState = BankingState.BANK_BOOTS;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
							return;
						case 12:
							bankingState = BankingState.BANK_RING;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
							return;
						case 13:
							bankingState = BankingState.BANK_AMMO;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
							return;
					}
				}
			}

			gearDone = true;
		}

		if (client.getWidget(WidgetInfo.BANK_TITLE_BAR) != null && client.getWidget(WidgetInfo.BANK_TITLE_BAR).getText().contains("character"))
		{
			bankingState = BankingState.HIDE_WORN_ITEMS;
		}
		else if (client.getWidget(WidgetInfo.BANK_TITLE_BAR) != null && !client.getWidget(WidgetInfo.BANK_TITLE_BAR).getText().contains("character"))
		{
			for (final EquipmentInventorySlot slot : EquipmentInventorySlot.values())
			{
				int i = slot.getSlotIdx();
				int savedItem = equipmentItems
					.get(i)
					.getId();
				savedItem = WORN_ITEMS.getOrDefault(savedItem, savedItem);

				Item equippedItem;

				if (equipmentContainer == null)
				{
					equippedItem = null;
				}
				else
				{
					equippedItem = equipmentContainer.getItem(i);
				}

				if (savedItem != -1 && equippedItem == null || (equippedItem != null && WORN_ITEMS.getOrDefault(equippedItem.getId(), equippedItem.getId()) != savedItem))
				{
					boolean equip = false;
					boolean grab = false;

					if (hasAnyBankInventoryItem(savedItem, client))
					{
						equip = true;
					}
					else if (hasAnyBankItem(savedItem, client))
					{
						grab = true;
					}

					switch (i)
					{
						case 0:
							if (equip)
							{
								bankingState = BankingState.EQUIP_HEAD;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
							else if (grab)
							{
								bankingState = BankingState.GET_HEAD;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
						case 1:
							if (equip)
							{
								bankingState = BankingState.EQUIP_CAPE;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
							else if (grab)
							{
								bankingState = BankingState.GET_CAPE;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
						case 2:
							if (equip)
							{
								bankingState = BankingState.EQUIP_AMULET;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
							else if (grab)
							{
								bankingState = BankingState.GET_AMULET;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
						case 3:
							if (equip)
							{
								bankingState = BankingState.EQUIP_WEAPON;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
							else if (grab)
							{
								bankingState = BankingState.GET_WEAPON;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
						case 4:
							if (equip)
							{
								bankingState = BankingState.EQUIP_BODY;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
							else if (grab)
							{
								bankingState = BankingState.GET_BODY;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
						case 5:
							if (equip)
							{
								bankingState = BankingState.EQUIP_SHIELD;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
							else if (grab)
							{
								bankingState = BankingState.GET_SHIELD;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
						case 7:
							if (equip)
							{
								bankingState = BankingState.EQUIP_LEGS;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
							else if (grab)
							{
								bankingState = BankingState.GET_LEGS;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
						case 9:
							if (equip)
							{
								bankingState = BankingState.EQUIP_GLOVES;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
							else if (grab)
							{
								bankingState = BankingState.GET_GLOVES;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
						case 10:
							if (equip)
							{
								bankingState = BankingState.EQUIP_BOOTS;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
							else if (grab)
							{
								bankingState = BankingState.GET_BOOTS;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
						case 12:
							if (equip)
							{
								bankingState = BankingState.EQUIP_RING;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
							else if (grab)
							{
								bankingState = BankingState.GET_RING;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
						case 13:
							if (equip)
							{
								bankingState = BankingState.EQUIP_AMMO;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
							else if (grab)
							{
								bankingState = BankingState.GET_AMMO;
								disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
								return;
							}
					}
				}
			}

			if (chinManager.bankingPlugin() != chinManager.getCurrentlyActive())
			{
				if (!chinManager.getPluginConfig().get(chinManager.bankingPlugin()).get("startLocation").equals(chinManager.getPluginConfig().get(chinManager.getCurrentlyActive()).get("startLocation")))
				{
					Location location = chinManager.getStartLocation(chinManager.bankingPlugin());

					if (location == null)
					{
						bankingState = BankingState.DONE;
					}
					else if (needRingOfDueling(location) ||
						needGamesNecklace(location) ||
						needCombatBracelet(location) ||
						needSkillsNecklace(location) ||
						needRingOfWealth(location) ||
						needAmuletOfGlory(location) ||
						needXericsTalisman(location) ||
						needDigsitePendant(location))
					{
						if (hasAnyBankInventoryItem(getTeleportJewellery(location), client))
						{
							bankingState = BankingState.DONE;
						}
						else if (chinManagerPlugin.getLowestItemMatch(getTeleportJewellery(location)) != -1)
						{
							bankingState = BankingState.FETCH_EXTRA;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());

							return;
						}
					}
					else if (needPohTeleport(location))
					{
						List<Integer> items = new ArrayList<>();

						if (teleportsConfig.pohTeleport() == Poh.TELEPORT_TABLET)
						{
							items.add(ItemID.TELEPORT_TO_HOUSE);
						}
						else if (teleportsConfig.pohTeleport() == Poh.RUNES)
						{
							items.add(ItemID.LAW_RUNE);
							items.add(ItemID.AIR_RUNE);
							items.add(ItemID.EARTH_RUNE);
						}
						else if (teleportsConfig.pohTeleport() == Poh.CONSTRUCTION_CAPE)
						{
							items.add(
								chinManagerPlugin.getLowestItemMatch(
									ChinManagerPlugin.CONSTRUCT_CAPE
								)
							);
						}

						if (hasBankInventoryItems(items, client))
						{
							bankingState = BankingState.DONE;
						}
						else if (hasBankItems(items, client))
						{
							bankingState = BankingState.FETCH_EXTRA;
							disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());

							return;
						}
					}
				}
			}

			bankingState = BankingState.DONE;
		}
	}

	private boolean needRingOfDueling(Location location)
	{
		switch (location)
		{
			case DUEL_ARENA:
			case CASTLE_WARS:
			case FEROX_ENCLAVE:
				return teleportsConfig.ringOfDuelingTeleport() != RingOfDueling.POH;

			default:
				return false;
		}
	}

	private boolean needGamesNecklace(Location location)
	{
		switch (location)
		{
			case BURTHORPE:
			case BARBARIAN_OUTPOST:
			case CORPOREAL_BEAST:
			case CHASM_OF_TEARS:
			case WINTERTODT:
				return teleportsConfig.gamesNecklaceTeleport() != GamesNecklace.POH;

			default:
				return false;
		}
	}

	private boolean needCombatBracelet(Location location)
	{
		switch (location)
		{
			case WARRIORS_GUILD:
			case CHAMPIONS_GUILD:
			case EDGEVILLE_MONASTERY:
			case RANGING_GUILD:
				return teleportsConfig.combatBraceletTeleport() != CombatBracelet.POH;

			default:
				return false;
		}
	}

	private boolean needSkillsNecklace(Location location)
	{
		switch (location)
		{
			case FISHING_GUILD:
			case MINING_GUILD:
			case CRAFTING_GUILD:
			case COOKS_GUILD:
			case WOODCUTTING_GUILD:
			case FARMING_GUILD:
				return teleportsConfig.skillsNecklaceTeleport() != SkillsNecklace.POH;

			default:
				return false;
		}
	}

	private boolean needRingOfWealth(Location location)
	{
		switch (location)
		{
			case MISCELLANIA:
			case GRAND_EXCHANGE:
			case FALADOR_PARK:
			case DONDAKANS_ROCK:
				return teleportsConfig.ringOfWealthTeleport() != RingOfWealth.POH;

			default:
				return false;
		}
	}

	private boolean needAmuletOfGlory(Location location)
	{
		switch (location)
		{
			case EDGEVILLE:
			case KARAMJA:
			case DRAYNOR_VILLAGE:
			case AL_KHARID:
				return teleportsConfig.amuletOfGloryTeleport() != AmuletOfGlory.POH;

			default:
				return false;
		}
	}

	private boolean needXericsTalisman(Location location)
	{
		switch (location)
		{
			case XERICS_LOOKOUT:
			case XERICS_GLADE:
			case XERICS_INFERNO:
			case XERICS_HEART:
			case XERICS_HONOUR:
				return teleportsConfig.xericsTalismanTeleport() != XericsTalisman.POH;

			default:
				return false;
		}
	}

	private boolean needDigsitePendant(Location location)
	{
		switch (location)
		{
			case DIGSITE:
			case FOSSIL_ISLAND:
				return teleportsConfig.digsitePendantTeleport() != DigsitePendant.POH;

			default:
				return false;
		}
	}

	private boolean needPohTeleport(Location location)
	{
		switch (location)
		{
			case DUEL_ARENA:
			case CASTLE_WARS:
			case FEROX_ENCLAVE:
				return teleportsConfig.ringOfDuelingTeleport() == RingOfDueling.POH;

			case BURTHORPE:
			case BARBARIAN_OUTPOST:
			case CORPOREAL_BEAST:
			case CHASM_OF_TEARS:
			case WINTERTODT:
				return teleportsConfig.gamesNecklaceTeleport() == GamesNecklace.POH;

			case WARRIORS_GUILD:
			case CHAMPIONS_GUILD:
			case EDGEVILLE_MONASTERY:
			case RANGING_GUILD:
				return teleportsConfig.combatBraceletTeleport() == CombatBracelet.POH;

			case FISHING_GUILD:
			case MINING_GUILD:
			case CRAFTING_GUILD:
			case COOKS_GUILD:
			case WOODCUTTING_GUILD:
			case FARMING_GUILD:
				return teleportsConfig.skillsNecklaceTeleport() == SkillsNecklace.POH;

			case MISCELLANIA:
			case GRAND_EXCHANGE:
			case FALADOR_PARK:
			case DONDAKANS_ROCK:
				return teleportsConfig.ringOfWealthTeleport() == RingOfWealth.POH;

			case EDGEVILLE:
			case KARAMJA:
			case DRAYNOR_VILLAGE:
			case AL_KHARID:
				return teleportsConfig.amuletOfGloryTeleport() == AmuletOfGlory.POH;

			case XERICS_LOOKOUT:
			case XERICS_GLADE:
			case XERICS_INFERNO:
			case XERICS_HEART:
			case XERICS_HONOUR:
				return teleportsConfig.xericsTalismanTeleport() == XericsTalisman.POH;

			case DIGSITE:
			case FOSSIL_ISLAND:
				return teleportsConfig.digsitePendantTeleport() == DigsitePendant.POH;

			default:
				return false;
		}
	}

	private List<Integer> getTeleportJewellery(Location location)
	{
		switch (location)
		{
			case DUEL_ARENA:
			case CASTLE_WARS:
			case FEROX_ENCLAVE:
				return RINGS_OF_DUELING;

			case BURTHORPE:
			case BARBARIAN_OUTPOST:
			case CORPOREAL_BEAST:
			case CHASM_OF_TEARS:
			case WINTERTODT:
				return GAMES_NECKLACES;

			case WARRIORS_GUILD:
			case CHAMPIONS_GUILD:
			case EDGEVILLE_MONASTERY:
			case RANGING_GUILD:
				return COMBAT_BRACELETS;

			case FISHING_GUILD:
			case MINING_GUILD:
			case CRAFTING_GUILD:
			case COOKS_GUILD:
			case WOODCUTTING_GUILD:
			case FARMING_GUILD:
				return SKILLS_NECKLACES;

			case MISCELLANIA:
			case GRAND_EXCHANGE:
			case FALADOR_PARK:
			case DONDAKANS_ROCK:
				return RINGS_OF_WEALTH;

			case EDGEVILLE:
			case KARAMJA:
			case DRAYNOR_VILLAGE:
			case AL_KHARID:
				return AMULETS_OF_GLORY;

			case XERICS_LOOKOUT:
			case XERICS_GLADE:
			case XERICS_INFERNO:
			case XERICS_HEART:
			case XERICS_HONOUR:
				return ChinManagerPlugin.XERICS_TALISMAN;

			case DIGSITE:
			case FOSSIL_ISLAND:
				return DIGSIDE_PENDANTS;

			default:
				return Collections.emptyList();
		}
	}

	enum BankingState
	{
		NONE,
		CLICK_BANK,
		WAIT_BANK,
		QUANTITY,
		BANK_TAB,
		SETTINGS,
		ITEM_OPTIONS,
		DEPOSIT_ALL,
		DEPOSIT_ALL_CLICK,
		SHOW_WORN_ITEMS,
		DETERMINE_STATE,
		BANK_HEAD,
		BANK_CAPE,
		BANK_AMULET,
		BANK_AMMO,
		BANK_WEAPON,
		BANK_BODY,
		BANK_SHIELD,
		BANK_LEGS,
		BANK_GLOVES,
		BANK_BOOTS,
		BANK_RING,
		HIDE_WORN_ITEMS,
		GET_HEAD,
		GET_CAPE,
		GET_AMULET,
		GET_AMMO,
		GET_WEAPON,
		GET_BODY,
		GET_SHIELD,
		GET_LEGS,
		GET_GLOVES,
		GET_BOOTS,
		GET_RING,
		EQUIP_HEAD,
		EQUIP_CAPE,
		EQUIP_AMULET,
		EQUIP_AMMO,
		EQUIP_WEAPON,
		EQUIP_BODY,
		EQUIP_SHIELD,
		EQUIP_LEGS,
		EQUIP_GLOVES,
		EQUIP_BOOTS,
		EQUIP_RING,
		FETCH_EXTRA,
		DONE,
	}
}
