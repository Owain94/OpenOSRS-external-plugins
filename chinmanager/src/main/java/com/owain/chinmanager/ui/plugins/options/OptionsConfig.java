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

	@ConfigTitle(
		name = "Notifications",
		description = "",
		position = 10
	)
	String notificationsTitle = "Notifications";

	@ConfigItem(
		keyName = "discordNotifications",
		name = "Discord notifications",
		description = "Enable discord notifications",
		position = 11,
		title = notificationsTitle
	)
	default boolean discordNotifications()
	{
		return false;
	}

	@ConfigItem(
		keyName = "username",
		name = "Username",
		description = "Send username in the notification",
		position = 12,
		title = notificationsTitle,
		hidden = true,
		unhide = "discordNotifications"
	)
	default boolean username()
	{
		return true;
	}

	@ConfigItem(
		keyName = "breaks",
		name = "Break",
		description = "Send a notification when taking a break",
		position = 13,
		title = notificationsTitle,
		hidden = true,
		unhide = "discordNotifications"
	)
	default boolean breaks()
	{
		return true;
	}

	@ConfigItem(
		keyName = "resume",
		name = "Resume",
		description = "Send a notification when resuming from a break",
		position = 14,
		title = notificationsTitle,
		hidden = true,
		unhide = "discordNotifications"
	)
	default boolean resume()
	{
		return true;
	}

	@ConfigItem(
		keyName = "plugin",
		name = "Plugin transition",
		description = "Send a notification when switching to a different plugin",
		position = 15,
		title = notificationsTitle,
		hidden = true,
		unhide = "discordNotifications"
	)
	default boolean plugin()
	{
		return true;
	}

	@ConfigItem(
		keyName = "level",
		name = "Level up",
		description = "Send a notification when leveling up",
		position = 16,
		title = notificationsTitle,
		hidden = true,
		unhide = "discordNotifications"
	)
	default boolean level()
	{
		return true;
	}

	@ConfigItem(
		keyName = "pet",
		name = "Pet",
		description = "Send a notification when receiving a pet",
		position = 17,
		title = notificationsTitle,
		hidden = true,
		unhide = "discordNotifications"
	)
	default boolean pet()
	{
		return true;
	}

	@ConfigItem(
		keyName = "collectionlog",
		name = "Collection log",
		description = "Send a notification when unlocking a new collection log item",
		position = 18,
		title = notificationsTitle,
		hidden = true,
		unhide = "discordNotifications"
	)
	default boolean collectionLog()
	{
		return true;
	}
}