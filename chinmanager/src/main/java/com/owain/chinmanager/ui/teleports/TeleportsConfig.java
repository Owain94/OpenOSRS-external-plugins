/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Dalton <delps1001@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.owain.chinmanager.ui.teleports;

import static com.owain.chinmanager.ChinManagerPlugin.CONFIG_GROUP;
import com.owain.chinmanager.ui.teleports.config.AmuletOfGlory;
import com.owain.chinmanager.ui.teleports.config.CombatBracelet;
import com.owain.chinmanager.ui.teleports.config.DigsitePendant;
import com.owain.chinmanager.ui.teleports.config.GamesNecklace;
import com.owain.chinmanager.ui.teleports.config.Poh;
import com.owain.chinmanager.ui.teleports.config.RingOfDueling;
import com.owain.chinmanager.ui.teleports.config.RingOfWealth;
import com.owain.chinmanager.ui.teleports.config.SkillsNecklace;
import com.owain.chinmanager.ui.teleports.config.XericsTalisman;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitle;

@ConfigGroup(CONFIG_GROUP)
public interface TeleportsConfig extends Config
{
	@ConfigTitle(
		name = "Player-owned house",
		description = "",
		position = 0
	)
	String pohTitle = "pohTitle";
	@ConfigTitle(
		name = "Digsite pendant",
		description = "",
		position = 2
	)
	String digsitePendant = "digsitePendant";
	@ConfigTitle(
		name = "Xeric's talisman",
		description = "",
		position = 4
	)
	String xericsTalisman = "xericsTalisman";
	@ConfigTitle(
		name = "Ring of dueling",
		description = "",
		position = 6
	)
	String ringOfDueling = "ringOfDueling";
	@ConfigTitle(
		name = "Games necklace",
		description = "",
		position = 8
	)
	String gamesNecklace = "gamesNecklace";
	@ConfigTitle(
		name = "Combat bracelet",
		description = "",
		position = 10
	)
	String combatBracelet = "combatBracelet";
	@ConfigTitle(
		name = "Skills necklace",
		description = "",
		position = 12
	)
	String skillsNecklace = "skillsNecklace";
	@ConfigTitle(
		name = "Ring of wealth",
		description = "",
		position = 14
	)
	String ringOfWealth = "ringOfWealth";
	@ConfigTitle(
		name = "Amulet of glory",
		description = "",
		position = 16
	)
	String amuletOfGlory = "amuletOfGlory";

	@ConfigItem(
		keyName = "pohTeleport",
		name = "",
		description = "",
		position = 1,
		title = pohTitle
	)
	default Poh pohTeleport()
	{
		return Poh.RUNES;
	}

	@ConfigItem(
		keyName = "digsitePendantTeleport",
		name = "",
		description = "",
		position = 3,
		title = digsitePendant
	)
	default DigsitePendant digsitePendantTeleport()
	{
		return DigsitePendant.DIGSITE_PENDANT;
	}

	@ConfigItem(
		keyName = "xericsTalismanTeleport",
		name = "",
		description = "",
		position = 5,
		title = xericsTalisman
	)
	default XericsTalisman xericsTalismanTeleport()
	{
		return XericsTalisman.XERICS_TALISMAN;
	}

	@ConfigItem(
		keyName = "ringOfDuelingTeleport",
		name = "",
		description = "",
		position = 7,
		title = ringOfDueling
	)
	default RingOfDueling ringOfDuelingTeleport()
	{
		return RingOfDueling.RING_OF_DUELING;
	}

	@ConfigItem(
		keyName = "gamesNecklaceTeleport",
		name = "",
		description = "",
		position = 9,
		title = gamesNecklace
	)
	default GamesNecklace gamesNecklaceTeleport()
	{
		return GamesNecklace.GAMES_NECKLACE;
	}

	@ConfigItem(
		keyName = "combatBraceletTeleport",
		name = "",
		description = "",
		position = 11,
		title = combatBracelet
	)
	default CombatBracelet combatBraceletTeleport()
	{
		return CombatBracelet.COMBAT_BRACELET;
	}

	@ConfigItem(
		keyName = "skillsNecklaceTeleport",
		name = "",
		description = "",
		position = 13,
		title = skillsNecklace
	)
	default SkillsNecklace skillsNecklaceTeleport()
	{
		return SkillsNecklace.SKILLS_NECKLACE;
	}

	@ConfigItem(
		keyName = "ringOfWealthTeleport",
		name = "",
		description = "",
		position = 15,
		title = ringOfWealth
	)
	default RingOfWealth ringOfWealthTeleport()
	{
		return RingOfWealth.RING_OF_WEALTH;
	}

	@ConfigItem(
		keyName = "amuletOfGloryTeleport",
		name = "",
		description = "",
		position = 17,
		title = amuletOfGlory
	)
	default AmuletOfGlory amuletOfGloryTeleport()
	{
		return AmuletOfGlory.AMULET_OF_GLORY;
	}
}