package com.owain.chinglassblow

import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem

@ConfigGroup("ChinBankPin")
interface GlassblowConfig : Config {
    enum class GlassType(private val type: String) {
        BEER_GLASS("Beer glass"),
        CANDLE_LANTERN("Candle lantern"),
        OIL_LAMP("Oil lamp"),
        VIAL("Vial"),
        EMPTY_FISHBOWL("Empty fishbowl"),
        UNPOWERED_ORB("Unpowered orb"),
        LANTERN_LENS("Lantern lens"),
        LIGHT_ORB("Light orb");

        override fun toString(): String {
            return type
        }
    }

    @ConfigItem(
        keyName = "output",
        name = "Output",
        description = "The output object to glassblow",
        position = 0
    )
    @JvmDefault
    fun output(): GlassType {
        return GlassType.BEER_GLASS
    }
}