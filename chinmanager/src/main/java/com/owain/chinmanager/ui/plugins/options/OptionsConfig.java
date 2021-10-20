package com.owain.chinmanager.ui.plugins.options;

import static com.owain.chinmanager.ChinManagerPlugin.CONFIG_GROUP;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitle;

@ConfigGroup(CONFIG_GROUP)
public interface OptionsConfig extends Config
{
	@ConfigTitle(
		name = "Miscellaneous",
		description = "",
		position = 0
	)
	String miscellaneousTitle = "Miscellaneous";

	@ConfigItem(
		keyName = "stopAfterBreaks",
		name = "Stop after x breaks",
		description = "Stop after a given amount of breaks (0 to disable)",
		position = 1,
		title = miscellaneousTitle
	)
	default int stopAfterBreaks()
	{
		return 0;
	}

	@ConfigTitle(
		name = "Hopping",
		description = "",
		position = 2
	)
	String hoppingTitle = "Hopping";

	@ConfigItem(
		keyName = "hop-after-break",
		name = "Hop world after break",
		description = "Hop to a different world after taking a break",
		position = 3,
		title = hoppingTitle
	)
	default boolean hopAfterBreak()
	{
		return false;
	}

	@ConfigItem(
		keyName = "american",
		name = "American",
		description = "Enable hopping to American worlds",
		position = 4,
		title = hoppingTitle,
		hidden = true,
		unhide = "hop-after-break"
	)
	default boolean american()
	{
		return false;
	}

	@ConfigItem(
		keyName = "united-kingdom",
		name = "United kingdom",
		description = "Enable hopping to UK worlds",
		position = 5,
		title = hoppingTitle,
		hidden = true,
		unhide = "hop-after-break"
	)
	default boolean unitedKingdom()
	{
		return false;
	}

	@ConfigItem(
		keyName = "german",
		name = "German",
		description = "Enable hopping to German worlds",
		position = 6,
		title = hoppingTitle,
		hidden = true,
		unhide = "hop-after-break"
	)
	default boolean german()
	{
		return false;
	}

	@ConfigItem(
		keyName = "australian",
		name = "Australian",
		description = "Enable hopping to Australian worlds",
		position = 7,
		title = hoppingTitle,
		hidden = true,
		unhide = "hop-after-break"
	)
	default boolean australian()
	{
		return false;
	}

	@ConfigTitle(
		name = "Overlays",
		description = "",
		position = 8
	)
	String overlaysTitle = "Overlays";

	@ConfigItem(
		keyName = "showOverlays",
		name = "Enable overlays",
		description = "This options toggles the outline on objects and NPCs",
		position = 9,
		title = overlaysTitle
	)
	default boolean showOverlays()
	{
		return true;
	}
}