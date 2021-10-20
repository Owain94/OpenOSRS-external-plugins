package com.owain.chinmanager.ui.plugins;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import static com.owain.chinmanager.ui.ChinManagerPanel.NORMAL_FONT;
import static com.owain.chinmanager.ui.ChinManagerPanel.PANEL_BACKGROUND_COLOR;
import static com.owain.chinmanager.ui.ChinManagerPanel.SMALL_FONT;
import static com.owain.chinmanager.ui.ChinManagerPanel.wrapContainer;
import com.owain.chinmanager.ui.plugins.breaks.BreakOptionsPanel;
import com.owain.chinmanager.ui.plugins.status.InfoPanel;
import com.owain.chinmanager.ui.plugins.status.PluginStatusPanel;
import com.owain.chinmanager.ui.teleports.TeleportsConfig;
import com.owain.chinmanager.ui.utils.AbstractButtonSource;
import com.owain.chinmanager.ui.utils.ConfigPanel;
import com.owain.chinmanager.ui.utils.GridBagHelper;
import com.owain.chinmanager.ui.utils.JMultilineLabel;
import com.owain.chinmanager.ui.utils.SwingScheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;

@Slf4j
public class StatusPanel extends JPanel
{
	public static final CompositeDisposable DISPOSABLES = new CompositeDisposable();
	private final SwingScheduler swingScheduler;
	private final ChinManager chinManager;
	private final ConfigManager configManager;
	private final InfoPanel infoPanel;
	private final JPanel headerPanel = new JPanel(new GridBagLayout());
	private final JPanel unlockAccountPanel = new JPanel(new BorderLayout());
	private final JPanel pluginsPanel = new JPanel(new GridBagLayout());
	private final JButton stopPluginsButton = new JButton();

	@Inject
	StatusPanel(
		SwingScheduler swingScheduler,
		ChinManager chinManager,
		InfoPanel infoPanel,
		BreakOptionsPanel breakOptionsPanel,
		ConfigPanel teleportsPanel,
		ConfigManager configManager)
	{
		this.swingScheduler = swingScheduler;
		this.chinManager = chinManager;
		this.configManager = configManager;

		this.infoPanel = infoPanel;

		teleportsPanel.init(configManager.getConfig(TeleportsConfig.class));

		setLayout(new BorderLayout());
		setBackground(PANEL_BACKGROUND_COLOR);

		pluginsPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

		headerPanel();

		add(headerPanel, BorderLayout.NORTH);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBorder(new EmptyBorder(5, 0, 0, 0));
		add(tabbedPane, BorderLayout.CENTER);

		tabbedPane.add("Status", wrapContainer(pluginsPanel));
		tabbedPane.add("Options", wrapContainer(breakOptionsPanel));
		tabbedPane.add("Teleports", wrapContainer(teleportsPanel));

		pluginsPanel();

		DISPOSABLES.addAll(
			chinManager
				.getActiveObservable()
				.observeOn(swingScheduler)
				.subscribe(
					(plugins) -> {
						this.pluginsPanel(plugins);
						headerPanel();
					}),

			chinManager
				.configChanged
				.observeOn(swingScheduler)
				.subscribe(
					(ignored) -> headerPanel()),

			AbstractButtonSource.fromActionOf(stopPluginsButton, swingScheduler)
				.subscribe((e) -> {
					for (Plugin plugin : Set.copyOf(chinManager.getActivePlugins()))
					{
						chinManager.stopPlugin(plugin);
					}

					chinManager.setCurrentlyActive(null);
					chinManager.setAmountOfBreaks(0);
				})
		);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH, super.getPreferredSize().height);
	}

	private boolean unlockAccountsPanel()
	{
		unlockAccountPanel.removeAll();

		Set<Plugin> activePlugins = chinManager.getActivePlugins();

		boolean manual = Boolean.parseBoolean(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection"));

		String data = ChinManagerPlugin.getProfileData();

		if (activePlugins.isEmpty() || manual || (data != null && !data.trim().isEmpty()))
		{
			return false;
		}

		JPanel titleWrapper = new JPanel(new BorderLayout());
		titleWrapper.setBackground(new Color(125, 40, 40));
		titleWrapper.setBorder(new CompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(115, 30, 30)),
			BorderFactory.createLineBorder(new Color(125, 40, 40))
		));

		JLabel title = new JLabel();
		title.setText("Warning");
		title.setFont(NORMAL_FONT);
		title.setPreferredSize(new Dimension(0, 24));
		title.setForeground(Color.WHITE);
		title.setBorder(new EmptyBorder(0, 8, 0, 0));

		titleWrapper.add(title, BorderLayout.CENTER);

		unlockAccountPanel.add(titleWrapper, BorderLayout.NORTH);

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBackground(new Color(125, 40, 40));
		contentPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

		JMultilineLabel description = new JMultilineLabel();

		description.setText("Please make sure to unlock your profiles plugins data in the accounts tab!");
		description.setFont(SMALL_FONT);
		description.setDisabledTextColor(Color.WHITE);
		description.setBackground(new Color(115, 30, 30));

		description.setBorder(new EmptyBorder(5, 5, 10, 5));

		contentPanel.add(description, BorderLayout.CENTER);

		unlockAccountPanel.add(contentPanel, BorderLayout.CENTER);

		unlockAccountPanel.revalidate();
		unlockAccountPanel.repaint();

		return true;
	}

	private void headerPanel()
	{
		headerPanel.removeAll();
		headerPanel.setBorder(new EmptyBorder(5, 10, 0, 10));

		if (unlockAccountsPanel())
		{
			GridBagHelper.addComponent(headerPanel,
				unlockAccountPanel,
				0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0));
		}

		GridBagHelper.addComponent(headerPanel,
			infoPanel,
			0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0));

		GridBagHelper.addComponent(headerPanel,
			stopPluginsButton,
			0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 0, 5, 0));

		headerPanel.revalidate();
		headerPanel.repaint();
	}

	private void pluginsPanel()
	{
		Set<Plugin> plugins = chinManager.getActivePlugins();
		pluginsPanel(plugins);
	}

	private void pluginsPanel(Set<Plugin> plugins)
	{
		pluginsPanel.removeAll();

		int counter = 0;

		stopPluginsButton.setText(plugins.size() > 1 ? "Stop plugins" : "Stop plugin");

		for (Plugin plugin : plugins)
		{
			GridBagHelper.addComponent(pluginsPanel,
				new PluginStatusPanel(swingScheduler, chinManager, plugin),
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 0, 0));

			counter++;
		}

		pluginsPanel.revalidate();
		pluginsPanel.repaint();
	}
}
