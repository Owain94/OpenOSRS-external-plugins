package com.owain.chinmanager.magicnumbers;

public enum MagicNumberVars
{
	RUN(MagicNumberVar.RUN),
	BANK_DEPOSIT_BOX_QUANTITY(MagicNumberVar.BANK_DEPOSIT_BOX_QUANTITY),
	BANK_QUANTITY(MagicNumberVar.BANK_QUANTITY),
	LEPRECHAUN_STORE_QUANTITY(MagicNumberVar.LEPRECHAUN_STORE_QUANTITY),
	BANK_OPTIONS(MagicNumberVar.BANK_OPTIONS),
	SINGLES_PLUS(MagicNumberVar.SINGLES_PLUS),
	GROUPING_TAB(MagicNumberVar.GROUPING_TAB),
	FALADOR_ELITE(MagicNumberVar.FALADOR_ELITE),
	NOTING(MagicNumberVar.NOTING),
	WITHDRAWX(MagicNumberVar.WITHDRAWX),

	;

	private final int id;

	MagicNumberVars(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}
}

