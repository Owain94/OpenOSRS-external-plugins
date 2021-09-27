package com.owain.chinmanager.ui.plugins.breaks;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ui.ChinManagerPanel;
import static com.owain.chinmanager.ui.ChinManagerPanel.PANEL_BACKGROUND_COLOR;
import com.owain.chinmanager.ui.utils.OnOffToggleButton;
import com.owain.chinmanager.ui.utils.UnitFormatterFactory;
import com.owain.chinmanager.utils.Integers;
import com.owain.chinmanager.utils.Plugins;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import static java.lang.Integer.parseInt;
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

public class BreaksPanel extends JPanel
{
	private final ChinManager chinManager;
	private final ConfigManager configManager;
	private final Plugin plugin;

	BreaksPanel(ChinManager chinManager, ChinManagerPlugin chinManagerPlugin, Plugin plugin)
	{
		this.chinManager = chinManager;
		this.configManager = chinManagerPlugin.getConfigManager();
		this.plugin = plugin;

		setupDefaults();

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 10, 0, 10));
		setBackground(PANEL_BACKGROUND_COLOR);

		init();
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PluginPanel.PANEL_WIDTH, super.getPreferredSize().height);
	}

	private void init()
	{
		JPanel titleWrapper = new JPanel(new BorderLayout());
		titleWrapper.setBackground(ChinManagerPanel.BACKGROUND_COLOR);
		titleWrapper.setBorder(new CompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, ChinManagerPanel.PANEL_BACKGROUND_COLOR),
			BorderFactory.createLineBorder(ChinManagerPanel.BACKGROUND_COLOR)
		));

		JLabel title = new JLabel();
		title.setText(plugin.getName());
		title.setFont(ChinManagerPanel.NORMAL_FONT);
		title.setPreferredSize(new Dimension(0, 24));
		title.setForeground(Color.WHITE);
		title.setBorder(new EmptyBorder(0, 8, 0, 0));

		JPanel titleActions = new JPanel(new BorderLayout(3, 0));
		titleActions.setBackground(ChinManagerPanel.BACKGROUND_COLOR);
		titleActions.setBorder(new EmptyBorder(0, 0, 0, 8));

		JToggleButton onOffToggle = new OnOffToggleButton();

		String pluginName = Plugins.sanitizedName(plugin);

		onOffToggle.setSelected(Boolean.parseBoolean(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-enabled")));
		onOffToggle.addItemListener(i ->
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-enabled", onOffToggle.isSelected()));

		titleActions.add(onOffToggle, BorderLayout.EAST);

		titleWrapper.add(title, BorderLayout.CENTER);
		titleWrapper.add(titleActions, BorderLayout.EAST);

		add(titleWrapper, BorderLayout.NORTH);

		add(breakPanel(), BorderLayout.CENTER);
		if (chinManager.getActivePlugins().size() == 1)
		{
			add(typePanel(), BorderLayout.SOUTH);
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

	private JPanel breakPanel()
	{
		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBackground(ChinManagerPanel.BACKGROUND_COLOR);
		contentPanel.setBorder(new CompoundBorder(
			new CompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, ChinManagerPanel.PANEL_BACKGROUND_COLOR),
				BorderFactory.createLineBorder(ChinManagerPanel.BACKGROUND_COLOR)
			), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		String pluginName = Plugins.sanitizedName(plugin);

		JSpinner thresholdFrom = createSpinner(
			parseInt(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-thresholdfrom"), 10)
		);

		JSpinner thresholdTo = createSpinner(
			parseInt(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-thresholdto"), 10)
		);

		JSpinner breakFrom = createSpinner(
			parseInt(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-breakfrom"), 10)
		);

		JSpinner breakTo = createSpinner(
			parseInt(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-breakto"), 10)
		);

		thresholdFrom.addChangeListener(e ->
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-thresholdfrom", thresholdFrom.getValue()));

		thresholdTo.addChangeListener(e ->
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-thresholdto", thresholdTo.getValue()));

		breakFrom.addChangeListener(e ->
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-breakfrom", breakFrom.getValue()));

		breakTo.addChangeListener(e ->
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-breakto", breakTo.getValue()));

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
		contentPanel.setBackground(ChinManagerPanel.BACKGROUND_COLOR);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		ButtonGroup buttonGroup = new ButtonGroup();

		JCheckBox logoutButton = new ToggleButton("Logout");
		JCheckBox afkButton = new ToggleButton("AFK");

		String pluginName = Plugins.sanitizedName(plugin);

		boolean logout = Boolean.parseBoolean(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-logout"));

		logoutButton.setSelected(logout);
		afkButton.setSelected(!logout);

		logoutButton.addActionListener(e ->
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-logout", logoutButton.isSelected()));

		afkButton.addActionListener(e ->
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-logout", !afkButton.isSelected()));

		buttonGroup.add(logoutButton);
		buttonGroup.add(afkButton);

		contentPanel.add(logoutButton);
		contentPanel.add(afkButton);

		return contentPanel;
	}

	private void setupDefaults()
	{
		String pluginName = Plugins.sanitizedName(plugin);

		String enabled = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-thresholdfrom");
		String logout = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-logout");

		String thresholdfrom = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-thresholdfrom");
		String thresholdto = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-thresholdto");
		String breakfrom = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-breakfrom");
		String breakto = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-breakto");

		if (enabled == null)
		{
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-enabled", false);
		}

		if (logout == null)
		{
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-logout", true);
		}

		if (!Integers.isNumeric(thresholdfrom) || (Integers.isNumeric(thresholdfrom) && parseInt(thresholdfrom) < 0))
		{
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-thresholdfrom", 60);
		}

		if (!Integers.isNumeric(thresholdto) || (Integers.isNumeric(thresholdto) && parseInt(thresholdto) < 0))
		{
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-thresholdto", 120);
		}

		if (!Integers.isNumeric(breakfrom) || (Integers.isNumeric(breakfrom) && parseInt(breakfrom) < 0))
		{
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-breakfrom", 10);
		}

		if (!Integers.isNumeric(breakto) || (Integers.isNumeric(breakto) && parseInt(breakto) < 0))
		{
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, pluginName + "-breakto", 15);
		}
	}
}
