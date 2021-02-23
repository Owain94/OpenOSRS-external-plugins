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
package com.owain.chindaeyalt;

import static com.owain.chindaeyalt.ChinDaeyaltPlugin.CONFIG_GROUP;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitle;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Units;

@SuppressWarnings({"unused"})
@ConfigGroup(CONFIG_GROUP)
public interface ChinDaeyaltConfig extends Config
{
	@ConfigTitle(
		name = "On / off toggle",
		description = "On / off toggle",
		position = 1
	)
	String toggle = "toggle";

	@ConfigItem(
		keyName = "hotkeyToggle",
		name = "Hotkey toggle",
		description = "Toggles the plugin on and off",
		position = 2,
		title = toggle
	)
	default Keybind hotkeyToggle()
	{
		return Keybind.NOT_SET;
	}

	@ConfigTitle(
		name = "Delays",
		description = "",
		position = 3
	)
	String delays = "delays";

	@Units(Units.MILLISECONDS)
	@ConfigItem(
		keyName = "miniDelay",
		name = "Minimum delay",
		description = "Absolute minimum delay between actions",
		position = 4,
		title = delays
	)
	default int minimumDelay()
	{
		return 120;
	}

	@Units(Units.MILLISECONDS)
	@ConfigItem(
		keyName = "maxiDelay",
		name = "Maximum delay",
		description = "Absolute maximum delay between actions",
		position = 5,
		title = delays
	)
	default int maximumDelay()
	{
		return 240;
	}

	@Units(Units.MILLISECONDS)
	@ConfigItem(
		keyName = "target",
		name = "Delay Target",
		description = "",
		position = 6,
		title = delays
	)
	default int target()
	{
		return 180;
	}

	@Units(Units.MILLISECONDS)
	@ConfigItem(
		keyName = "deviation",
		name = "Delay Deviation",
		description = "",
		position = 7,
		title = delays
	)
	default int deviation()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "weightedDistribution",
		name = "Weighted Distribution",
		description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
		position = 8,
		title = delays
	)
	default boolean weightedDistribution()
	{
		return false;
	}
}