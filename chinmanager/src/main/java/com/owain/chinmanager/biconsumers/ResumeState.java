package com.owain.chinmanager.biconsumers;

import com.owain.chinmanager.ChinManagerContext;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinstatemachine.StateMachine;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import javax.inject.Inject;

public class ResumeState
{

	private final ChinManagerPlugin chinManagerPlugin;

	private Disposable disposable;

	@Inject
	ResumeState(ChinManagerPlugin chinManagerPlugin)
	{
		this.chinManagerPlugin = chinManagerPlugin;
	}

	public BiConsumer<ChinManagerContext, StateMachine.State<ChinManagerContext, ChinManagerStates>> resume()
	{
		return (t1, state) -> {
			if (ChinManagerPlugin.shouldSetup)
			{
				chinManagerPlugin.transition(ChinManagerStates.SETUP);
			}
			else
			{
				chinManagerPlugin.transition(ChinManagerStates.IDLE);
			}
		};
	}
}
