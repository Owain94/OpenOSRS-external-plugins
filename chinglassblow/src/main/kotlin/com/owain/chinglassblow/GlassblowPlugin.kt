package com.owain.chinglassblow

import com.google.inject.Provides
import net.runelite.api.Client
import net.runelite.api.events.GameTick
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.plugins.PluginType
import org.pf4j.Extension
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import javax.inject.Inject

@Extension
@PluginDescriptor(
        name = "Chin glass blow",
        description = "Automatically select the item to make on molten glass related widgets",
        type = PluginType.MISCELLANEOUS,
        enabledByDefault = false
)
class GlassblowPlugin : Plugin() {

    @Inject
    private lateinit var client: Client

    @Inject
    private lateinit var config: GlassblowConfig

    @Provides
    fun provideConfig(configManager: ConfigManager): GlassblowConfig {
        return configManager.getConfig(GlassblowConfig::class.java)
    }

    @Subscribe
    fun onGameTick(event: GameTick) {
        if (client.getWidget(270, 14) != null) {
            if (client.getWidget(270, 14)!!.name.contains("Beer glass")) {
                when (config.output()) {
                    GlassblowConfig.GlassType.BEER_GLASS -> sendKey(49)
                    GlassblowConfig.GlassType.CANDLE_LANTERN -> sendKey(50)
                    GlassblowConfig.GlassType.OIL_LAMP -> sendKey(51)
                    GlassblowConfig.GlassType.VIAL -> sendKey(52)
                    GlassblowConfig.GlassType.EMPTY_FISHBOWL -> sendKey(53)
                    GlassblowConfig.GlassType.UNPOWERED_ORB -> sendKey(54)
                    GlassblowConfig.GlassType.LANTERN_LENS -> sendKey(55)
                    GlassblowConfig.GlassType.LIGHT_ORB -> sendKey(56)
                }
            } else if (client.getWidget(270, 14)!!.name.contains("Molten glass")) {
                sendKey(49)
            } else if (client.getWidget(270, 14)!!.name.contains("shark")) {
                sendKey(49)
            }
        }
    }

    private fun sendKey(char: Int) {
        val kvPressed = KeyEvent(client.canvas, KEY_PRESSED, System.currentTimeMillis(), 0, char, CHAR_UNDEFINED)
        val kvTyped = KeyEvent(client.canvas, KEY_TYPED, System.currentTimeMillis(), 0, VK_UNDEFINED, char.toChar())
        val kvReleased = KeyEvent(client.canvas, KEY_RELEASED, System.currentTimeMillis(), 0, char, CHAR_UNDEFINED)

        client.canvas.dispatchEvent(kvPressed)
        client.canvas.dispatchEvent(kvTyped)
        client.canvas.dispatchEvent(kvReleased)
    }
}