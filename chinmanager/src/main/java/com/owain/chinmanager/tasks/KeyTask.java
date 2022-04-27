package com.owain.chinmanager.tasks;

import static com.owain.automation.Automation.randomDelay;
import static com.owain.automation.Automation.sendKey;
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
		if (chinManagerPlugin.getExecutorService().isShutdown() || chinManagerPlugin.getExecutorService().isTerminated())
		{
			return;
		}

		chinManagerPlugin.getExecutorService().submit(() ->
		{
			try
			{
				if (input.equals(" "))
				{
					randomDelay(false, 120, 240, 180, 10, chinManagerPlugin.getRandom());
					sendKey(KeyEvent.VK_SPACE, client, false);
				}
				else if (input.equals("\n"))
				{
					randomDelay(false, 120, 240, 180, 10, chinManagerPlugin.getRandom());
					sendKey(KeyEvent.VK_ENTER, client, false);
				}
				else
				{
					for (char ch : input.toCharArray())
					{
						randomDelay(false, 120, 240, 180, 10, chinManagerPlugin.getRandom());
						sendKey(ch, client);
					}
				}
			}
			catch (InterruptedException ignored)
			{
			}
		});
	}
}
