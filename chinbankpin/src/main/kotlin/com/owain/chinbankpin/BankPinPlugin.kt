package com.owain.chinbankpin

import com.google.inject.Provides
import net.runelite.api.Client
import net.runelite.api.events.GameTick
import net.runelite.api.widgets.WidgetID
import net.runelite.api.widgets.WidgetInfo.BANK_PIN_INSTRUCTION_TEXT
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import org.pf4j.Extension
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import javax.inject.Inject


@Extension
@PluginDescriptor(
        name = "Chin bank pin",
        description = "Automatically enters your bank pin",
        enabledByDefault = false
)
class BankPinPlugin : Plugin() {
    @Inject
    private lateinit var client: Client

    @Inject
    private lateinit var config: BankPinConfig

    @Inject
    private lateinit var configManager: ConfigManager

    private var first = false
    private var second = false
    private var third = false
    private var fourth = false

    @Provides
    fun provideConfig(configManager: ConfigManager): BankPinConfig {
        return configManager.getConfig(BankPinConfig::class.java)
    }

    override fun startUp() {
		configManager.setConfiguration("bank", "bankPinKeyboard", true)
    }

    @Subscribe
    fun onGameTick(event: GameTick) {
        if (client.getWidget(WidgetID.BANK_PIN_GROUP_ID, BANK_PIN_INSTRUCTION_TEXT.childId) == null
                || (client.getWidget(BANK_PIN_INSTRUCTION_TEXT)!!.text != "First click the FIRST digit."
                        && client.getWidget(BANK_PIN_INSTRUCTION_TEXT)!!.text != "Now click the SECOND digit."
                        && client.getWidget(BANK_PIN_INSTRUCTION_TEXT)!!.text != "Time for the THIRD digit."
                        && client.getWidget(BANK_PIN_INSTRUCTION_TEXT)!!.text != "Finally, the FOURTH digit.")) {
            return
        }

        if (config.bankpin().length != 4) {
            return
        }

        val number: String = config.bankpin()

        val digits = number.toCharArray()
        var charCode = -1

        when (client.getWidget(BANK_PIN_INSTRUCTION_TEXT)!!.text) {
            "First click the FIRST digit." -> {
                if (first) {
                    return
                }
                charCode = getExtendedKeyCodeForChar(digits[0].toInt())
                first = true
            }
            "Now click the SECOND digit." -> {
                if (second) {
                    return
                }
                charCode = getExtendedKeyCodeForChar(digits[1].toInt())
                second = true
            }
            "Time for the THIRD digit." -> {
                if (third) {
                    return
                }
                charCode = getExtendedKeyCodeForChar(digits[2].toInt())
                third = true
            }
            "Finally, the FOURTH digit." -> {
                if (!first && !fourth) {
                    return
                }
                charCode = getExtendedKeyCodeForChar(digits[3].toInt())
                fourth = true
            }
        }

        if (charCode != -1)
        {
            sendKey(charCode)

            if (fourth)
            {
                first = false
                second = false
                third = false
                fourth = false
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