package com.owain.chinmanager.tasks;

import com.owain.chinmanager.ChinManagerPlugin;
import static com.owain.chinmanager.ChinManagerState.stateMachine;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chintasks.Task;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetID;
import static net.runelite.api.widgets.WidgetInfo.BANK_PIN_INSTRUCTION_TEXT;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class BankPinTask implements Task<Void>
{
	private final ChinManagerPlugin chinManagerPlugin;
	private final Client client;
	private final ConfigManager configManager;
	private final EventBus eventBus;

	private final List<Disposable> disposables = new ArrayList<>();

	private boolean first = false;
	private boolean second = false;
	private boolean third = false;
	private boolean fourth = false;

	@Inject
	BankPinTask(ChinManagerPlugin chinManagerPlugin, EventBus eventBus)
	{
		this.chinManagerPlugin = chinManagerPlugin;
		this.client = chinManagerPlugin.getClient();
		this.configManager = chinManagerPlugin.getConfigManager();
		this.eventBus = eventBus;
	}

	@Override
	public void routine(ObservableEmitter<Void> emitter)
	{
		eventBus.register(this);
	}

	public void unsubscribe()
	{
		first = false;
		second = false;
		third = false;
		fourth = false;

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
		if (client.getWidget(WidgetID.BANK_PIN_GROUP_ID, BANK_PIN_INSTRUCTION_TEXT.getChildId()) != null &&
			(client.getWidget(BANK_PIN_INSTRUCTION_TEXT).getText().equals("First click the FIRST digit.") ||
				client.getWidget(BANK_PIN_INSTRUCTION_TEXT).getText().equals("Now click the SECOND digit.") ||
				client.getWidget(BANK_PIN_INSTRUCTION_TEXT).getText().equals("Time for the THIRD digit.") ||
				client.getWidget(BANK_PIN_INSTRUCTION_TEXT).getText().equals("Finally, the FOURTH digit.")))
		{
			if (client.getItemContainer(InventoryID.BANK) != null)
			{
				stateMachine.accept(ChinManagerStates.IDLE);
				return;
			}

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
				stateMachine.accept(ChinManagerStates.IDLE);
				return;
			}

			if (bankpin == null || bankpin.length() != 4)
			{
				stateMachine.accept(ChinManagerStates.IDLE);
				return;
			}

			switch (client.getWidget(BANK_PIN_INSTRUCTION_TEXT).getText())
			{
				case "First click the FIRST digit.":
					if (first)
					{
						return;
					}

					first = true;

					break;
				case "Now click the SECOND digit.":
					if (second)
					{
						return;
					}

					second = true;

					break;
				case "Time for the THIRD digit.":
					if (third)
					{
						return;
					}

					third = true;

					break;
				case "Finally, the FOURTH digit.":
					if (!first && !fourth)
					{
						return;
					}

					fourth = true;

					break;
			}

			if (first || second || third || fourth)
			{
				char number = 0;

				if (fourth)
				{
					number = bankpin.charAt(3);
				}
				else if (third)
				{
					number = bankpin.charAt(2);
				}
				else if (second)
				{
					number = bankpin.charAt(1);
				}
				else if (first)
				{
					number = bankpin.charAt(0);
				}

				disposables.add(chinManagerPlugin.getTaskExecutor().prepareTask(new KeyTask(chinManagerPlugin, String.valueOf(number))).ignoreElements().subscribe());

				if (fourth)
				{
					first = false;
					second = false;
					third = false;
					fourth = false;
				}
			}
		}
		else
		{
			stateMachine.accept(ChinManagerStates.IDLE);
		}
	}
}
