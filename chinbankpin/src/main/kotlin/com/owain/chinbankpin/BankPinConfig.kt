package com.owain.chinbankpin

import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem

@ConfigGroup("ChinBankPin")
interface BankPinConfig : Config {
    @ConfigItem(
            keyName = "bankpin",
            name = "Bank Pin",
            description = "Bank pin that will be entered",
            position = 0,
            secret = true
    )
    @JvmDefault
    fun bankpin(): String {
        return ""
    }
}