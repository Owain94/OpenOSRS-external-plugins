package com.owain.chinmanager.tasks;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerStates;
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
import net.runelite.api.widgets.WidgetInfo;
import static net.runelite.api.widgets.WidgetInfo.BANK_PIN_INSTRUCTION_TEXT;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class BankPinTask implements Task<Void>
{
	enum BankPinState
	{
		NONE,
		WAIT,

		DIGIT,

		DONE,
	}

	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	private final Client client;
	private final ConfigManager configManager;
	private final EventBus eventBus;

	private final List<Disposable> disposables = new ArrayList<>();

	private BankPinState bankPinState;
	private int tikkie = 5;
	private String digit;
	private String instruction;

	@Inject
	BankPinTask(ChinManager chinManager, ChinManagerPlugin chinManagerPlugin, EventBus eventBus)
	{
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;
		this.client = chinManagerPlugin.getClient();
		this.configManager = chinManagerPlugin.getConfigManager();
		this.eventBus = eventBus;
	}

	@Override
	public void routine(ObservableEmitter<Void> emitter)
	{
		bankPinState = BankPinState.NONE;
		tikkie = 5;
		digit = null;
		instruction = null;

		if (getBankPin() == null)
		{
			chinManager.addWarning("Manager: Bank pin", "Bank pin is not set / profile data not unlocked");
			chinManagerPlugin.transition(ChinManagerStates.IDLE);

			return;
		}

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

		bankPinState = BankPinState.NONE;
		tikkie = 5;
		digit = null;
		instruction = null;

		for (Disposable disposable : disposables)
		{
			if (!disposable.isDisposed())
			{
				disposable.dispose();
			}
		}
	}

	private String getBankPin()
	{
		boolean manual = Boolean.parseBoolean(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection"));

		String bankpin;

		if (manual)
		{
			bankpin = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-pin");
		}
		else if (ChinManagerPlugin.getProfileData() != null)
		{
			String account = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-profiles-account");
			bankpin = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-profiles-pin-" + account);
		}
		else
		{
			return null;
		}

		if (bankpin.length() != 4)
		{
			return null;
		}

		return bankpin;
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (tikkie >= 5)
		{
			tikkie = 0;
			bankPinState = BankPinState.NONE;
		}

		Widget instructionTextWidget = client.getWidget(BANK_PIN_INSTRUCTION_TEXT);
		if (client.getWidget(WidgetInfo.BANK_PIN_CONTAINER) == null || instructionTextWidget == null || getBankPin() == null)
		{
			chinManagerPlugin.transition(ChinManagerStates.IDLE);
			return;
		}

		if (instruction == null || !instruction.equals(instructionTextWidget.getText()))
		{
			instruction = instructionTextWidget.getText();
			bankPinState = BankPinState.NONE;
		}

		if (bankPinState == BankPinState.NONE || instruction == null)
		{
			switch (instructionTextWidget.getText())
			{
				case "First click the FIRST digit.":
					digit = String.valueOf(getBankPin().charAt(0));
					bankPinState = BankPinState.DIGIT;
					disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
					break;
				case "Now click the SECOND digit.":
					digit = String.valueOf(getBankPin().charAt(1));
					bankPinState = BankPinState.DIGIT;
					disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
					break;
				case "Time for the THIRD digit.":
					digit = String.valueOf(getBankPin().charAt(2));
					bankPinState = BankPinState.DIGIT;
					disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
					break;
				case "Finally, the FOURTH digit.":
					digit = String.valueOf(getBankPin().charAt(3));
					bankPinState = BankPinState.DIGIT;
					disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new ClickTask(chinManagerPlugin)).ignoreElements().subscribe());
					break;
			}
		}

		tikkie += 1;
	}

	public Widget getDigitWidget()
	{
		List<WidgetInfo> digitWidgets = List.of(
			WidgetInfo.BANK_PIN_1,
			WidgetInfo.BANK_PIN_2,
			WidgetInfo.BANK_PIN_3,
			WidgetInfo.BANK_PIN_4,
			WidgetInfo.BANK_PIN_5,
			WidgetInfo.BANK_PIN_6,
			WidgetInfo.BANK_PIN_7,
			WidgetInfo.BANK_PIN_8,
			WidgetInfo.BANK_PIN_9,
			WidgetInfo.BANK_PIN_10
		);

		for (WidgetInfo digitWidgetInfo : digitWidgets)
		{
			Widget digitWidget = client.getWidget(digitWidgetInfo);

			if (digitWidget == null)
			{
				continue;
			}

			for (Widget dynamicChildren : digitWidget.getDynamicChildren())
			{
				if (dynamicChildren.getText().equals(digit))
				{
					return digitWidget;
				}
			}
		}

		return null;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (digit == null)
		{
			instruction = null;
			menuOptionClicked.consume();
			return;
		}

		if (bankPinState == BankPinState.DIGIT)
		{
			Widget digitWidget = getDigitWidget();

			if (digitWidget == null)
			{
				instruction = null;
				menuOptionClicked.consume();
				return;
			}

			menuOptionClicked = chinManagerPlugin.menuAction(
				menuOptionClicked,
				"Select",
				"",
				1,
				MenuAction.CC_OP,
				0,
				digitWidget.getId()
			);
		}

		if (!menuOptionClicked.isConsumed() && menuOptionClicked.getMenuAction() == MenuAction.WALK && menuOptionClicked.getParam0() == 0 && menuOptionClicked.getParam1() == 0)
		{
			instruction = null;
			menuOptionClicked.consume();
		}
		else
		{
			tikkie = 0;
		}
	}
}
