package com.owain.chinlogin

import com.google.inject.Provides
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.Point
import net.runelite.api.events.GameStateChanged
import net.runelite.api.events.GameTick
import net.runelite.api.widgets.WidgetID
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import org.pf4j.Extension
import java.awt.Rectangle
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import javax.inject.Inject

@Extension
@PluginDescriptor(
    name = "Chin login",
    description = "Automatically logs you in on the login screen because a 6 hour log is annoying",
    enabledByDefault = false
)
class ChinLoginPlugin : Plugin() {
    @Inject
    private lateinit var client: Client

    @Inject
    private lateinit var config: ChinLoginConfig

    @Inject
    private lateinit var configManager: ConfigManager

    private var executorService: ExecutorService? = null
    private var loginClicked: Boolean = false

    @Provides
    fun provideConfig(configManager: ConfigManager): ChinLoginConfig {
        return configManager.getConfig(ChinLoginConfig::class.java)
    }

    override fun startUp() {
        configManager.setConfiguration("loginscreen", "hideDisconnect", true)
        client.setHideDisconnect(true)
        executorService = Executors.newSingleThreadExecutor()
    }

    override fun shutDown() {
        if (executorService != null) {
            executorService!!.shutdown()
        }
    }

    @Subscribe
    private fun onGameTick(gametick: GameTick) {
        if (!loginClicked) {
            handleLoginScreen()
        }
    }

    @Subscribe
    private fun onGameStateChanged(gameStateChanged: GameStateChanged) {
        loginClicked = false

        if (executorService == null || config.email() == "" || config.password() == "") {
            return
        }

        executorService!!.submit {
            try {
                if (client.gameState == GameState.LOGIN_SCREEN) {
                    val username = config.email()
                    val password = config.password()
                    if (username != "" && password != "") {
                        waitDelayTime(400, 600)

                        sendKey(KeyEvent.VK_ENTER)

                        client.username = config.email()
                        client.setPassword(config.password())

                        waitDelayTime(400, 600)

                        sendKey(KeyEvent.VK_ENTER)
                        sendKey(KeyEvent.VK_ENTER)
                    }
                }
            } catch (e: InterruptedException) {
            }
        }
    }

    private fun handleLoginScreen() {
        val login = client.getWidget(WidgetID.LOGIN_CLICK_TO_PLAY_GROUP_ID, 87)

        if (login != null && login.text == "CLICK HERE TO PLAY") {
            if (login.bounds.x != -1 && login.bounds.y != -1) {
                executorService!!.submit {
                    try {
                        click(login.bounds)
                        waitDelayTime(400, 600)
                    } catch (e: InterruptedException) {
                    }
                }

                loginClicked = true
            }
        }
    }

    private fun click(rectangle: Rectangle) {
        val point: Point = getClickPoint(rectangle)
        click(point, client)
    }

    private fun click(p: Point, client: Client) {
        if (client.isStretchedEnabled) {
            val stretched = client.stretchedDimensions
            val real = client.realDimensions
            val width = stretched.width / real.getWidth()
            val height = stretched.height / real.getHeight()
            val point = Point((p.x * width).toInt(), (p.y * height).toInt())

            mouseEvent(MouseEvent.MOUSE_PRESSED, point, false)
            mouseEvent(MouseEvent.MOUSE_RELEASED, point, false)
            mouseEvent(MouseEvent.MOUSE_FIRST, point, false)

            return
        }

        mouseEvent(MouseEvent.MOUSE_PRESSED, p, false)
        mouseEvent(MouseEvent.MOUSE_RELEASED, p, false)
        mouseEvent(MouseEvent.MOUSE_FIRST, p, false)
    }

    private fun getClickPoint(rect: Rectangle): Point {
        val x = (rect.getX() + getRandomIntBetweenRange(rect.getWidth().toInt() / 6 * -1, rect.getWidth().toInt() / 6) + rect.getWidth() / 2).toInt()
        val y = (rect.getY() + getRandomIntBetweenRange(rect.getHeight().toInt() / 6 * -1, rect.getHeight().toInt() / 6) + rect.getHeight() / 2).toInt()

        return Point(x, y)
    }

    private fun getRandomIntBetweenRange(min: Int, max: Int): Int {
        return (Math.random() * (max - min + 1) + min).toInt()
    }

    private fun mouseEvent(id: Int, point: Point, move: Boolean) {
        val e = MouseEvent(
                client.canvas, id,
                System.currentTimeMillis(),
                0, point.x, point.y,
                if (move) 0 else 1, false, 1
        )

        if (client.gameState != GameState.LOGGED_IN) {
            return
        }

        client.canvas.dispatchEvent(e)
    }

    private fun sendKey(key: Int) {
        keyEvent(KeyEvent.KEY_PRESSED, key)
        keyEvent(KeyEvent.KEY_RELEASED, key)
    }

    private fun keyEvent(id: Int, key: Int) {
        val e = KeyEvent(
                client.canvas, id, System.currentTimeMillis(),
                0, key, KeyEvent.CHAR_UNDEFINED
        )
        client.canvas.dispatchEvent(e)
    }

    @Throws(InterruptedException::class)
    private fun waitDelayTime(lowerDelay: Int, upperDelay: Int) {
        Thread.sleep(lowerDelay + ThreadLocalRandom.current().nextInt(upperDelay - lowerDelay).toLong())
    }
}