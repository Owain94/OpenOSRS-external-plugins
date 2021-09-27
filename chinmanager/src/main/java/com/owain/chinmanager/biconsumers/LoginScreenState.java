package com.owain.chinmanager.biconsumers;

import com.owain.chinmanager.ChinManagerContext;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinmanager.tasks.LoginScreenTask;
import com.owain.chinstatemachine.StateMachine;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import javax.inject.Inject;

public class LoginScreenState
{
	private final ChinManagerPlugin chinManagerPlugin;
	private final LoginScreenTask loginScreenTask;

	private Disposable disposable;

	@Inject
	LoginScreenState(ChinManagerPlugin chinManagerPlugin, LoginScreenTask loginScreenTask)
	{
		this.chinManagerPlugin = chinManagerPlugin;
		this.loginScreenTask = loginScreenTask;
	}

	public BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> loginScreen()
	{
		return (t1, state) -> {
			@NonNull Completable obs = chinManagerPlugin.getTaskExecutor().prepareTask(loginScreenTask).ignoreElements();
			disposable = obs
				.subscribe();
		};
	}

	public BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> unsubscribe()
	{
		return (t1, state) -> {
			if (loginScreenTask != null)
			{
				loginScreenTask.unsubscribe();
			}

			if (!disposable.isDisposed())
			{
				disposable.dispose();
			}
		};
	}
}
