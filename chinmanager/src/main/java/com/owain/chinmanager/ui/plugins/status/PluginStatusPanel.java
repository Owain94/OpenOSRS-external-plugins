package com.owain.chinmanager.ui.plugins.status;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import static com.owain.chinmanager.ui.ChinManagerPanel.BACKGROUND_COLOR;
import static com.owain.chinmanager.ui.ChinManagerPanel.NORMAL_FONT;
import static com.owain.chinmanager.ui.ChinManagerPanel.PANEL_BACKGROUND_COLOR;
import static com.owain.chinmanager.ui.ChinManagerPanel.SMALL_FONT;
import com.owain.chinmanager.ui.utils.SwingScheduler;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import net.runelite.client.plugins.Plugin;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;

public class PluginStatusPanel extends JPanel
{
	private final ChinManager chinManager;
	private final Plugin plugin;
	private final JPanel extraDataPanel = new JPanel(new GridBagLayout());
	private final JLabel status = new JLabel();

	public PluginStatusPanel(
		SwingScheduler swingScheduler,
		ChinManager chinManager,
		Plugin plugin
	)
	{
		this.chinManager = chinManager;
		this.plugin = plugin;

		setLayout(new BorderLayout());
		setBackground(PANEL_BACKGROUND_COLOR);
		setBorder(new EmptyBorder(0, 10, 0, 10));

		if (ChinManagerPlugin.PLUGIN_DISPOSABLE_MAP.containsKey(plugin))
		{
			ChinManagerPlugin.PLUGIN_DISPOSABLE_MAP.get(plugin).clear();
			ChinManagerPlugin.PLUGIN_DISPOSABLE_MAP.remove(plugin);
		}

		CompositeDisposable compositeDisposable = new CompositeDisposable();

		compositeDisposable.addAll(
			chinManager
				.getExtraDataObservable()
				.observeOn(swingScheduler)
				.subscribe((ignored) -> extraData()),

			Observable
				.interval(1, TimeUnit.SECONDS)
				.observeOn(swingScheduler)
				.subscribe((i) -> pluginStatus())
		);

		ChinManagerPlugin.PLUGIN_DISPOSABLE_MAP.put(plugin, compositeDisposable);

		init();
		extraData();
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH, super.getPreferredSize().height);
	}

	private void pluginStatus()
	{
		if (chinManager.getHandover().contains(plugin))
		{
			status.setText("Pending");
			status.setForeground(Color.ORANGE);
		}
		else if (chinManager.getNextActive() == plugin && !chinManager.isCurrentlyActive(plugin))
		{
			status.setText("Next");
			status.setForeground(Color.ORANGE);
		}
		else if (chinManager.isCurrentlyActive(plugin))
		{
			status.setText("Running");
			status.setForeground(Color.GREEN);
		}
		else
		{
			status.setText("Waiting");
			status.setForeground(Color.RED);
		}
	}

	private void init()
	{
		JPanel titleWrapper = new JPanel(new BorderLayout());
		titleWrapper.setBackground(BACKGROUND_COLOR);
		titleWrapper.setBorder(new CompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, PANEL_BACKGROUND_COLOR),
			BorderFactory.createLineBorder(BACKGROUND_COLOR)
		));

		extraDataPanel.setBackground(BACKGROUND_COLOR);
		extraDataPanel.setBorder(new CompoundBorder(
			new CompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, PANEL_BACKGROUND_COLOR),
				BorderFactory.createLineBorder(BACKGROUND_COLOR)
			), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		JLabel title = new JLabel();
		title.setText(plugin.getName());
		title.setFont(NORMAL_FONT);
		title.setPreferredSize(new Dimension(0, 24));
		title.setForeground(Color.WHITE);
		title.setBorder(new EmptyBorder(0, 8, 0, 0));

		JPanel titleActions = new JPanel(new BorderLayout(3, 0));
		titleActions.setBackground(BACKGROUND_COLOR);
		titleActions.setBorder(new EmptyBorder(0, 0, 0, 8));

		status.setText("Waiting");
		status.setForeground(Color.RED);
		status.setFont(SMALL_FONT.deriveFont(16f));

		titleActions.add(status, BorderLayout.EAST);

		titleWrapper.add(title, BorderLayout.CENTER);
		titleWrapper.add(titleActions, BorderLayout.EAST);

		add(titleWrapper, BorderLayout.NORTH);
		add(extraDataPanel, BorderLayout.CENTER);
	}

	private void extraData()
	{
		Map<Plugin, Map<String, String>> allData = chinManager.getExtraData();
		Map<String, String> pluginData = allData.get(plugin);

		if (pluginData == null || pluginData.isEmpty())
		{
			return;
		}

		extraDataPanel.removeAll();

		GridBagConstraints c = new GridBagConstraints();

		c.insets = new Insets(2, 0, 2, 0);
		c.gridx = 0;
		c.gridy = 0;

		if (pluginData.containsKey("State") || pluginData.containsKey("state"))
		{
			JLabel keyLabel = new JLabel("State");
			c.gridwidth = 2;
			c.weightx = 2;
			c.gridx = 0;
			c.gridy = 0;
			extraDataPanel.add(keyLabel, c);

			JLabel valueLabel = new JLabel(pluginData.containsKey("State") ? pluginData.get("State") : pluginData.get("state"));
			c.weightx = 1;
			c.gridx = 0;
			c.gridy = 1;
			extraDataPanel.add(valueLabel, c);

			c.gridx += 1;
		}

		int index = 0;
		boolean hasState = false;

		Iterator<Map.Entry<String, String>> iterator = pluginData.entrySet().iterator();
		//noinspection WhileLoopReplaceableByForEach
		while (iterator.hasNext())
		{
			Map.Entry<String, String> data = iterator.next();

			String key = data.getKey();
			String value = data.getValue();

			if (key.equals("State") || key.equals("state"))
			{
				hasState = true;
				continue;
			}

			JLabel keyLabel = new JLabel(key);
			JLabel valueLabel = new JLabel(value.isEmpty() ? "-" : value);

			index++;

			boolean bump = index % 2 == 0;
			int total = index;

			if (hasState)
			{
				total++;
			}

			if (!bump && total == pluginData.size())
			{
				c.gridwidth = 2;
			}
			else
			{
				c.gridwidth = 1;
			}

			c.gridx = bump ? 1 : 0;

			c.weightx = 2;
			if (bump)
			{
				c.gridy -= 1;
			}
			else
			{
				c.gridy += 1;
			}
			extraDataPanel.add(keyLabel, c);

			c.weightx = 1;
			c.gridy += 1;
			extraDataPanel.add(valueLabel, c);
		}

		extraDataPanel.revalidate();
		extraDataPanel.repaint();
	}

}
