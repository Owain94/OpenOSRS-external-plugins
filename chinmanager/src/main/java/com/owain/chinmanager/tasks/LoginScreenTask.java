package com.owain.chinmanager.tasks;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerState;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chintasks.Task;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
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
		eventBus.register(this);
	}

	public void unsubscribe()
	{
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
	private void onGameTick(GameTick gameTick)
	{
		if (chinManager.getActivePlugins().isEmpty())
		{
			return;
		}

		ChinManagerPlugin.logout = false;

		Widget loginScreen = client.getWidget(WidgetInfo.LOGIN_CLICK_TO_PLAY_SCREEN);
		Widget playButtonText = client.getWidget(WidgetID.LOGIN_CLICK_TO_PLAY_GROUP_ID, 87);

		if (playButtonText != null && playButtonText.getText().equals("CLICK HERE TO PLAY"))
		{
			disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
		}
		else if (loginScreen == null)
		{
			if (client.getVar(VarClientInt.INVENTORY_TAB) != 3)
			{
				client.runScript(915, 3);
			}
			ChinManagerState.stateMachine.accept(ChinManagerStates.RESUME);
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		Widget playButton = client.getWidget(WidgetID.LOGIN_CLICK_TO_PLAY_GROUP_ID, 78);

		if (playButton == null)
		{
			return;
		}

		chinManagerPlugin.menuAction(
			menuOptionClicked,
			"Play",
			"",
			1,
			MenuAction.CC_OP,
			-1,
			playButton.getId()
		);
	}
}
