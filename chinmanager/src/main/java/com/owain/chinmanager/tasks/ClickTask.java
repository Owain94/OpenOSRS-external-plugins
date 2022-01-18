package com.owain.chinmanager.tasks;

import com.owain.automation.Automation;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chintasks.Task;
import io.reactivex.rxjava3.core.ObservableEmitter;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Point;

@Slf4j
public class ClickTask implements Task<Void>
{
	private final Client client;
	private final ChinManagerPlugin chinManagerPlugin;

	@Inject
	ClickTask(ChinManagerPlugin chinManagerPlugin)
	{
		this.chinManagerPlugin = chinManagerPlugin;
		this.client = chinManagerPlugin.getClient();
	}

	@Override
	public void routine(ObservableEmitter<Void> emitter)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (chinManagerPlugin.getExecutorService().isShutdown() || chinManagerPlugin.getExecutorService().isTerminated())
		{
			return;
		}

		chinManagerPlugin.getExecutorService().submit(() ->
		{
			try
			{
				Point point = new Point(0, 0);

				Automation.randomDelay(false, 120, 240, 180, 10, chinManagerPlugin.getRandom());
				Automation.click(point, client);
			}
			catch (InterruptedException e)
			{
				// ignore
			}
		});
	}
}
