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
package com.owain.chinbreakhandler;

import static com.owain.chinbreakhandler.ChinBreakHandlerPlugin.CONFIG_GROUP;
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
}