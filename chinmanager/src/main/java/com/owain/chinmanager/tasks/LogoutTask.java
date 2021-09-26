package com.owain.chinmanager.tasks;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerState;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinmanager.ui.plugins.options.OptionsConfig;
import com.owain.chintasks.Task;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.MenuAction;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;

@Slf4j
public class LogoutTask implements Task<Void>
{
	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	private final Client client;
	private final EventBus eventBus;
	private final OptionsConfig optionsConfig;
	private final List<Disposable> disposables = new ArrayList<>();
	private LogoutState logoutState;
	private int tikkie = 10;

	@Inject
	LogoutTask(ChinManager chinManager, ChinManagerPlugin chinManagerPlugin, EventBus eventBus, @NonNull ConfigManager configManager, Client client)
	{
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;
		this.client = client;
		this.eventBus = eventBus;
		this.optionsConfig = configManager.getConfig(OptionsConfig.class);
	}

	@Override
	public void routine(ObservableEmitter<Void> emitter)
	{
		if (chinManager.getActivePlugins().isEmpty() || chinManager.getActiveBreaks().isEmpty())
		{
			ChinManagerState.stateMachine.accept(ChinManagerStates.IDLE);
		}

		tikkie = 10;

		eventBus.register(this);
	}

	public void unsubscribe()
	{
		eventBus.unregister(this);
		tikkie = 10;

		logoutState = LogoutState.NONE;

		for (Disposable disposable : disposables)
		{
			if (!disposable.isDisposed())
			{
				disposable.dispose();
			}
		}
	}

	@Subscribe
	private void onGameTick(GameTick gameTick)
	{
		if (tikkie >= 10)
		{
			logoutState = LogoutState.LOGOUT;
			tikkie = 0;
		}
		else
		{
			tikkie++;
		}

		if (logoutState == LogoutState.LOGOUT)
		{
			Widget store = client.getWidget(125, 1);

			if (client.getItemContainer(InventoryID.BANK) != null)
			{
				logoutState = LogoutState.CLOSE_BANK;
				disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
			}
			else if (store != null && !store.isHidden())
			{
				logoutState = LogoutState.CLOSE_LEPRECHAUN_STORE;
				disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
			}
			else
			{
				logoutState = LogoutState.LOGOUT_TAB_SWITCH;
			}
		}
		else if (logoutState == LogoutState.LOGOUT_TAB_SWITCH)
		{
			// Logout tab
			if (client.getVar(VarClientInt.INVENTORY_TAB) != 10)
			{
				client.runScript(915, 10);
			}

			logoutState = LogoutState.LOGOUT_TAB;
		}
		else if (logoutState == LogoutState.LOGOUT_TAB)
		{
			Widget logoutButton = client.getWidget(182, 8);
			Widget logoutDoorButton = client.getWidget(69, 23);

			if (logoutButton != null || logoutDoorButton != null)
			{
				logoutState = LogoutState.LOGOUT_BUTTON;
			}
		}
		else if (logoutState == LogoutState.LOGOUT_BUTTON)
		{
			disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
		}
	}

	@Subscribe
	private void onMenuOptionClicked(@NonNull MenuOptionClicked menuOptionClicked)
	{
		if (logoutState == LogoutState.CLOSE_BANK)
		{
			Widget bankContainerChild = client.getWidget(12, 2);

			logoutState = LogoutState.LOGOUT;

			chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Close",
				"",
				1,
				MenuAction.CC_OP,
				11,
				bankContainerChild.getId()
			);
		}
		else if (logoutState == LogoutState.CLOSE_LEPRECHAUN_STORE)
		{
			Widget storeContainerChild = client.getWidget(125, 1);

			logoutState = LogoutState.LOGOUT;

			chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Close",
				"",
				1,
				MenuAction.CC_OP,
				11,
				storeContainerChild.getId()
			);
		}
		else if (logoutState == LogoutState.LOGOUT_BUTTON)
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

			logoutState = LogoutState.NONE;

			chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Logout",
				"",
				1,
				MenuAction.CC_OP,
				-1,
				param1
			);
		}
	}

	@Subscribe
	private void onGamestateChanged(GameStateChanged gameStateChanged)
	{
		ChinManagerPlugin.logout = false;

		if (optionsConfig.stopAfterBreaks() != 0 && chinManager.getAmountOfBreaks() >= optionsConfig.stopAfterBreaks())
		{
			for (Plugin plugin : Set.copyOf(chinManager.getActivePlugins()))
			{
				chinManager.stopPlugin(plugin);
			}

			chinManager.setCurrentlyActive(null);
			chinManager.setAmountOfBreaks(0);
		}

		ChinManagerState.stateMachine.accept(ChinManagerStates.IDLE);
	}

	enum LogoutState
	{
		NONE,
		CLOSE_BANK,
		CLOSE_LEPRECHAUN_STORE,
		LOGOUT,
		LOGOUT_TAB_SWITCH,
		LOGOUT_TAB,
		LOGOUT_BUTTON,
		LOGOUT_WAIT,

	}
}
