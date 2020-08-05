/*
 * Created by JFormDesigner on Sat Aug 01 23:27:36 CEST 2020
 */

package com.owain.chinbreakhandler.ui;

import com.owain.chinbreakhandler.ChinBreakHandlerPlugin;
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
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.PluginPanel;

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
			JToggleButton onOffToggle = new OnOffToggleButton();

			onOffToggle.setSelected(Boolean.parseBoolean(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-enabled")));
			onOffToggle.addItemListener(i ->
				configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-enabled", onOffToggle.isSelected()));

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

	private JSpinner createSpinner(int defaultValue, int min)
	{
		SpinnerModel model = new SpinnerNumberModel(defaultValue, min, Integer.MAX_VALUE, 1);
		JSpinner spinner = new JSpinner(model);
		Component editor = spinner.getEditor();
		JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();
		spinnerTextField.setColumns(4);
		spinnerTextField.setFormatterFactory(new UnitFormatterFactory());

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
		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBackground(BACKGROUND_COLOR);
		contentPanel.setBorder(new CompoundBorder(
			new CompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, PANEL_BACKGROUND_COLOR),
				BorderFactory.createLineBorder(BACKGROUND_COLOR)
			), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		JSpinner thresholdFrom = createSpinner(
			parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdfrom"), 60),
			10
		);

		JSpinner thresholdTo = createSpinner(
			parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdto"), 120),
			10
		);

		JSpinner breakFrom = createSpinner(
			parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakfrom"), 10),
			5
		);

		JSpinner breakTo = createSpinner(
			parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakto"), 15),
			5
		);

		thresholdFrom.addChangeListener(e ->
		{
			if ((int) thresholdFrom.getValue() < (int) thresholdTo.getValue())
			{
				configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdfrom", thresholdFrom.getValue());
			}
			else
			{
				thresholdFrom.setValue(parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdfrom"), 20));
			}
		});

		thresholdTo.addChangeListener(e ->
		{
			if ((int) thresholdTo.getValue() > (int) thresholdFrom.getValue())
			{
				configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdto", thresholdTo.getValue());
			}
			else
			{
				thresholdTo.setValue(parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdto"), 20));
			}
		});

		breakFrom.addChangeListener(e ->
		{
			if ((int) breakFrom.getValue() < (int) breakTo.getValue())
			{
				configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakfrom", breakFrom.getValue());
			}
			else
			{
				breakFrom.setValue(parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakfrom"), 20));
			}
		});

		breakTo.addChangeListener(e ->
		{
			if ((int) breakTo.getValue() > (int) breakFrom.getValue())
			{
				configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakto", breakTo.getValue());
			}
			else
			{
				breakTo.setValue(parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakto"), 20));
			}
		});

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
		JPanel contentPanel = new JPanel(new GridLayout(0, 2));
		contentPanel.setBackground(BACKGROUND_COLOR);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		ButtonGroup buttonGroup = new ButtonGroup();

		JCheckBox logoutButton = new JCheckBox("Logout");
		JCheckBox afkButton = new JCheckBox("AFK");

		boolean logout = Boolean.parseBoolean(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-logout"));

		logoutButton.setSelected(logout);
		afkButton.setSelected(!logout);

		logoutButton.addActionListener(e ->
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-logout", logoutButton.isSelected()));

		afkButton.addActionListener(e ->
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-logout", !afkButton.isSelected()));

		buttonGroup.add(logoutButton);
		buttonGroup.add(afkButton);

		contentPanel.add(logoutButton);
		contentPanel.add(afkButton);

		return contentPanel;
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

	private void setupDefaults()
	{
		if (configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-enabled") == null)
		{
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-enabled", false);
		}

		if (configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdfrom") == null)
		{
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdfrom", 60);
		}

		if (configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdto") == null)
		{
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-thresholdto", 120);
		}

		if (configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakfrom") == null)
		{
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakfrom", 10);
		}

		if (configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakto") == null)
		{
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakto", 15);
		}

		if (configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-logout") == null)
		{
			configManager.setConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-logout", true);
		}
	}
}
