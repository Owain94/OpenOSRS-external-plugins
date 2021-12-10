package com.owain.chinmanager.tasks;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinmanager.magicnumbers.MagicNumberScripts;
import com.owain.chinmanager.magicnumbers.MagicNumberWidgets;
import com.owain.chintasks.Task;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class LoginScreenTask implements Task<Void>
{
	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	private final Client client;
	private final EventBus eventBus;

	private final List<Disposable> disposables = new ArrayList<>();

	@Inject
	LoginScreenTask(ChinManager chinManager, ChinManagerPlugin chinManagerPlugin, EventBus eventBus)
	{
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;
		this.client = chinManagerPlugin.getClient();
		this.eventBus = eventBus;
	}

	@Override
	public void routine(ObservableEmitter<Void> emitter)
	{
		if (chinManagerPlugin.getExecutorService() == null || chinManagerPlugin.getExecutorService().isShutdown() || chinManagerPlugin.getExecutorService().isTerminated())
		{
			chinManagerPlugin.setExecutorService(Executors.newSingleThreadExecutor());
		}

		eventBus.register(this);
	}

	public void unsubscribe()
	{
		chinManagerPlugin.getExecutorService().shutdownNow();
		eventBus.unregister(this);

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
		if (chinManager.getActiveSortedPlugins().isEmpty())
		{
			return;
		}

		ChinManagerPlugin.logout = false;

		Widget loginScreen = client.getWidget(WidgetInfo.LOGIN_CLICK_TO_PLAY_SCREEN);
		Widget playButtonText = client.getWidget(MagicNumberWidgets.LOGIN_SCREEN_PLAY_NOW_TEXT.getGroupId(), MagicNumberWidgets.LOGIN_SCREEN_PLAY_NOW_TEXT.getChildId());

		if (playButtonText != null && playButtonText.getText().equals("CLICK HERE TO PLAY"))
		{
			disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
		}
		else if (loginScreen == null)
		{
			if (client.getVar(VarClientInt.INVENTORY_TAB) != 3)
			{
				client.runScript(MagicNumberScripts.ACTIVE_TAB.getId(), 3);
			}
			chinManagerPlugin.transition(ChinManagerStates.RESUME);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		Widget playButton = client.getWidget(MagicNumberWidgets.LOGIN_SCREEN_PLAY_NOW.getGroupId(), MagicNumberWidgets.LOGIN_SCREEN_PLAY_NOW.getChildId());

		if (playButton == null)
		{
			return;
		}

		menuOptionClicked = chinManagerPlugin.menuAction(
			menuOptionClicked,
			"Play",
			"",
			1,
			MenuAction.CC_OP,
			-1,
			playButton.getId()
		);

		if (!menuOptionClicked.isConsumed() && menuOptionClicked.getMenuAction() == MenuAction.WALK && menuOptionClicked.getParam0() == 0 && menuOptionClicked.getParam1() == 0)
		{
			menuOptionClicked.consume();
		}
	}
}
