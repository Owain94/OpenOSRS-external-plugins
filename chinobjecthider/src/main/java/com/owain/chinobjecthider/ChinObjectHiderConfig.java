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
package com.owain.chinobjecthider;

import static com.owain.chinobjecthider.ChinObjectHiderPlugin.CONFIG_GROUP;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@SuppressWarnings({"unused"})
@ConfigGroup(CONFIG_GROUP)
public interface ChinObjectHiderConfig extends Config
{
	@ConfigItem(
		keyName = "hideChanged",
		name = "Hide changed objects",
		description = "Warning: This may cause decrease in performance",
		position = -1
	)
	default boolean hideChanged()
	{
		return false;
	}

	@ConfigSection(
		name = "Hide by ID",
		description = "Hide objects by ID",
		position = 0
	)
	String hideObjectsID = "hideObjectsID";

	@ConfigItem(
		keyName = "objectIds",
		name = "Object IDs",
		description = "Configure hidden objects by id. Format: (id), (id)",
		position = 1,
		section = hideObjectsID
	)
	default String objectIds()
	{
		return "";
	}

	@ConfigSection(
		name = "Hide by name",
		description = "Hide objects by name",
		position = 2
	)
	String hideObjectsName = "hideObjectsName";

	@ConfigItem(
		keyName = "objectNames",
		name = "Objects names",
		description = "Configure hidden objects by name. Format: (Tree), (Mom)",
		position = 3,
		section = hideObjectsName
	)
	default String objectNames()
	{
		return "";
	}

	@ConfigSection(
		name = "Hide all objects",
		description = "Hide all objects",
		position = 4
	)
	String hideAllObjects = "hideAllObjects";

	@ConfigItem(
		keyName = "hideAllGameObjects",
		name = "Hide all game objects",
		description = "",
		position = 5,
		section = hideAllObjects
	)
	default boolean hideAllGameObjects()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideAllDecorativeObjects",
		name = "Hide all decorative objects",
		description = "",
		position = 6,
		section = hideAllObjects
	)
	default boolean hideAllDecorativeObjects()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideAllWallObjects",
		name = "Hide all wall objects",
		description = "",
		position = 7,
		section = hideAllObjects
	)
	default boolean hideAllWallObjects()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideAllGroundObjects",
		name = "Hide all ground objects",
		description = "",
		position = 8,
		section = hideAllObjects
	)
	default boolean hideAllGroundObjects()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideAllGraphicsObjects",
		name = "Hide all graphics objects",
		description = "",
		position = 9,
		section = hideAllObjects
	)
	default boolean hideAllGraphicsObjects()
	{
		return false;
	}
}