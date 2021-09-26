/*
 * Created by JFormDesigner on Sat Aug 01 23:27:36 CEST 2020
 */

package com.owain.chinbreakhandler.ui;

import com.owain.chinbreakhandler.ChinBreakHandlerPlugin;
import static com.owain.chinbreakhandler.ChinBreakHandlerPlugin.isNumeric;
import static com.owain.chinbreakhandler.ChinBreakHandlerPlugin.sanitizedName;
import static com.owain.chinbreakhandler.ui.ChinBreakHandlerPanel.BACKGROUND_COLOR;
import static com.owain.chinbreakhandler.ui.ChinBreakHandlerPanel.NORMAL_FONT;
import static com.owain.chinbreakhandler.ui.ChinBreakHandlerPanel.PANEL_BACKGROUND_COLOR;
import static com.owain.chinbreakhandler.ui.ChinBreakHandlerPanel.SMALL_FONT;
import com.owain.chinbreakhandler.ui.utils.JMultilineLabel;
import com.owain.chinbreakhandler.ui.utils.OnOffToggleButton;
import com.owain.chinbreakhandler.ui.utils.UnitFormatterFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ToggleButton;

public class ChinBreakHandlerPluginPanel extends JPanel
{
	private final ConfigManager configManager;
	private final Plugin plugin;
	private final boolean configurable;

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PluginPanel.PANEL_WIDTH, super.getPreferredSize().height);
	}

	ChinBreakHandlerPluginPanel(ChinBreakHandlerPlugin chinBreakHandlerPlugin, Plugin plugin, boolean configurable)
	{
		this.configManager = chinBreakHandlerPlugin.getConfigManager();
		this.plugin = plugin;
		this.configurable = configurable;

		if (configurable)
		{
			setupDefaults();
		}

		setLayout(new BorderLayout());
		setBackground(BACKGROUND_COLOR);

		init();
	}

	private void init()
	{
		JPanel titleWrapper = new JPanel(new BorderLayout());
		titleWrapper.setBackground(BACKGROUND_COLOR);
		titleWrapper.setBorder(new CompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, PANEL_BACKGROUND_COLOR),
			BorderFactory.createLineBorder(BACKGROUND_COLOR)
		));

		JLabel title = new JLabel();
		title.setText(plugin.getName());
		title.setFont(NORMAL_FONT);
		title.setPreferredSize(new Dimension(0, 24));
		title.setForeground(Color.WHITE);
		title.setBorder(new EmptyBorder(0, 8, 0, 0));

		JPanel titleActions = new JPanel(new BorderLayout(3, 0));
		titleActions.setBackground(BACKGROUND_COLOR);
		titleActions.setBorder(new EmptyBorder(0, 0, 0, 8));

		if (configurable)
		{
			String pluginName = sanitizedName(plugin);

			JToggleButton onOffToggle = new OnOffToggleButton();

			onOffToggle.setSelected(Boolean.parseBoolean(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-enabled")));
			onOffToggle.addItemListener(i ->
				configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-enabled", onOffToggle.isSelected()));

			titleActions.add(onOffToggle, BorderLayout.EAST);
		}

		titleWrapper.add(title, BorderLayout.CENTER);
		titleWrapper.add(titleActions, BorderLayout.EAST);

		add(titleWrapper, BorderLayout.NORTH);

		if (configurable)
		{
			add(breakPanel(), BorderLayout.CENTER);
			add(typePanel(), BorderLayout.SOUTH);
		}
		else
		{
			add(notConfigurable(), BorderLayout.CENTER);
		}
	}

	private JSpinner createSpinner(int value)
	{
		SpinnerModel model = new SpinnerNumberModel(value, 0, Integer.MAX_VALUE, 1);
		JSpinner spinner = new JSpinner(model);
		Component editor = spinner.getEditor();
		JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();
		spinnerTextField.setColumns(4);
		spinnerTextField.setFormatterFactory(new UnitFormatterFactory(Units.MINUTES));

		return spinner;
	}

	private JPanel notConfigurable()
	{
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBackground(BACKGROUND_COLOR);
		JMultilineLabel description = new JMultilineLabel();

		description.setText("The break timings for this plugin are not configurable.");
		description.setFont(SMALL_FONT);
		description.setDisabledTextColor(Color.WHITE);
		description.setBackground(BACKGROUND_COLOR);

		contentPanel.add(description, BorderLayout.CENTER);

		return contentPanel;
	}

	private JPanel breakPanel()
	{
		String pluginName = sanitizedName(plugin);

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBackground(BACKGROUND_COLOR);
		contentPanel.setBorder(new CompoundBorder(
			new CompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, PANEL_BACKGROUND_COLOR),
				BorderFactory.createLineBorder(BACKGROUND_COLOR)
			), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		JSpinner thresholdFrom = createSpinner(
			parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-thresholdfrom"), 60)
		);

		JSpinner thresholdTo = createSpinner(
			parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-thresholdto"), 120)
		);

		JSpinner breakFrom = createSpinner(
			parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-breakfrom"), 10)
		);

		JSpinner breakTo = createSpinner(
			parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-breakto"), 15)
		);

		thresholdFrom.addChangeListener(e ->
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-thresholdfrom", thresholdFrom.getValue()));

		thresholdTo.addChangeListener(e ->
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-thresholdto", thresholdTo.getValue()));

		breakFrom.addChangeListener(e ->
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-breakfrom", breakFrom.getValue()));

		breakTo.addChangeListener(e ->
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-breakto", breakTo.getValue()));

		GridBagConstraints c = new GridBagConstraints();


		c.insets = new Insets(2, 0, 2, 0);
		c.fill = GridBagConstraints.CENTER;
		c.weightx = 0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 0;
		contentPanel.add(new JLabel("After running for"), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		contentPanel.add(thresholdFrom, c);

		c.fill = GridBagConstraints.CENTER;
		c.weightx = 0.75;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = 1;
		contentPanel.add(new JLabel(" - "), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridwidth = 1;
		c.gridx = 2;
		c.gridy = 1;
		contentPanel.add(thresholdTo, c);

		c.insets = new Insets(8, 0, 2, 0);
		c.fill = GridBagConstraints.CENTER;
		c.weightx = 0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 2;
		contentPanel.add(new JLabel("Schedule a break for"), c);

		c.insets = new Insets(2, 0, 2, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 3;
		contentPanel.add(breakFrom, c);

		c.fill = GridBagConstraints.CENTER;
		c.weightx = 0.75;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = 3;
		contentPanel.add(new JLabel(" - "), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridwidth = 1;
		c.gridx = 2;
		c.gridy = 3;
		contentPanel.add(breakTo, c);

		return contentPanel;
	}

	private JPanel typePanel()
	{
		String pluginName = sanitizedName(plugin);

		JPanel contentPanel = new JPanel(new GridLayout(0, 2));
		contentPanel.setBackground(BACKGROUND_COLOR);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		ButtonGroup buttonGroup = new ButtonGroup();

		JCheckBox logoutButton = new ToggleButton("Logout");
		JCheckBox afkButton = new ToggleButton("AFK");

		boolean logout = Boolean.parseBoolean(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-logout"));

		logoutButton.setSelected(logout);
		afkButton.setSelected(!logout);

		logoutButton.addActionListener(e ->
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-logout", logoutButton.isSelected()));

		afkButton.addActionListener(e ->
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-logout", !afkButton.isSelected()));

		buttonGroup.add(logoutButton);
		buttonGroup.add(afkButton);

		contentPanel.add(logoutButton);
		contentPanel.add(afkButton);

		return contentPanel;
	}

	private void setupDefaults()
	{
		String pluginName = sanitizedName(plugin);

		String enabled = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdfrom");
		String logout = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-logout");

		String thresholdfrom = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdfrom");
		String thresholdto = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdto");
		String breakfrom = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakfrom");
		String breakto = configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakto");

		if (enabled == null)
		{
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-enabled", false);
		}

		if (logout == null)
		{
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-logout", true);
		}

		if (!isNumeric(thresholdfrom) || (isNumeric(thresholdfrom) && Integer.parseInt(thresholdfrom) < 0))
		{
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-thresholdfrom", 60);
		}

		if (!isNumeric(thresholdto) || (isNumeric(thresholdto) && Integer.parseInt(thresholdto) < 0))
		{
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-thresholdto", 120);
		}

		if (!isNumeric(breakfrom) || (isNumeric(breakfrom) && Integer.parseInt(breakfrom) < 0))
		{
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-breakfrom", 10);
		}

		if (!isNumeric(breakto) || (isNumeric(breakto) && Integer.parseInt(breakto) < 0))
		{
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, pluginName + "-breakto", 15);
		}
	}

	private int parseInt(String value, int def)
	{
		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			return def;
		}
	}
}
