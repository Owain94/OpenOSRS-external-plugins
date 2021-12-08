package com.owain.chinmanager.biconsumers;

import com.owain.chinmanager.ChinManagerContext;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinmanager.tasks.SetupTask;
import com.owain.chinstatemachine.StateMachine;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import javax.inject.Inject;

public class SetupState
{
	private final ChinManagerPlugin chinManagerPlugin;
	private final SetupTask setupTask;

	private Disposable disposable;

	@Inject
	SetupState(ChinManagerPlugin chinManagerPlugin, SetupTask setupTask)
	{
		this.chinManagerPlugin = chinManagerPlugin;
		this.setupTask = setupTask;
	}

	public BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> setup()
	{
		return (t1, state) -> {
			@NonNull Completable obs = chinManagerPlugin.getTaskExecutor().prepareTask(setupTask).ignoreElements();
			disposable = obs
				.subscribe();
		};
	}

	public BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> unsubscribe()
	{
		return (t1, state) -> {
			if (setupTask != null)
			{
				setupTask.unsubscribe();
			}

			if (!disposable.isDisposed())
			{
				disposable.dispose();
			}
		};
	}
}
