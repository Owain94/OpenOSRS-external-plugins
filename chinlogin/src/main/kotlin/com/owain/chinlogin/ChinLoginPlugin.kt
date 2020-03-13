package com.owain.chinlogin

import com.google.inject.Provides
import com.owain.chinbankpin.ChinLoginConfig
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.events.GameStateChanged
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.plugins.PluginType
import org.pf4j.Extension
import javax.inject.Inject

@Extension
@PluginDescriptor(
        name = "Chin login",
        description = "Automatically logs you in on the login screen because a 6 hour log is annoying",
        type = PluginType.MISCELLANEOUS
)
class ChinLoginPlugin : Plugin() {
    @Inject
    private lateinit var client: Client

    @Inject
    private lateinit var config: ChinLoginConfig

    @Inject
    private lateinit var configManager: ConfigManager

    @Provides
    fun provideConfig(configManager: ConfigManager): ChinLoginConfig {
        return configManager.getConfig(ChinLoginConfig::class.java)
    }

    override fun startUp() {
        configManager.setConfiguration("loginscreen", "hideDisconnect", true)
        client.setHideDisconnect(true)
    }

    @Subscribe
    private fun onGameStateChanged(gameStateChanged: GameStateChanged) {
        if (config.email() == "" || config.password() == "") {
            return;
        }

        if (gameStateChanged.gameState == GameState.LOGIN_SCREEN) {
            client.username = config.email()
            client.setPassword(config.password())
            client.gameState = GameState.LOGGING_IN
        }
    }
}