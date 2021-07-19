/*
 * Created by JFormDesigner on Sat Aug 01 23:27:36 CEST 2020
 */

package com.owain.chinmanager.ui.account;

import com.google.inject.Inject;
import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import static com.owain.chinmanager.ui.ChinManagerPanel.PANEL_BACKGROUND_COLOR;
import static com.owain.chinmanager.ui.ChinManagerPanel.SMALL_FONT;
import static com.owain.chinmanager.ui.ChinManagerPanel.wrapContainer;
import com.owain.chinmanager.ui.utils.DeferredDocumentChangedListener;
import com.owain.chinmanager.ui.utils.GridBagHelper;
import com.owain.chinmanager.ui.utils.JMultilineLabel;
import com.owain.chinmanager.utils.ProfilesData;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.PluginPanel;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;

public class OsrsAccountPanel extends JPanel
{
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH, super.getPreferredSize().height);
	}

	private final ConfigManager configManager;
	private final ChinManager chinManager;
	private final JPanel contentPanel = new JPanel(new GridBagLayout());

	@Inject
	OsrsAccountPanel(ChinManagerPlugin chinManagerPlugin, ChinManager chinManager)
	{
		this.configManager = chinManagerPlugin.getConfigManager();
		this.chinManager = chinManager;

		setupDefaults();

		setLayout(new BorderLayout());
		setBackground(PANEL_BACKGROUND_COLOR);

		init();
	}

	private boolean getConfigValue()
	{
		String accountselection = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection");

		return Boolean.parseBoolean(accountselection);
	}

	private void init()
	{
		contentPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

		JPanel accountSelection = new JPanel(new GridLayout(0, 2));
		accountSelection.setBorder(new EmptyBorder(5, 5, 0, 5));
		ButtonGroup buttonGroup = new ButtonGroup();

		JCheckBox manualButton = new JCheckBox("Manual");
		JCheckBox profilesButton = new JCheckBox("Profiles plugin");

		String profilesSalt = configManager.getConfiguration("betterProfiles", "salt");
		boolean profilesSavePasswords = Boolean.parseBoolean(configManager.getConfiguration("betterProfiles", "rememberPassword"));

		if (profilesSalt == null || profilesSalt.length() == 0 || !profilesSavePasswords)
		{
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection", true);
			profilesButton.setEnabled(false);
		}

		manualButton.addActionListener(e -> {
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection", manualButton.isSelected());
			contentPanel(manualButton.isSelected());
		});

		profilesButton.addActionListener(e -> {
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection", !profilesButton.isSelected());
			contentPanel(!profilesButton.isSelected());
		});

		buttonGroup.add(manualButton);
		buttonGroup.add(profilesButton);

		boolean config = getConfigValue();

		manualButton.setSelected(config);
		profilesButton.setSelected(!config);

		accountSelection.add(manualButton);
		accountSelection.add(profilesButton);

		add(accountSelection, BorderLayout.NORTH);

		contentPanel(config);

		add(wrapContainer(contentPanel), BorderLayout.CENTER);
	}

	private void contentPanel(boolean manual)
	{
		contentPanel.removeAll();

		int counter = 0;

		if (manual)
		{
			GridBagHelper.addComponent(contentPanel,
				new JLabel("Username"),
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 10, 0));
			counter++;

			final JTextField usernameField = new JTextField();
			usernameField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			usernameField.setText(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-username"));
			DeferredDocumentChangedListener usernameListener = new DeferredDocumentChangedListener();
			usernameListener.addChangeListener(e ->
				configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-username", usernameField.getText()));
			usernameField.getDocument().addDocumentListener(usernameListener);

			GridBagHelper.addComponent(contentPanel,
				usernameField,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 10, 0));
			counter++;

			GridBagHelper.addComponent(contentPanel,
				new JLabel("Password"),
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 10, 0));
			counter++;

			final JPasswordField passwordField = new JPasswordField();
			passwordField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			passwordField.setText(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-password"));
			DeferredDocumentChangedListener passwordListener = new DeferredDocumentChangedListener();
			passwordListener.addChangeListener(e ->
				configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-password", String.valueOf(passwordField.getPassword())));
			passwordField.getDocument().addDocumentListener(passwordListener);

			GridBagHelper.addComponent(contentPanel,
				passwordField,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 10, 0));
			counter++;

			GridBagHelper.addComponent(contentPanel,
				new JLabel("Bank pin"),
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 10, 0));
			counter++;

			final JPasswordField bankPin = new JPasswordField();
			bankPin.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			bankPin.setText(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-pin"));
			DeferredDocumentChangedListener bankPinListener = new DeferredDocumentChangedListener();
			bankPinListener.addChangeListener(e ->
				configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-pin", String.valueOf(bankPin.getPassword())));
			bankPin.getDocument().addDocumentListener(bankPinListener);

			GridBagHelper.addComponent(contentPanel,
				bankPin,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 10, 0));
			counter++;

			JMultilineLabel description = new JMultilineLabel();
			description.setText("If you use this, make sure to turn off the Chin bank pin plugin. Prefer the Chin bank pin plugin? Just leave this field empty!");
			description.setFont(SMALL_FONT.deriveFont(16f));
			description.setDisabledTextColor(Color.WHITE);

			GridBagHelper.addComponent(contentPanel,
				description,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 10, 0));
		}
		else if (ChinManagerPlugin.getProfileData() == null)
		{
			GridBagHelper.addComponent(contentPanel,
				new JLabel("Profiles plugin password"),
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 10, 0));
			counter++;

			final JPasswordField passwordField = new JPasswordField();
			passwordField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			JLabel parsingLabel = new JLabel();
			parsingLabel.setHorizontalAlignment(SwingConstants.CENTER);
			parsingLabel.setPreferredSize(new Dimension(PANEL_WIDTH, 15));

			DeferredDocumentChangedListener passwordListener = new DeferredDocumentChangedListener();
			passwordListener.addChangeListener(e ->
			{
				try
				{
					ChinManagerPlugin.setProfileData(ProfilesData.getProfileData(configManager, passwordField.getPassword()));
					contentPanel(false);
				}
				catch (InvalidKeySpecException | NoSuchPaddingException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException | NoSuchAlgorithmException ignored)
				{
					parsingLabel.setText("Incorrect password!");
				}
			});
			passwordField.getDocument().addDocumentListener(passwordListener);

			GridBagHelper.addComponent(contentPanel,
				passwordField,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 10, 0));
			counter++;

			GridBagHelper.addComponent(contentPanel,
				parsingLabel,
				0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 0, 10, 0));
		}
		else
		{
			ConfigChanged configChanged = new ConfigChanged();
			configChanged.setGroup("mock");
			configChanged.setKey("mock");
			chinManager.configChanged.onNext(configChanged);

			if (!ChinManagerPlugin.getProfileData().contains(":"))
			{
				GridBagHelper.addComponent(contentPanel,
					new JLabel("No accounts found"),
					0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 0, 10, 0));
			}
			else
			{
				GridBagHelper.addComponent(contentPanel,
					new JLabel("Select account"),
					0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 0, 10, 0));
				counter++;

				String[] accounts = Arrays.stream(ChinManagerPlugin.getProfileData().split("\\n"))
					.map((s) -> s.split(":")[0])
					.sorted()
					.toArray(String[]::new);

				final JComboBox<String> filterComboBox = new JComboBox<>(accounts);
				final JPasswordField bankPin = new JPasswordField();

				filterComboBox.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
				filterComboBox.addActionListener(e -> {
					if (filterComboBox.getSelectedItem() != null)
					{
						configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-profiles-account", filterComboBox.getSelectedItem().toString());
						bankPin.setText(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-profiles-pin-" + filterComboBox.getSelectedItem().toString()));
					}
				});

				String config = configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-profiles-account");

				if (config != null)
				{
					int index = Arrays.asList(accounts).indexOf(config);

					if (index != -1)
					{
						filterComboBox.setSelectedIndex(index);
					}
					else
					{
						filterComboBox.setSelectedIndex(0);
					}
				}

				GridBagHelper.addComponent(contentPanel,
					filterComboBox,
					0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 0, 10, 0));
				counter++;

				if (filterComboBox.getSelectedItem() != null)
				{
					GridBagHelper.addComponent(contentPanel,
						new JLabel("Bank pin"),
						0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 10, 0));
					counter++;

					bankPin.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					bankPin.setText(configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-profiles-pin-" + filterComboBox.getSelectedItem().toString()));
					DeferredDocumentChangedListener bankPinListener = new DeferredDocumentChangedListener();
					bankPinListener.addChangeListener(e ->
						configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-profiles-pin-" + filterComboBox.getSelectedItem().toString(), String.valueOf(bankPin.getPassword())));
					bankPin.getDocument().addDocumentListener(bankPinListener);

					GridBagHelper.addComponent(contentPanel,
						bankPin,
						0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 10, 0));
					counter++;

					JMultilineLabel description = new JMultilineLabel();
					description.setText("If you use this, make sure to turn off the Chin bank pin plugin. Prefer the Chin bank pin plugin? Just leave this field empty!");
					description.setFont(SMALL_FONT.deriveFont(16f));
					description.setDisabledTextColor(Color.WHITE);

					GridBagHelper.addComponent(contentPanel,
						description,
						0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 10, 0));
				}
			}
		}

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	private void setupDefaults()
	{
		if (configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection") == null)
		{
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection", true);
		}

		if (configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-username") == null)
		{
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-username", "");
		}

		if (configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-password") == null)
		{
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-manual-password", "");
		}

		if (configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-profiles-account") == null)
		{
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "accountselection-profiles-account", "");
		}
	}
}
