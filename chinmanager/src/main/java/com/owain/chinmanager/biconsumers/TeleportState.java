package com.owain.chinmanager.biconsumers;

import com.owain.chinmanager.ChinManagerContext;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinmanager.tasks.TeleportTask;
import com.owain.chinstatemachine.StateMachine;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import javax.inject.Inject;

public class TeleportState
{
	private final ChinManagerPlugin chinManagerPlugin;
	private final TeleportTask teleportTask;

	private Disposable disposable;

	@Inject
	TeleportState(ChinManagerPlugin chinManagerPlugin, TeleportTask teleportTask)
	{
		this.chinManagerPlugin = chinManagerPlugin;
		this.teleportTask = teleportTask;
	}

	public BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> teleport()
	{
		return (t1, state) -> {
			@NonNull Completable obs = chinManagerPlugin.getTaskExecutor().prepareTask(teleportTask).ignoreElements();
			disposable = obs
				.subscribe();
		};
	}

	public BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> unsubscribe()
	{
		return (t1, state) -> {
			if (teleportTask != null)
			{
				teleportTask.unsubscribe();
			}

			if (!disposable.isDisposed())
			{
				disposable.dispose();
			}
		};
	}
}
