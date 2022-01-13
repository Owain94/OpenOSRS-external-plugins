package com.owain.chinlogin

import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem

@ConfigGroup("ChinLogin")
interface ChinLoginConfig : Config {
    @ConfigItem(
            keyName = "startup",
            name = "Sign in on client start",
            description = "",
            position = -1
    )
    @JvmDefault
    fun startup(): Boolean {
        return false;
    }

    @ConfigItem(
            keyName = "startupDelay",
            name = "Startup Delay",
            description = "",
            position = 0
    )
    @JvmDefault
    fun startupDelay(): Int {
        return 5000;
    }

    @ConfigItem(
            keyName = "email",
            name = "Email",
            description = "",
            position = 1
    )
    @JvmDefault
    fun email(): String {
        return ""
    }

    @ConfigItem(
            keyName = "password",
            name = "Password",
            description = "",
            position = 2,
            secret = true
    )
    @JvmDefault
    fun password(): String {
        return ""
    }
}