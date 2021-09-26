package com.owain.chinmanager.biconsumers;

import com.owain.chinmanager.ChinManagerContext;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinmanager.tasks.LogoutTask;
import com.owain.chinstatemachine.StateMachine;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import javax.inject.Inject;

public class LogoutState
{
	private final ChinManagerPlugin chinManagerPlugin;
	private final LogoutTask logoutTask;

	private Disposable disposable;

	@Inject
	LogoutState(ChinManagerPlugin chinManagerPlugin, LogoutTask logoutTask)
	{
		this.chinManagerPlugin = chinManagerPlugin;
		this.logoutTask = logoutTask;
	}

	public @NonNull BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> logout()
	{
		return (t1, state) -> {
			@NonNull Completable obs = chinManagerPlugin.getTaskExecutor().prepareTask(logoutTask).ignoreElements();
			disposable = obs
				.subscribe();
		};
	}

	public @NonNull BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> unsubscribe()
	{
		return (t1, state) -> {
			if (logoutTask != null)
			{
				logoutTask.unsubscribe();
			}

			if (!disposable.isDisposed())
			{
				disposable.dispose();
			}
		};
	}
}
