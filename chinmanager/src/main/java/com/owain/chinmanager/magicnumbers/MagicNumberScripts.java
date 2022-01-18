package com.owain.chinmanager.magicnumbers;

public enum MagicNumberScripts
{
	MINIGAME_TELEPORT(MagicNumberScript.MINIGAME_TELEPORT),
	CHAT_INPUT_ENTER(MagicNumberScript.CHAT_INPUT_ENTER),
	ACTIVE_TAB(MagicNumberScript.ACTIVE_TAB),
	TOPLEVEL_COMPASS(MagicNumberScript.TOPLEVEL_COMPASS),
	FORCE_CAMERA_ANGLE(MagicNumberScript.FORCE_CAMERA_ANGLE),

	;

	private final int id;

	MagicNumberScripts(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}
}

