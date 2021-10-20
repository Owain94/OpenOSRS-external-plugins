package com.owain.chinmanager.ui.plugins.status;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import static com.owain.chinmanager.ui.ChinManagerPanel.PANEL_BACKGROUND_COLOR;
import static com.owain.chinmanager.ui.utils.GridBagHelper.addComponent;
import com.owain.chinmanager.ui.utils.SwingScheduler;
import static com.owain.chinmanager.ui.utils.Time.formatDuration;
import com.owain.chinmanager.utils.Plugins;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public class InfoPanel extends JPanel
{
	public static final CompositeDisposable DISPOSABLES = new CompositeDisposable();
	public static boolean breakShowing;
	public final JLabel breakingTimeLabel = new JLabel("", SwingConstants.CENTER);
	public final JLabel scheduledTimeLabel = new JLabel("", SwingConstants.CENTER);
	public final JLabel runtimeLabel = new JLabel("", SwingConstants.CENTER);
	public final JLabel breaksLabel = new JLabel("", SwingConstants.CENTER);
	public final JLabel inGameLabel = new JLabel("", SwingConstants.CENTER);
	public final JLabel breakTimeLabel = new JLabel("", SwingConstants.CENTER);
	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	private final ConfigManager configManager;
	private final Client client;
	private final JPanel contentPanel = new JPanel(new GridBagLayout());
	public boolean breaking;
	private long inGameTime = 0;
	private long breakTime = 0;

	@Inject
	InfoPanel(SwingScheduler swingScheduler, ChinManager chinManager, ChinManagerPlugin chinManagerPlugin)
	{
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;
		this.configManager = chinManagerPlugin.getConfigManager();
		this.client = chinManagerPlugin.getClient();

		setLayout(new BorderLayout());
		setBackground(PANEL_BACKGROUND_COLOR);

		add(contentPanel, BorderLayout.CENTER);

		infoPanel();

		DISPOSABLES.addAll(
			Observable
				.interval(500, TimeUnit.MILLISECONDS)
				.observeOn(swingScheduler)
				.subscribe(this::milliseconds),

			Observable.merge(
					chinManager
						.getActiveBreaksObservable(),
					chinManager
						.getActiveObservable(),
					chinManager
						.getPlannedBreaksObservable(),
					chinManager
						.gameStateChanged
				)
				.observeOn(swingScheduler)
				.subscribe((i) -> infoPanel())
		);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH, super.getPreferredSize().height);
	}

	private void milliseconds(Long aLong)
	{
		Set<Plugin> activePlugins = chinManager.getActivePlugins();

		if (activePlugins.size() == 0)
		{
			breakingTimeLabel.setText("00:00:00");
			scheduledTimeLabel.setText("00:00:00");
			runtimeLabel.setText("00:00:00");
			breaksLabel.setText("0");

			inGameTime = 0;
			inGameLabel.setText("00:00:00");

			breakTime = 0;
			breakTimeLabel.setText("00:00:00");

			return;
		}

		Instant now = Instant.now();

		Map<Plugin, Instant> startTimes = chinManager.getStartTimes();

		Instant startTime = startTimes
			.entrySet()
			.stream()
			.filter((pluginInstantEntry) -> activePlugins.contains(pluginInstantEntry.getKey()))
			.map(Map.Entry::getValue)
			.min(Comparator.comparing(Instant::getEpochSecond))
			.orElse(null);

		if (startTime == null)
		{
			runtimeLabel.setText("??:??:??");
			return;
		}

		Duration duration = Duration.between(startTime, now);
		runtimeLabel.setText(formatDuration(duration));
		inGameLabel.setText(formatDuration(inGameTime));
		breakTimeLabel.setText(formatDuration(breakTime));
		breaksLabel.setText(String.valueOf(chinManager.getAmountOfBreaks()));

		Instant almost = Instant.MAX;
		if (chinManager.getActiveBreaks().size() == chinManager.getActivePlugins().size())
		{
			for (Instant instant : chinManager.getActiveBreaks().values())
			{
				if (instant.isBefore(almost))
				{
					almost = instant;
				}
			}

			if (now.isAfter(almost))
			{
				breakingTimeLabel.setText("00:00:00");
			}
			else
			{
				breakingTimeLabel.setText(formatDuration(Duration.between(now, almost)));

				if (client.getGameState() == GameState.LOGIN_SCREEN)
				{
					breakTime += 500;
				}
			}
		}
		else
		{
			breakingTimeLabel.setText("00:00:00");
		}

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			inGameTime += 500;
		}

		if (!chinManager.getPlannedBreaks().isEmpty())
		{
			almost = Instant.MAX;
			for (Pair<Instant, Integer> instantIntegerPair : chinManager.getPlannedBreaks().values())
			{
				if (instantIntegerPair.getLeft().isBefore(almost))
				{
					almost = instantIntegerPair.getLeft();
				}
			}

			if (now.isAfter(almost))
			{
				scheduledTimeLabel.setText("Waiting...");
			}
			else
			{
				scheduledTimeLabel.setText(formatDuration(Duration.between(now, almost)));
			}
		}
		else
		{
			scheduledTimeLabel.setText("00:00:00");
		}

		for (Plugin plugin : chinManager.getActivePlugins())
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

			boolean enabled = Boolean.parseBoolean(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP_BREAKHANDLER, Plugins.sanitizedName(plugin) + "-enabled"));

			if (enabled && chinManager.getPlugins().get(plugin) && chinManagerPlugin.isValidBreak(plugin) && !chinManager.isBreakPlanned(plugin) && !chinManager.isBreakActive(plugin))
			{
				chinManagerPlugin.scheduleBreak(plugin);
			}
		}

		chinManager.updatePlannedBreaks();

		if (!breakShowing && !breaking && !chinManager.getPlannedBreaks().isEmpty() && chinManager.getPlannedBreaks().containsKey(chinManager.getCurrentlyActive()))
		{
			infoPanel();
		}
	}

	private void infoPanel()
	{
		contentPanel.removeAll();

		if (chinManager.getActiveBreaks().size() == chinManager.getActivePlugins().size() && (chinManager.getCurrentlyActive() == null || chinManager.getActivePlugins().size() == 1))
		{
			breaking = true;

			addComponent(contentPanel,
				new JLabel("Taking a break for:", SwingConstants.CENTER),
				0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(2, 0, 2, 0));

			addComponent(contentPanel,
				breakingTimeLabel,
				0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(2, 0, 2, 0));
		}
		else
		{
			breaking = false;
		}

		if (!breaking && !chinManager.getPlannedBreaks().isEmpty() && chinManager.getPlannedBreaks().containsKey(chinManager.getCurrentlyActive()))
		{
			breakShowing = true;

			addComponent(contentPanel,
				new JLabel("Scheduled break in:", SwingConstants.CENTER),
				0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(2, 0, 2, 0));

			addComponent(contentPanel,
				scheduledTimeLabel,
				0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(2, 0, 2, 0));
		}
		else
		{
			breakShowing = false;
		}

		addComponent(contentPanel,
			new JLabel("Total runtime", SwingConstants.CENTER),
			0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(2, 0, 2, 0));

		addComponent(contentPanel,
			new JLabel("Amount of breaks", SwingConstants.CENTER),
			1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(2, 0, 2, 0));

		addComponent(contentPanel,
			runtimeLabel,
			0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(2, 0, 2, 0));

		addComponent(contentPanel,
			breaksLabel,
			1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(2, 0, 2, 0));

		addComponent(contentPanel,
			new JLabel("In game time", SwingConstants.CENTER),
			0, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(2, 0, 2, 0));

		addComponent(contentPanel,
			new JLabel("Break time", SwingConstants.CENTER),
			1, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(2, 0, 2, 0));

		addComponent(contentPanel,
			inGameLabel,
			0, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(2, 0, 2, 0));

		addComponent(contentPanel,
			breakTimeLabel,
			1, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(2, 0, 2, 0));

		contentPanel.revalidate();
		contentPanel.repaint();
	}
}
