package com.owain.ignorecompliance

import net.runelite.api.Client
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import org.pf4j.Extension
import javax.inject.Inject

@Extension
@PluginDescriptor(
        name = "Ignore compliance",
        description = "Sets the compliance value of OpenOSRS to false",
        enabledByDefault = false
)
class CompliancePlugin : Plugin() {

    @Inject
    private lateinit var client: Client

    override fun startUp() {
        client.setComplianceValue("orbInteraction", true)
    }


    override fun shutDown() {
        client.setComplianceValue("orbInteraction", false)
    }
}