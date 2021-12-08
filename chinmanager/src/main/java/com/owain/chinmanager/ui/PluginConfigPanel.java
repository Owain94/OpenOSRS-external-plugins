package com.owain.chinmanager.ui;

import com.owain.chinmanager.ui.gear.EquipmentPanel;
import com.owain.chinmanager.ui.plugins.PluginPanel;
import com.owain.chinmanager.ui.teleports.TeleportsConfig;
import com.owain.chinmanager.ui.utils.ConfigPanel;
import com.owain.chinmanager.ui.utils.JMultilineLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;

@Slf4j
public class PluginConfigPanel extends JPanel
{
	@Inject
	PluginConfigPanel(PluginPanel pluginPanel, EquipmentPanel equipmentPanel, ConfigPanel teleportsPanel, ConfigManager configManager)
	{
		teleportsPanel.init(configManager.getConfig(TeleportsConfig.class));

		setLayout(new BorderLayout());
		setBackground(ChinManagerPanel.PANEL_BACKGROUND_COLOR);
		setBorder(new EmptyBorder(5, 0, 0, 0));

		String startPlugins = "Select the plugin(s) you would like to start below and click on the start button. Make sure to configure your gear in the gear tab below!";
		String gear = "Configure your gear for each plugin below. If any or all slots are left empty your current equipped item for that slot will be used!";
		String teleports = "Configure the teleports that you have available on your account, these gear setups are used when transitioning between plugins!";

		JMultilineLabel description = new JMultilineLabel();
		description.setText(startPlugins);
		description.setFont(ChinManagerPanel.SMALL_FONT);
		description.setDisabledTextColor(Color.WHITE);
		description.setBorder(new EmptyBorder(0, 10, 5, 10));

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Start plugins", ChinManagerPanel.wrapContainer(pluginPanel));
		tabbedPane.add("Gear", ChinManagerPanel.wrapContainer(equipmentPanel));
		tabbedPane.add("Teleports", ChinManagerPanel.wrapContainer(teleportsPanel));

		tabbedPane.addChangeListener((change) -> {
			switch (tabbedPane.getSelectedIndex())
			{
				case 0:
					description.setText(startPlugins);
					break;
				case 1:
					description.setText(gear);
					break;
				case 2:
					description.setText(teleports);
					break;
			}

			description.revalidate();
			description.repaint();
		});

		add(description, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH, super.getPreferredSize().height);
	}
}
