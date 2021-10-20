package com.owain.chinmanager.ui;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ui.account.OsrsAccountPanel;
import com.owain.chinmanager.ui.account.WebAccountPanel;
import com.owain.chinmanager.ui.plugins.StatusPanel;
import com.owain.chinmanager.ui.utils.SwingScheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
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
import net.runelite.client.util.ImageUtil;

public class ChinManagerPanel extends net.runelite.client.ui.PluginPanel
{
	public static final Color PANEL_BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;
	public static final Color BACKGROUND_COLOR = ColorScheme.DARKER_GRAY_COLOR;

	public static final Font NORMAL_FONT = FontManager.getRunescapeFont();
	public static final Font SMALL_FONT = FontManager.getRunescapeSmallFont();
	public static final CompositeDisposable DISPOSABLES = new CompositeDisposable();
	private static final ImageIcon HELP_ICON;
	private static final ImageIcon HELP_HOVER_ICON;
	private static final JTabbedPane MAIN_TABBED_PANE = new JTabbedPane();

	static
	{
		final BufferedImage helpIcon =
			ImageUtil.recolorImage(
				ImageUtil.loadImageResource(ChinManagerPlugin.class, "help.png"), ColorScheme.BRAND_BLUE
			);
		HELP_ICON = new ImageIcon(helpIcon);
		HELP_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(helpIcon, 0.53f));
	}

	private final ChinManager chinManager;

	private final StatusPanel statusPanel;
	private final PluginConfigPanel pluginConfigPanel;
	private final OsrsAccountPanel osrsAccountPanel;
	private final WebAccountPanel webAccountPanel;


	@Inject
	ChinManagerPanel(
		SwingScheduler swingScheduler,
		ChinManager chinManager,
		StatusPanel statusPanel,
		PluginConfigPanel pluginConfigPanel,
		OsrsAccountPanel osrsAccountPanel,
		WebAccountPanel webAccountPanel
	)
	{
		super(false);

		this.chinManager = chinManager;

		this.statusPanel = statusPanel;
		this.pluginConfigPanel = pluginConfigPanel;
		this.osrsAccountPanel = osrsAccountPanel;
		this.webAccountPanel = webAccountPanel;

		this.setBackground(PANEL_BACKGROUND_COLOR);
		this.setLayout(new BorderLayout());

		buildPanel();

		DISPOSABLES.add(
			chinManager
				.getActiveObservable()
				.observeOn(swingScheduler)
				.subscribe(this::tabbedPane));
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

	void buildPanel()
	{
		removeAll();

		tabbedPane();

		add(titleBar(), BorderLayout.NORTH);
		add(MAIN_TABBED_PANE, BorderLayout.CENTER);

		revalidate();
		repaint();
	}

	private void tabbedPane()
	{
		Set<Plugin> plugins = chinManager.getActivePlugins();
		tabbedPane(plugins);
	}

	private void tabbedPane(Set<Plugin> plugins)
	{
		MAIN_TABBED_PANE.removeAll();

		if (plugins.size() > 0)
		{
			MAIN_TABBED_PANE.add("Status", statusPanel);
		}
		else
		{
			MAIN_TABBED_PANE.add("Plugins", pluginConfigPanel);
		}
		MAIN_TABBED_PANE.add("Accounts", osrsAccountPanel);
		MAIN_TABBED_PANE.add("ChinPlugins", wrapContainer(webAccountPanel));

		MAIN_TABBED_PANE.revalidate();
		MAIN_TABBED_PANE.repaint();

		MAIN_TABBED_PANE.setSelectedIndex(0);
	}

	private JPanel titleBar()
	{
		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel title = new JLabel();
		JLabel help = new JLabel(HELP_ICON);

		title.setText(ChinManagerPlugin.PLUGIN_NAME);
		title.setForeground(Color.WHITE);

		help.setToolTipText("Info");
		help.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				JOptionPane.showMessageDialog(
					ClientUI.getFrame(),
					// TODO: Change this
					"<html><center>bla bla bla.</center></html>",
					ChinManagerPlugin.PLUGIN_NAME,
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
}