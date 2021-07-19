package com.owain.chinmanager.tasks;

import com.owain.automation.Automation;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chintasks.Task;
import io.reactivex.rxjava3.core.ObservableEmitter;
import java.awt.event.KeyEvent;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

@Slf4j
public class KeyTask implements Task<Void>
{
	private final ChinManagerPlugin chinManagerPlugin;
	private final Client client;
	private final String input;

	@Inject
	KeyTask(ChinManagerPlugin chinManagerPlugin, String input)
	{
		this.chinManagerPlugin = chinManagerPlugin;
		this.client = chinManagerPlugin.getClient();
		this.input = input;
	}

	@Override
	public void routine(ObservableEmitter<Void> emitter)
	{
		chinManagerPlugin.getExecutorService().submit(() ->
		{

			try
			{
				if (input.equals(" "))
				{
					Automation.randomDelay(false, 120, 240, 180, 10, chinManagerPlugin.getRandom());
					Automation.sendKey(KeyEvent.VK_SPACE, client, false);
				}
				else
				{
					for (char ch : input.toCharArray())
					{
						Automation.randomDelay(false, 120, 240, 180, 10, chinManagerPlugin.getRandom());
						Automation.sendKey(ch, client);
					}
				}
			}
			catch (InterruptedException ignored)
			{
			}
		});
	}
}
