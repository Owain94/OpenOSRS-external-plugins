package com.owain.chinbreakhandler;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter(AccessLevel.PACKAGE)
@AllArgsConstructor
public enum ChinBreakHandlerState
{
	NULL,

	LOGIN_SCREEN,
	LOGIN_MESSAGE_SCREEN,
	INVENTORY,

	LOGOUT,
	LOGOUT_TAB,
	LOGOUT_BUTTON,
	LOGOUT_WAIT,

	;
}
