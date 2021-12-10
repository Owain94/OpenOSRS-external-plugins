package com.owain.chinmanager.ui.plugins.breaks;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import static com.owain.chinmanager.ui.ChinManagerPanel.PANEL_BACKGROUND_COLOR;
import com.owain.chinmanager.ui.plugins.options.OptionsConfig;
import com.owain.chinmanager.ui.utils.ConfigPanel;
import com.owain.chinmanager.ui.utils.Separator;
import com.owain.chinmanager.ui.utils.SwingScheduler;
import com.owain.chinmanager.utils.IntRandomNumberGenerator;
import com.owain.chinmanager.utils.Plugins;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;

public class BreakOptionsPanel extends JPanel
{
	public static final CompositeDisposable DISPOSABLES = new CompositeDisposable();
	public static final CompositeDisposable BREAK_OPTIONS_DISPOSABLES = new CompositeDisposable();

	private final SwingScheduler swingScheduler;
	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	private final ConfigManager configManager;
	private final JPanel optionsPanel;
	private final JPanel contentPanel = new JPanel(new BorderLayout());

	@Inject
	BreakOptionsPanel(SwingScheduler swingScheduler, ChinManagerPlugin chinManagerPlugin, ChinManager chinManager, ConfigPanel optionsPanel)
	{
		this.swingScheduler = swingScheduler;
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;
		this.configManager = chinManagerPlugin.getConfigManager();
		this.optionsPanel = optionsPanel;

		optionsPanel.init(chinManagerPlugin.getConfigManager().getConfig(OptionsConfig.class));

		setLayout(new BorderLayout());
		setBackground(PANEL_BACKGROUND_COLOR);

		contentPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		add(contentPanel, BorderLayout.CENTER);

		breakOptionsPanel();

		DISPOSABLES.addAll(
			chinManager
				.getActiveObservable()
				.observeOn(swingScheduler)
				.subscribe((ignored) -> breakOptionsPanel()),

			chinManager
				.configChanged
				.subscribe(this::onConfigChanged)
		);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH, super.getPreferredSize().height);
	}

	private void breakOptionsPanel()
	{
		contentPanel.removeAll();
		BREAK_OPTIONS_DISPOSABLES.clear();

		for (Plugin plugin : chinManager.getActiveSortedPlugins())
		{
			Map<Plugin, Boolean> plugins = chinManager.getPlugins();

			if (!plugins.containsKey(plugin))
			{
				continue;
			}

			if (!plugins.get(plugin))
			{
				continue;
			}

			BreaksPanel breaksPanel = new BreaksPanel(
				BREAK_OPTIONS_DISPOSABLES,
				swingScheduler,
				chinManager,
				chinManagerPlugin,
				plugin
			);
			breaksPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

			contentPanel.add(breaksPanel, BorderLayout.NORTH);

			Separator sep = new Separator();
			sep.setBorder(new EmptyBorder(5, 10, 0, 10));

			contentPanel.add(sep, BorderLayout.CENTER);
		}

		contentPanel.add(optionsPanel, BorderLayout.SOUTH);

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	private void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged == null || !configChanged.getGroup().equals(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER))
		{
			return;
		}

		Plugin plugin = chinManager.getPlugin(configChanged.getKey().replace("-enabled", ""));

		if (plugin == null)
		{
			return;
		}

		if (configChanged.getKey().contains("enabled") && configChanged.getNewValue().equals("false"))
		{
			chinManager.removePlannedBreak(plugin);
		}
		else if (configChanged.getKey().contains("threshold"))
		{
			chinManager.removePlannedBreak(plugin);
		}
		else if (configChanged.getKey().contains("break"))
		{
			int breakFrom = Integer.parseInt(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(plugin) + "-breakfrom")) * 60;
			int breakTo = Integer.parseInt(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(plugin) + "-breakto")) * 60;

			int breakRandom = new IntRandomNumberGenerator(breakFrom, breakTo).nextInt();

			chinManager.updatePlannedBreakValue(plugin, breakRandom);
		}
	}
}
