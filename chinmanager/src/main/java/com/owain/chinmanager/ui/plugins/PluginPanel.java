package com.owain.chinmanager.ui.plugins;

import com.openosrs.client.events.OPRSPluginChanged;
import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import static com.owain.chinmanager.ui.ChinManagerPanel.PANEL_BACKGROUND_COLOR;
import static com.owain.chinmanager.ui.ChinManagerPanel.SMALL_FONT;
import com.owain.chinmanager.ui.utils.Components;
import com.owain.chinmanager.ui.utils.GridBagHelper;
import com.owain.chinmanager.ui.utils.JMultilineLabel;
import com.owain.chinmanager.ui.utils.Separator;
import com.owain.chinmanager.utils.Plugins;
import io.reactivex.rxjava3.disposables.Disposable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;
import net.runelite.client.ui.overlay.Overlay;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public class PluginPanel extends JPanel
{
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH, super.getPreferredSize().height);
	}

	public static final List<Disposable> DISPOSABLES = new ArrayList<>();
	public static final Map<String, Pair<Boolean, Integer>> PLUGIN_CONFIG_MAP = new HashMap<>();

	private static final ImageIcon CONFIG_ICON;
	private static final ImageIcon CONFIG_ICON_HOVER;

	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	private final EventBus eventBus;
	private final ConfigManager configManager;

	private final JPanel contentPanel = new JPanel(new GridBagLayout());
	private final JPanel pluginOrderPanel = new JPanel(new GridBagLayout());
	private final JPanel requiredItemsPanel = new JPanel(new GridBagLayout());

	static
	{
		BufferedImage configIcon = ImageUtil.loadImageResource(PluginPanel.class, "config_edit_icon.png");
		CONFIG_ICON = new ImageIcon(configIcon);
		CONFIG_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(configIcon, -100));
	}

	@Inject
	PluginPanel(ChinManagerPlugin chinManagerPlugin, ChinManager chinManager)
	{
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;
		this.eventBus = chinManagerPlugin.getEventBus();
		this.configManager = chinManagerPlugin.getConfigManager();

		setLayout(new BorderLayout());
		setBackground(PANEL_BACKGROUND_COLOR);

		Disposable pluginManager = chinManager
			.getManagerPluginObservable()
			.subscribe((plugins) -> {
				updatePluginConfigMap();

				SwingUtil.syncExec(this::pluginsPanel);
			});

		DISPOSABLES.add(pluginManager);

		contentPanel.setBorder(new EmptyBorder(5, 10, 0, 10));
		add(contentPanel, BorderLayout.CENTER);

		pluginsPanel();
	}

	@Subscribe
	public void onOPRSPluginChanged(OPRSPluginChanged externalPluginChanged)
	{
		updatePluginConfigMap();

		try
		{
			SwingUtil.syncExec(this::pluginsPanel);
		}
		catch (InvocationTargetException | InterruptedException ignored)
		{
		}
	}

	private void updatePluginConfigMap()
	{
		for (Plugin plugin : chinManager.getManagerPlugins())
		{
			String pluginName = Plugins.sanitizedName(plugin);

			if (!PLUGIN_CONFIG_MAP.containsKey(pluginName))
			{
				PLUGIN_CONFIG_MAP.put(pluginName, new ImmutablePair<>(
					Boolean.parseBoolean(chinManager.getPluginConfig().get(plugin).get("combiningAvailable")),
					Integer.parseInt(chinManager.getPluginConfig().get(plugin).get("combiningPriority"), 10)
				));
			}
		}
	}

	private void pluginsPanel()
	{
		contentPanel.removeAll();

		Set<Plugin> plugins = chinManager.getManagerPlugins();
		int counter = 0;

		if (plugins.isEmpty())
		{
			JMultilineLabel noPlugins = new JMultilineLabel();
			noPlugins.setText("There were no plugins that registered themselves.");
			noPlugins.setFont(SMALL_FONT);
			noPlugins.setDisabledTextColor(Color.WHITE);

			GridBagHelper.addComponent(contentPanel,
				noPlugins,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(10, 0, 0, 0));
		}
		else
		{
			for (Plugin plugin : plugins)
			{
				JPanel item = new JPanel();
				item.setLayout(new BorderLayout());
				item.setMinimumSize(new Dimension(PANEL_WIDTH, 0));

				JCheckBox checkbox = new JCheckBox(plugin.getName());
				checkbox.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
				checkbox.addActionListener(e ->
				{
					checkboxState();
					startButtonState();
				});

				if (plugins.size() == 1)
				{
					checkbox.setSelected(true);
					checkbox.setEnabled(false);
				}
				else
				{
					checkbox.setSelected(Boolean.parseBoolean(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, Plugins.sanitizedName(checkbox.getText()) + "Checkbox")));
				}

				item.add(checkbox, BorderLayout.CENTER);

				JButton configButton = new JButton(CONFIG_ICON);
				configButton.setRolloverIcon(CONFIG_ICON_HOVER);
				SwingUtil.removeButtonDecorations(configButton);
				configButton.setPreferredSize(new Dimension(25, 0));
				configButton.setToolTipText("Edit plugin configuration");

				configButton.addActionListener(e ->
				{
					configButton.setIcon(CONFIG_ICON);
					openGroupConfigPanel(plugin);
				});

				item.add(configButton, BorderLayout.EAST);

				GridBagHelper.addComponent(contentPanel,
					item,
					0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 0, 0, 0));
				counter++;
			}

			GridBagHelper.addComponent(contentPanel,
				pluginOrderPanel,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 0, 0));
			counter++;

			GridBagHelper.addComponent(contentPanel,
				requiredItemsPanel,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 0, 0));
			counter++;

			final JButton resetButton = new JButton();
			resetButton.setText("Reset");
			resetButton.setEnabled(plugins.size() > 1);
			resetButton.addActionListener(e -> resetCheckboxes());
			GridBagHelper.addComponent(contentPanel,
				resetButton,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(10, 0, 0, 0));
			counter++;

			final JButton startPluginsButton = new JButton();
			startPluginsButton.setText("Start plugin");
			startPluginsButton.setEnabled(
				Components.findComponents(contentPanel, JCheckBox.class)
					.stream()
					.anyMatch(AbstractButton::isSelected)
			);
			startPluginsButton.addActionListener(e -> {
					chinManager.setAmountOfBreaks(0);
					chinManager.startPlugins(
						Components.findComponents(contentPanel, JCheckBox.class)
							.stream()
							.filter(AbstractButton::isSelected)
							.map(AbstractButton::getText)
							.map(Plugins::sanitizedName)
							.map(chinManager::getPlugin)
							.filter(Objects::nonNull)
							.collect(Collectors.toList())
					);

					chinManager.setCurrentlyActive(chinManagerPlugin);
				}
			);

			GridBagHelper.addComponent(contentPanel,
				startPluginsButton,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 0, 0));
		}

		checkboxState();
	}

	private void openGroupConfigPanel(Plugin plugin)
	{
		Overlay overlay = new Overlay(plugin)
		{
			@Override
			public Dimension render(Graphics2D graphics)
			{
				return null;
			}
		};

		eventBus.post(new OverlayMenuClicked(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, ""), overlay));
	}

	private void pluginOrder()
	{
		pluginOrderPanel.removeAll();

		Set<Plugin> selectedPlugins = new TreeSet<>(
			(plugin1, plugin2) -> {
				Long p1 = (long) PLUGIN_CONFIG_MAP.get(Plugins.sanitizedName(plugin1)).getRight();
				Long p2 = (long) PLUGIN_CONFIG_MAP.get(Plugins.sanitizedName(plugin2)).getRight();

				return p2.compareTo(p1);
			});

		selectedPlugins.addAll(
			Components.findComponents(contentPanel, JCheckBox.class)
				.stream()
				.filter(AbstractButton::isSelected)
				.map(AbstractButton::getText)
				.map(Plugins::sanitizedName)
				.map(chinManager::getPlugin)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet())
		);

		if (selectedPlugins.size() > 0)
		{
			int counter = 0;

			GridBagHelper.addComponent(pluginOrderPanel,
				new Separator(),
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 0, 0));
			counter++;

			final JLabel startLocation = new JLabel("Start location");
			startLocation.setForeground(ColorScheme.BRAND_ORANGE);
			startLocation.setFont(FontManager.getRunescapeBoldFont());
			startLocation.setBorder(new EmptyBorder(0, 0, 0, 1));

			GridBagHelper.addComponent(pluginOrderPanel,
				startLocation,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 10, 0));
			counter++;

			JMultilineLabel actualStartLocation = new JMultilineLabel();
			actualStartLocation.setText(chinManager.getPluginConfig().get(selectedPlugins.stream().findFirst().get()).get("startLocation"));
			actualStartLocation.setDisabledTextColor(Color.WHITE);

			GridBagHelper.addComponent(pluginOrderPanel,
				actualStartLocation,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 0, 0));
			counter++;


			if (selectedPlugins.size() > 1)
			{
				final JLabel pluginOrder = new JLabel("Plugin order");
				pluginOrder.setForeground(ColorScheme.BRAND_ORANGE);
				pluginOrder.setFont(FontManager.getRunescapeBoldFont());
				pluginOrder.setBorder(new EmptyBorder(3, 0, 0, 1));

				GridBagHelper.addComponent(pluginOrderPanel,
					pluginOrder,
					0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 0, 0, 0));
				counter++;

				for (Plugin plugin : selectedPlugins)
				{
					GridBagHelper.addComponent(pluginOrderPanel,
						new JLabel(counter - 3 + ". " + plugin.getName()),
						0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 0, 0));
					counter++;
				}
			}
		}

		pluginOrderPanel.revalidate();
		pluginOrderPanel.repaint();
	}

	private void requiredItems()
	{
		requiredItemsPanel.removeAll();

		Set<Plugin> selectedPlugins = Components.findComponents(contentPanel, JCheckBox.class)
			.stream()
			.filter(AbstractButton::isSelected)
			.map(AbstractButton::getText)
			.map(Plugins::sanitizedName)
			.map(chinManager::getPlugin)
			.collect(Collectors.toSet());

//		if (items.size() > 0 &&
//			selectedPlugins.size() > 1 &&
//			selectedPlugins
//				.stream()
//				.map((plugin) -> chinManager.getPluginConfig().get(plugin).get("startLocation"))
//				.collect(Collectors.toSet()).size() > 1)
//		{
//			int counter = 0;
//
//			GridBagHelper.addComponent(requiredItemsPanel,
//				new Separator(),
//				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//				new Insets(5, 0, 0, 0));
//			counter++;
//
//			GridBagHelper.addComponent(requiredItemsPanel,
//				new JLabel("Required transitioning items:"),
//				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//				new Insets(5, 0, 0, 0));
//			counter++;
//
//			GridBagHelper.addComponent(requiredItemsPanel,
//				new JLabel(String.join(", ", items)),
//				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//				new Insets(5, 0, 0, 0));
//		}

		requiredItemsPanel.revalidate();
		requiredItemsPanel.repaint();
	}

	private void resetCheckboxes()
	{
		Components.findComponents(contentPanel, JCheckBox.class).forEach((checkbox) -> {
			checkbox.setEnabled(true);
			checkbox.setSelected(false);
		});
		checkboxState();
		startButtonState();
	}

	private void checkboxState()
	{
		updatePluginConfigMap();

		List<JCheckBox> checkboxes = Components.findComponents(contentPanel, JCheckBox.class);

		if (checkboxes.size() == 0 || checkboxes.stream() == null)
		{
			return;
		}

		if (checkboxes
			.stream()
			.anyMatch((checkbox) ->
				checkbox.isSelected() &&
					PLUGIN_CONFIG_MAP.containsKey(Plugins.sanitizedName(checkbox.getText())) &&
					!PLUGIN_CONFIG_MAP.get(
						Plugins.sanitizedName(checkbox.getText())
					)
						.getLeft()))
		{
			checkboxes.forEach((checkbox) -> {
				if (!checkbox.isSelected())
				{
					checkbox.setEnabled(false);
				}
			});
		}
		else if (checkboxes.stream().anyMatch(AbstractButton::isSelected))
		{
			checkboxes.forEach((checkbox) -> {
				if (PLUGIN_CONFIG_MAP.containsKey(Plugins.sanitizedName(checkbox.getText())) && !PLUGIN_CONFIG_MAP.get(Plugins.sanitizedName(checkbox.getText())).getLeft())
				{
					checkbox.setSelected(false);
					checkbox.setEnabled(false);
				}
				else
				{
					checkbox.setEnabled(true);
				}
			});
		}
		else
		{
			checkboxes.forEach((checkbox) -> checkbox.setEnabled(true));
		}

		if (checkboxes.size() == 1)
		{
			checkboxes.forEach((checkbox) -> {
				checkbox.setEnabled(true);
				checkbox.setSelected(true);
			});
		}

		List<Integer> selectedPriorities = checkboxes.stream().filter(AbstractButton::isSelected).filter((checkbox) -> PLUGIN_CONFIG_MAP.containsKey(Plugins.sanitizedName(checkbox.getText()))).map((checkbox) -> PLUGIN_CONFIG_MAP.get(Plugins.sanitizedName(checkbox.getText())).getRight()).collect(Collectors.toList());
		List<Integer> verifyPriorities = new ArrayList<>();

		checkboxes.forEach((checkbox) -> {
			if (checkbox == null)
			{
				return;
			}

			String pluginName = Plugins.sanitizedName(checkbox.getText());

			if (!PLUGIN_CONFIG_MAP.containsKey(pluginName))
			{
				return;
			}

			if (!checkbox.isSelected() &&
				checkbox.isEnabled() &&
				selectedPriorities.contains(PLUGIN_CONFIG_MAP.get(pluginName).getRight()))
			{
				checkbox.setEnabled(false);
				checkbox.setSelected(false);
			}

			if (checkboxes.size() != 1)
			{
				configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, pluginName + "Checkbox", checkbox.isSelected());
			}

			if (checkbox.isSelected() && !verifyPriorities.contains(PLUGIN_CONFIG_MAP.get(pluginName).getRight()))
			{
				verifyPriorities.add(PLUGIN_CONFIG_MAP.get(pluginName).getRight());
			}
			else if (checkbox.isSelected() && verifyPriorities.contains(PLUGIN_CONFIG_MAP.get(pluginName).getRight()))
			{
				resetCheckboxes();
			}
		});

		Optional<JButton> startButton = getStartButton();

		startButton.ifPresent(button -> button
			.setText(checkboxes.stream().filter(AbstractButton::isSelected).count() > 1 ? "Start plugins" : "Start plugin"));

		pluginOrder();
		requiredItems();
	}

	private Optional<JButton> getStartButton()
	{
		return Components.findComponents(contentPanel, JButton.class)
			.stream()
			.filter(
				(button) ->
					button.getText().equals("Start plugin") || button.getText().equals("Start plugins")
			)
			.findFirst();
	}

	private void startButtonState()
	{
		Optional<JButton> startButton = getStartButton();

		startButton.ifPresent(button -> button
			.setEnabled(
				Components.findComponents(contentPanel, JCheckBox.class)
					.stream()
					.anyMatch(AbstractButton::isSelected)
			));
	}
}
