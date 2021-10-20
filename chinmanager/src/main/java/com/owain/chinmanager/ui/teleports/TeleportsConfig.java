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

	@ConfigTitle(
		name = "Digsite pendant",
		description = "",
		position = 2
	)
	String digsitePendant = "digsitePendant";

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

	@ConfigTitle(
		name = "Xeric's talisman",
		description = "",
		position = 4
	)
	String xericsTalisman = "xericsTalisman";

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

	@ConfigTitle(
		name = "Ring of dueling",
		description = "",
		position = 6
	)
	String ringOfDueling = "ringOfDueling";

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

	@ConfigTitle(
		name = "Games necklace",
		description = "",
		position = 8
	)
	String gamesNecklace = "gamesNecklace";

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

	@ConfigTitle(
		name = "Combat bracelet",
		description = "",
		position = 10
	)
	String combatBracelet = "combatBracelet";

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

	@ConfigTitle(
		name = "Skills necklace",
		description = "",
		position = 12
	)
	String skillsNecklace = "skillsNecklace";

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

	@ConfigTitle(
		name = "Ring of wealth",
		description = "",
		position = 14
	)
	String ringOfWealth = "ringOfWealth";

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

	@ConfigTitle(
		name = "Amulet of glory",
		description = "",
		position = 16
	)
	String amuletOfGlory = "amuletOfGlory";

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