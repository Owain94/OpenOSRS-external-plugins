package com.owain.chinmanager.biconsumers;

import com.owain.chinmanager.ChinManagerContext;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinmanager.tasks.BankingTask;
import com.owain.chinstatemachine.StateMachine;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import javax.inject.Inject;

public class BankingState
{
	private final ChinManagerPlugin chinManagerPlugin;
	private final BankingTask bankingTask;

	private Disposable disposable;

	@Inject
	BankingState(ChinManagerPlugin chinManagerPlugin, BankingTask bankingTask)
	{
		this.chinManagerPlugin = chinManagerPlugin;
		this.bankingTask = bankingTask;
	}

	public BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> bank()
	{
		return (t1, state) -> {
			@NonNull Completable obs = chinManagerPlugin.getTaskExecutor().prepareTask(bankingTask).ignoreElements();
			disposable = obs
				.subscribe();
		};
	}

	public BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> unsubscribe()
	{
		return (t1, state) -> {
			if (bankingTask != null)
			{
				bankingTask.unsubscribe();
			}

			if (!disposable.isDisposed())
			{
				disposable.dispose();
			}
		};
	}
}
