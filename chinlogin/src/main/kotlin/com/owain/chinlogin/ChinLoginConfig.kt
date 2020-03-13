package com.owain.chinbankpin

import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem

@ConfigGroup("ChinLogin")
interface ChinLoginConfig : Config {
    @ConfigItem(
            keyName = "email",
            name = "Email",
            description = "",
            position = 0
    )
    @JvmDefault
    fun email(): String {
        return ""
    }

    @ConfigItem(
            keyName = "password",
            name = "Password",
            description = "",
            position = 1
    )
    @JvmDefault
    fun password(): String {
        return ""
    }
}