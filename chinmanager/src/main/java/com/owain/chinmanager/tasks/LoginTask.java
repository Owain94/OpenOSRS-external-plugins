package com.owain.chinmanager.tasks;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerState;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinmanager.utils.IntRandomNumberGenerator;
import com.owain.chintasks.Task;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class LoginTask implements Task<Void>
{
	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	private final Client client;
	private final ClientThread clientThread;
	private final ConfigManager configManager;
	private final EventBus eventBus;

	private final List<Disposable> disposables = new ArrayList<>();

	@Inject
	LoginTask(ChinManager chinManager, ChinManagerPlugin chinManagerPlugin)
	{
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;
		this.client = chinManagerPlugin.getClient();
		this.clientThread = chinManagerPlugin.getClientThread();
		this.configManager = chinManagerPlugin.getConfigManager();
		this.eventBus = chinManagerPlugin.getEventBus();
	}

	@Override
	public void routine(ObservableEmitter<Void> emitter)
	{
		eventBus.register(this);

		login();
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
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		ChinManagerState.stateMachine.accept(ChinManagerStates.LOGIN_SCREEN);
	}

	private void login()
	{
		disposables.add(
			Observable.timer(3, TimeUnit.SECONDS)
				.observeOn(Schedulers.from(clientThread))
				.subscribe((a) -> {
					if (chinManager.getActivePlugins().isEmpty())
					{
						return;
					}

					if (client.getGameState() == GameState.LOGIN_SCREEN)
					{
						login();
					}
				})
		);

		boolean manual = Boolean.parseBoolean(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection"));

		String username = null;
		String password = null;

		if (manual)
		{
			username = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-username");
			password = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-password");
		}
		else
		{
			String account = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-profiles-account");

			String profileData = ChinManagerPlugin.getProfileData();

			if (profileData == null)
			{
				return;
			}

			Optional<String> accountData = Arrays.stream(profileData.split("\\n"))
				.filter(s -> s.startsWith(account))
				.findFirst();

			if (accountData.isPresent())
			{
				String[] parts = accountData.get().split(":");
				username = parts[1];
				if (parts.length == 3)
				{
					password = parts[2];
				}
			}
		}

		if (username != null && password != null)
		{
			String finalUsername = username;
			String finalPassword = password;

			clientThread.invoke(() ->
				{
					client.setUsername(finalUsername);
					client.setPassword(finalPassword);

					chinManagerPlugin.getExecutorService().submit(() ->
					{
						try
						{
							sendKey(KeyEvent.VK_ENTER);
							Thread.sleep(new IntRandomNumberGenerator(80, 160).nextInt());
							sendKey(KeyEvent.VK_ENTER);
							Thread.sleep(new IntRandomNumberGenerator(80, 160).nextInt());
							sendKey(KeyEvent.VK_ENTER);
						}
						catch (InterruptedException e)
						{
							log.error("", e);
						}
					});
				}
			);
		}
	}

	@SuppressWarnings("SameParameterValue")
	private void sendKey(int key)
	{
		keyEvent(KeyEvent.KEY_PRESSED, key);
		keyEvent(KeyEvent.KEY_RELEASED, key);
	}

	private void keyEvent(int id, int key)
	{
		KeyEvent e = new KeyEvent(
			client.getCanvas(), id, System.currentTimeMillis(),
			0, key, KeyEvent.CHAR_UNDEFINED
		);

		client.getCanvas().dispatchEvent(e);
	}
}
