package com.owain.chinmanager.tasks;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinmanager.magicnumbers.MagicNumberScripts;
import com.owain.chintasks.Task;
import io.reactivex.rxjava3.core.ObservableEmitter;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.VarClientInt;
import net.runelite.client.callback.ClientThread;

@Slf4j
public class SetupTask implements Task<Void>
{
	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	private final Client client;
	private final ClientThread clientThread;

	@Inject
	SetupTask(ChinManager chinManager, ChinManagerPlugin chinManagerPlugin)
	{
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;
		this.client = chinManagerPlugin.getClient();
		this.clientThread = chinManagerPlugin.getClientThread();
	}

	@Override
	public void routine(ObservableEmitter<Void> emitter)
	{
		if (chinManagerPlugin.getExecutorService() == null || chinManagerPlugin.getExecutorService().isShutdown() || chinManagerPlugin.getExecutorService().isTerminated())
		{
			chinManagerPlugin.setExecutorService(Executors.newSingleThreadExecutor());
		}

		clientThread.invoke(() -> {
			client.runScript(ScriptID.CAMERA_DO_ZOOM, 200, 200);
			client.runScript(MagicNumberScripts.TOPLEVEL_COMPASS.getId(), 1); // North
			client.runScript(MagicNumberScripts.FORCE_CAMERA_ANGLE.getId(), 512, client.getMapAngle());

			if (client.getVar(VarClientInt.INVENTORY_TAB) != 3)
			{
				client.runScript(MagicNumberScripts.ACTIVE_TAB.getId(), 3);
			}

			chinManagerPlugin.transition(ChinManagerStates.IDLE);
			chinManager.setCurrentlyActive(chinManager.getNextActive());
		});
	}

	public void unsubscribe()
	{
		chinManagerPlugin.getExecutorService().shutdownNow();
	}
}
