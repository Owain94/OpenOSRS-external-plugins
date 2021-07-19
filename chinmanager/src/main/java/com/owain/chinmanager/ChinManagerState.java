package com.owain.chinmanager;


import com.owain.chinmanager.biconsumers.BankPinState;
import com.owain.chinmanager.biconsumers.BankingState;
import com.owain.chinmanager.biconsumers.LoginScreenState;
import com.owain.chinmanager.biconsumers.LoginState;
import com.owain.chinmanager.biconsumers.LogoutState;
import com.owain.chinmanager.biconsumers.ResumeState;
import com.owain.chinmanager.biconsumers.SetupState;
import com.owain.chinmanager.biconsumers.TeleportState;
import com.owain.chinstatemachine.StateMachine;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChinManagerState
{
	public static StateMachine<ChinManagerContext, ChinManagerStates> stateMachine = null;

	public static StateMachine.State<ChinManagerContext, ChinManagerStates> IDLE = new StateMachine.State<>(ChinManagerStates.IDLE.toString());
	public static StateMachine.State<ChinManagerContext, ChinManagerStates> LOGIN = new StateMachine.State<>(ChinManagerStates.LOGIN.toString());
	public static StateMachine.State<ChinManagerContext, ChinManagerStates> LOGIN_SCREEN = new StateMachine.State<>(ChinManagerStates.LOGIN_SCREEN.toString());
	public static StateMachine.State<ChinManagerContext, ChinManagerStates> RESUME = new StateMachine.State<>(ChinManagerStates.RESUME.toString());
	public static StateMachine.State<ChinManagerContext, ChinManagerStates> SETUP = new StateMachine.State<>(ChinManagerStates.SETUP.toString());
	public static StateMachine.State<ChinManagerContext, ChinManagerStates> BANKING = new StateMachine.State<>(ChinManagerStates.BANKING.toString());
	public static StateMachine.State<ChinManagerContext, ChinManagerStates> BANK_PIN = new StateMachine.State<>(ChinManagerStates.BANK_PIN.toString());
	public static StateMachine.State<ChinManagerContext, ChinManagerStates> TELEPORTING = new StateMachine.State<>(ChinManagerStates.TELEPORTING.toString());
	public static StateMachine.State<ChinManagerContext, ChinManagerStates> LOGOUT = new StateMachine.State<>(ChinManagerStates.LOGOUT.toString());

	@Inject
	public ChinManagerState(BankingState bankingState, BankPinState bankPinState, SetupState setupState,
							LoginState loginState, LoginScreenState loginScreenState, ResumeState resumeState,
							LogoutState logoutState, TeleportState teleportState)
	{
		stateMachine = new StateMachine<>(new ChinManagerContext(), IDLE);

		IDLE
			.transition(ChinManagerStates.IDLE, IDLE)
			.transition(ChinManagerStates.LOGIN, LOGIN)
			.transition(ChinManagerStates.LOGIN_SCREEN, LOGIN_SCREEN)
			.transition(ChinManagerStates.RESUME, RESUME)
			.transition(ChinManagerStates.SETUP, SETUP)
			.transition(ChinManagerStates.BANK_PIN, BANK_PIN)
			.transition(ChinManagerStates.BANKING, BANKING)
			.transition(ChinManagerStates.TELEPORTING, TELEPORTING)
			.transition(ChinManagerStates.LOGOUT, LOGOUT);

		SETUP
			.onEnter(setupState.setup())
			.onExit(setupState.unsubscribe())
			.transition(ChinManagerStates.IDLE, IDLE);

		LOGIN
			.onEnter(loginState.login())
			.onExit(loginState.unsubscribe())
			.transition(ChinManagerStates.LOGIN, LOGIN)
			.transition(ChinManagerStates.LOGIN_SCREEN, LOGIN_SCREEN)
			.transition(ChinManagerStates.IDLE, IDLE);

		LOGIN_SCREEN
			.onEnter(loginScreenState.loginScreen())
			.onExit(loginScreenState.unsubscribe())
			.transition(ChinManagerStates.RESUME, RESUME)
			.transition(ChinManagerStates.IDLE, IDLE);

		RESUME
			.onEnter(resumeState.resume())
			.transition(ChinManagerStates.SETUP, SETUP)
			.transition(ChinManagerStates.BANKING, BANKING)
			.transition(ChinManagerStates.TELEPORTING, TELEPORTING)
			.transition(ChinManagerStates.IDLE, IDLE);

		BANKING
			.onEnter(bankingState.bank())
			.onExit(bankingState.unsubscribe())
			.transition(ChinManagerStates.IDLE, IDLE);

		TELEPORTING
			.onEnter(teleportState.teleport())
			.onExit(teleportState.unsubscribe())
			.transition(ChinManagerStates.IDLE, IDLE);

		BANK_PIN
			.onEnter(bankPinState.pin())
			.onExit(bankPinState.unsubscribe())
			.transition(ChinManagerStates.IDLE, IDLE);

		LOGOUT
			.onEnter(logoutState.logout())
			.onExit(logoutState.unsubscribe())
			.transition(ChinManagerStates.IDLE, IDLE);
	}
}

