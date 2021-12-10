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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import net.runelite.client.util.ImageUtil;

@Slf4j
public class StatusPanel extends JPanel
{
	public static final CompositeDisposable DISPOSABLES = new CompositeDisposable();

	private static final ImageIcon DELETE_ICON;
	private static final ImageIcon DELETE_HOVER_ICON;

	private final SwingScheduler swingScheduler;
	private final ChinManager chinManager;
	private final ConfigManager configManager;
	private final InfoPanel infoPanel;
	private final JPanel headerPanel = new JPanel(new GridBagLayout());
	private final JPanel pluginsPanel = new JPanel(new GridBagLayout());
	private final JButton stopPluginsButton = new JButton();

	static
	{
		final BufferedImage deleteImg =
			ImageUtil.recolorImage(
				ImageUtil.resizeCanvas(
					ImageUtil.loadImageResource(StatusPanel.class, "delete_icon.png"), 14, 14
				), Color.WHITE
			);
		DELETE_ICON = new ImageIcon(deleteImg);
		DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImg, 0.53f));
	}

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
					(ignored) -> {
						this.pluginsPanel();
						headerPanel();
					}),

			chinManager
				.configChanged
				.observeOn(swingScheduler)
				.subscribe(
					(ignored) -> headerPanel()),

			chinManager
				.getWarningsObservable()
				.observeOn(swingScheduler)
				.subscribe(
					(ignored) -> this.pluginsPanel()),

			AbstractButtonSource.fromActionOf(stopPluginsButton, swingScheduler)
				.subscribe((e) -> {
					for (Plugin plugin : Set.copyOf(chinManager.getActiveSortedPlugins()))
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
		Set<Plugin> activePlugins = chinManager.getActiveSortedPlugins();

		boolean manual = Boolean.parseBoolean(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection"));

		String data = ChinManagerPlugin.getProfileData();

		return !activePlugins.isEmpty() && !manual && (data == null || data.trim().isEmpty());
	}

	private JPanel warningPanel(String titleText, String descriptionText, boolean border)
	{
		JPanel warningPanel = new JPanel(new BorderLayout());
		if (border)
		{
			warningPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
		}

		JPanel titleWrapper = new JPanel(new BorderLayout());
		titleWrapper.setBackground(new Color(125, 40, 40));
		titleWrapper.setBorder(new CompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(115, 30, 30)),
			BorderFactory.createLineBorder(new Color(125, 40, 40))
		));

		JLabel title = new JLabel();
		title.setText(titleText);
		title.setFont(NORMAL_FONT);
		title.setPreferredSize(new Dimension(0, 24));
		title.setForeground(Color.WHITE);
		title.setBorder(new EmptyBorder(0, 8, 0, 0));

		titleWrapper.add(title, BorderLayout.CENTER);

		if (border)
		{
			JPanel titleActions = new JPanel(new BorderLayout(3, 0));
			titleActions.setBackground(new Color(125, 40, 40));
			titleActions.setBorder(new EmptyBorder(0, 0, 0, 8));

			JLabel delete = new JLabel();
			delete.setIcon(DELETE_ICON);

			delete.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					chinManager.removeWarning(titleText);
				}

				@Override
				public void mouseEntered(MouseEvent e)
				{
					delete.setIcon(DELETE_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent e)
				{
					delete.setIcon(DELETE_ICON);
				}
			});

			titleActions.add(delete, BorderLayout.EAST);

			titleWrapper.add(titleActions, BorderLayout.EAST);
		}

		warningPanel.add(titleWrapper, BorderLayout.NORTH);

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBackground(new Color(125, 40, 40));
		contentPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

		JMultilineLabel description = new JMultilineLabel();

		description.setText(descriptionText);
		description.setFont(SMALL_FONT);
		description.setDisabledTextColor(Color.WHITE);
		description.setBackground(new Color(115, 30, 30));

		description.setBorder(new EmptyBorder(5, 5, 10, 5));

		contentPanel.add(description, BorderLayout.CENTER);

		warningPanel.add(contentPanel, BorderLayout.CENTER);

		return warningPanel;
	}

	private int warningPanels(int counter)
	{
		for (Map.Entry<String, String> warning : chinManager.getWarnings().entrySet())
		{
			counter += 1;
			GridBagHelper.addComponent(pluginsPanel,
				warningPanel(warning.getKey(), warning.getValue(), true),
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0));
		}

		return counter;
	}

	private void headerPanel()
	{
		headerPanel.removeAll();
		headerPanel.setBorder(new EmptyBorder(5, 10, 0, 10));

		int i = 0;
		if (unlockAccountsPanel())
		{
			GridBagHelper.addComponent(headerPanel,
				warningPanel("Account issue!", "Please make sure to unlock your profiles plugins data in the accounts tab!", false),
				0, i, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0));
		}

		i += 1;
		GridBagHelper.addComponent(headerPanel,
			infoPanel,
			0, i, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0));

		i += 1;
		GridBagHelper.addComponent(headerPanel,
			stopPluginsButton,
			0, i, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 0, 5, 0));

		headerPanel.revalidate();
		headerPanel.repaint();
	}

	private void pluginsPanel()
	{
		Set<Plugin> plugins = chinManager.getActiveSortedPlugins();
		pluginsPanel(plugins);
	}

	private void pluginsPanel(Set<Plugin> plugins)
	{
		pluginsPanel.removeAll();

		int counter = -1;

		stopPluginsButton.setText(plugins.size() > 1 ? "Stop plugins" : "Stop plugin");

		counter = warningPanels(counter);

		for (Plugin plugin : plugins)
		{
			counter++;
			GridBagHelper.addComponent(pluginsPanel,
				new PluginStatusPanel(swingScheduler, chinManager, plugin),
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 0, 0));

		}

		pluginsPanel.revalidate();
		pluginsPanel.repaint();
	}
}
