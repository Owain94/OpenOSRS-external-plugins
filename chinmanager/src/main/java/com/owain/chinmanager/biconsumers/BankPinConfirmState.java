package com.owain.chinmanager.biconsumers;

import com.owain.chinmanager.ChinManagerContext;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinmanager.tasks.BankPinConfirmTask;
import com.owain.chinstatemachine.StateMachine;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import javax.inject.Inject;

public class BankPinConfirmState
{
	private final ChinManagerPlugin chinManagerPlugin;
	private final BankPinConfirmTask bankPinConfirmTask;

	private Disposable disposable;

	@Inject
	BankPinConfirmState(ChinManagerPlugin chinManagerPlugin, BankPinConfirmTask bankPinConfirmTask)
	{
		this.chinManagerPlugin = chinManagerPlugin;
		this.bankPinConfirmTask = bankPinConfirmTask;
	}

	public BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> confirm()
	{
		return (t1, state) -> {
			@NonNull Completable obs = chinManagerPlugin.getTaskExecutor().prepareTask(bankPinConfirmTask).ignoreElements();
			disposable = obs
				.subscribe();
		};
	}

	public BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> unsubscribe()
	{
		return (t1, state) -> {
			if (bankPinConfirmTask != null)
			{
				bankPinConfirmTask.unsubscribe();
			}

			if (!disposable.isDisposed())
			{
				disposable.dispose();
			}
		};
	}
}
