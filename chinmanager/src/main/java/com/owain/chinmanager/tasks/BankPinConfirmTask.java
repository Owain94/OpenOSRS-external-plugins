package com.owain.chinmanager.tasks;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerStates;
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
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class BankPinConfirmTask implements Task<Void>
{
	enum BankPinConfirm
	{
		WAIT,
		CONFIRM,
	}

	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	private final Client client;
	private final EventBus eventBus;

	private BankPinConfirm bankPinConfirm;
	private int tikkie = 0;

	private final List<Disposable> disposables = new ArrayList<>();

	@Inject
	BankPinConfirmTask(ChinManager chinManager, ChinManagerPlugin chinManagerPlugin, EventBus eventBus)
	{
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;
		this.client = chinManagerPlugin.getClient();
		this.eventBus = eventBus;
	}

	@Override
	public void routine(ObservableEmitter<Void> emitter)
	{
		tikkie = 0;
		bankPinConfirm = BankPinConfirm.CONFIRM;

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
		tikkie = 0;
		bankPinConfirm = BankPinConfirm.CONFIRM;

		for (Disposable disposable : disposables)
		{
			if (!disposable.isDisposed())
			{
				disposable.dispose();
			}
		}
	}

	@Subscribe(priority = -99)
	public void onGameTick(GameTick gameTick)
	{
		if (chinManager.getActiveSortedPlugins().isEmpty())
		{
			return;
		}

		if (tikkie >= 5)
		{
			bankPinConfirm = BankPinConfirm.CONFIRM;

			tikkie = 0;
		}
		else
		{
			tikkie++;
		}

		Widget bankPinConfirmWidget = client.getWidget(MagicNumberWidgets.BANK_PIN_CONFIRM_BUTTON.getGroupId(), MagicNumberWidgets.BANK_PIN_CONFIRM_BUTTON.getChildId());

		if (bankPinConfirm == BankPinConfirm.CONFIRM)
		{
			if (bankPinConfirmWidget != null && bankPinConfirmWidget.getText().contains("I want this PIN"))
			{
				disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
			}
		}

		if (bankPinConfirmWidget == null)
		{
			chinManagerPlugin.transition(ChinManagerStates.IDLE);
		}
	}

	@Subscribe(priority = -99)
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (bankPinConfirm == BankPinConfirm.CONFIRM)
		{
			Widget bankPinConfirmWidget = client.getWidget(MagicNumberWidgets.BANK_PIN_CONFIRM_BUTTON.getGroupId(), MagicNumberWidgets.BANK_PIN_CONFIRM_BUTTON.getChildId());

			if (bankPinConfirmWidget == null)
			{
				menuOptionClicked.consume();
				return;
			}

			bankPinConfirm = BankPinConfirm.WAIT;

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Continue",
				"",
				0,
				MenuAction.WIDGET_CONTINUE,
				-1,
				bankPinConfirmWidget.getId()
			);

			tikkie = 0;
		}

		if (!menuOptionClicked.isConsumed() && menuOptionClicked.getMenuAction() == MenuAction.WALK && menuOptionClicked.getParam0() == 0 && menuOptionClicked.getParam1() == 0)
		{
			menuOptionClicked.consume();
		}
	}
}
