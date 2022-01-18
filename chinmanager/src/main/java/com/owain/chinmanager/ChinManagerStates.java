package com.owain.chinmanager;

public enum ChinManagerStates
{
	IDLE("Idle"),
	LOGIN("Login"),
	LOGIN_SCREEN("Login screen"),
	RESUME("Resume"),
	SETUP("Setup"),
	BANK_PIN("Bank pin"),
	BANK_PIN_CONFIRM("Bank pin confirm"),
	BANKING("Banking"),
	TELEPORTING("Teleporting"),
	LOGOUT("Log out"),
	;

	private final String name;

	ChinManagerStates(final String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}

