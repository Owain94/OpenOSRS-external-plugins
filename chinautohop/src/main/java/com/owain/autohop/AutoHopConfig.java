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
package com.owain.autohop;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitleSection;
import net.runelite.client.config.Title;

@ConfigGroup("chinautohop")
public interface AutoHopConfig extends Config
{
	@ConfigTitleSection(
		keyName = "worldsTitle",
		name = "Worlds",
		description = "",
		position = 1
	)
	default Title worldsTitle()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "american",
		name = "American",
		description = "Allow hopping to American worlds",
		titleSection = "worldsTitle",
		position = 2
	)
	default boolean american()
	{
		return true;
	}

	@ConfigItem(
		keyName = "unitedkingdom",
		name = "UK",
		description = "Allow hopping to UK worlds",
		titleSection = "worldsTitle",
		position = 3
	)
	default boolean unitedkingdom()
	{
		return true;
	}

	@ConfigItem(
		keyName = "germany",
		name = "German",
		description = "Allow hopping to German worlds",
		titleSection = "worldsTitle",
		position = 4
	)
	default boolean germany()
	{
		return true;
	}

	@ConfigItem(
		keyName = "australia",
		name = "Australian",
		description = "Allow hopping to Australian worlds",
		titleSection = "worldsTitle",
		position = 5
	)
	default boolean australia()
	{
		return true;
	}

	@ConfigTitleSection(
		keyName = "ignoresTitle",
		name = "Ignore",
		description = "",
		position = 6
	)
	default Title ignoresTitle()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "friends",
		name = "Friends",
		description = "Don't hop when the player spawned is on your friend list",
		titleSection = "ignoresTitle",
		position = 7
	)
	default boolean friends()
	{
		return true;
	}

	@ConfigItem(
		keyName = "clanmembers",
		name = "Australian",
		description = "Don't hop when the player spawned is in your clan chat",
		titleSection = "ignoresTitle",
		position = 8
	)
	default boolean clanmember()
	{
		return true;
	}
}