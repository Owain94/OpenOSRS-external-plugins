package com.owain.chinbreakhandler.ui;

import com.owain.chinbreakhandler.ChinBreakHandler;
import com.owain.chinbreakhandler.ChinBreakHandlerPlugin;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

public class ChinBreakHandlerPanel extends PluginPanel
{
	final static Color PANEL_BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;
	final static Color BACKGROUND_COLOR = ColorScheme.DARKER_GRAY_COLOR;

	static final Font NORMAL_FONT = FontManager.getRunescapeFont();
	static final Font SMALL_FONT = FontManager.getRunescapeSmallFont();

	private static final ImageIcon HELP_ICON;
	private static final ImageIcon HELP_HOVER_ICON;

	static
	{
		final BufferedImage helpIcon =
			ImageUtil.recolorImage(
				ImageUtil.getResourceStreamFromClass(ChinBreakHandlerPlugin.class, "help.png"), ColorScheme.BRAND_BLUE
			);
		HELP_ICON = new ImageIcon(helpIcon);
		HELP_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(helpIcon, 0.53f));
	}

	private final ChinBreakHandlerPlugin chinBreakHandlerPlugin;
	private final ChinBreakHandler chinBreakHandler;

	public @NonNull Disposable pluginDisposable;
	public @NonNull Disposable activeDisposable;
	public @NonNull Disposable currentDisposable;

	@Inject
	private ChinBreakHandlerPanel(ChinBreakHandlerPlugin chinBreakHandlerPlugin, ChinBreakHandler chinBreakHandler)
	{
		super(false);

		this.chinBreakHandlerPlugin = chinBreakHandlerPlugin;
		this.chinBreakHandler = chinBreakHandler;

		pluginDisposable = chinBreakHandler
			.getPluginObservable()
			.subscribe((Map<Plugin, Boolean> plugins) ->
				SwingUtil.syncExec(() ->
					buildPanel(plugins)));

		activeDisposable = chinBreakHandler
			.getActiveObservable()
			.subscribe(
				(Set<Plugin> ignore) ->
					SwingUtil.syncExec(() ->
						buildPanel(chinBreakHandler.getPlugins()))
			);

		currentDisposable = chinBreakHandler
			.getActiveBreaksObservable()
			.subscribe(
				(Map<Plugin, Instant> ignore) ->
					SwingUtil.syncExec(() ->
						buildPanel(chinBreakHandler.getPlugins()))
			);

		this.setBackground(PANEL_BACKGROUND_COLOR);
		this.setLayout(new BorderLayout());

		buildPanel(chinBreakHandler.getPlugins());
	}

	void buildPanel(Map<Plugin, Boolean> plugins)
	{
		removeAll();

		if (plugins.isEmpty())
		{
			PluginErrorPanel errorPanel = new PluginErrorPanel();
			errorPanel.setContent("Chin break handler", "There were no plugins that registered themselves with the break handler.");

			add(errorPanel, BorderLayout.NORTH);
		}
		else
		{
			JPanel contentPanel = new JPanel(new BorderLayout());

			contentPanel.add(statusPanel(), BorderLayout.NORTH);
			contentPanel.add(tabbedPane(plugins), BorderLayout.CENTER);

			add(titleBar(), BorderLayout.NORTH);
			add(contentPanel, BorderLayout.CENTER);
		}

		revalidate();
		repaint();
	}

	private JPanel titleBar()
	{
		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel title = new JLabel();
		JLabel help = new JLabel(HELP_ICON);

		title.setText("Chin break handler");
		title.setForeground(Color.WHITE);

		help.setToolTipText("Info");
		help.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				JOptionPane.showMessageDialog(
					ClientUI.getFrame(),
					"<html><center>The configs in this panel can be used to <b>schedule</b> breaks.<br>" +
						"When the timer hits zero a break is scheduled. This does not mean that the break will be taken immediately!<br>" +
						"Plugins decide what the best time is for a break, for example a NMZ dream will be finished instead of interrupted.</center></html>",
					"Chin break handler",
					JOptionPane.QUESTION_MESSAGE
				);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				help.setIcon(HELP_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				help.setIcon(HELP_ICON);
			}
		});
		help.setBorder(new EmptyBorder(0, 3, 0, 0));

		titlePanel.add(title, BorderLayout.WEST);
		titlePanel.add(help, BorderLayout.EAST);

		return titlePanel;
	}

	private JPanel statusPanel()
	{
		Set<Plugin> activePlugins = chinBreakHandler.getActivePlugins();

		JPanel contentPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		if (activePlugins.isEmpty())
		{
			return contentPanel;
		}

		for (Plugin plugin : activePlugins)
		{
			ChinBreakHandlerStatusPanel statusPanel = new ChinBreakHandlerStatusPanel(chinBreakHandlerPlugin, chinBreakHandler, plugin);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			c.gridy += 1;
			c.insets = new Insets(5, 10, 0, 10);

			contentPanel.add(statusPanel, c);
		}

		return contentPanel;
	}

	private JTabbedPane tabbedPane(Map<Plugin, Boolean> plugins)
	{
		JTabbedPane mainTabPane = new JTabbedPane();

		JScrollPane pluginPanel = wrapContainer(contentPane(plugins));
		JScrollPane repositoryPanel = wrapContainer(new ChinBreakHandlerAccountPanel(chinBreakHandlerPlugin));

		mainTabPane.add("Plugins", pluginPanel);
		mainTabPane.add("Accounts", repositoryPanel);

		return mainTabPane;
	}

	private JPanel contentPane(Map<Plugin, Boolean> plugins)
	{
		JPanel contentPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		if (chinBreakHandler.getPlugins().isEmpty())
		{
			return contentPanel;
		}

		for (Map.Entry<Plugin, Boolean> plugin : plugins.entrySet())
		{
			ChinBreakHandlerPluginPanel panel = new ChinBreakHandlerPluginPanel(chinBreakHandlerPlugin, plugin.getKey(), plugin.getValue());

			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			c.gridy += 1;
			c.insets = new Insets(5, 10, 0, 10);

			contentPanel.add(panel, c);
		}

		return contentPanel;
	}

	public static JScrollPane wrapContainer(final JPanel container)
	{
		final JPanel wrapped = new JPanel(new BorderLayout());
		wrapped.add(container, BorderLayout.NORTH);
		wrapped.setBackground(PANEL_BACKGROUND_COLOR);

		final JScrollPane scroller = new JScrollPane(wrapped);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		scroller.setBackground(PANEL_BACKGROUND_COLOR);

		return scroller;
	}
}