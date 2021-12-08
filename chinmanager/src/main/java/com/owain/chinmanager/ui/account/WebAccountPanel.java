package com.owain.chinmanager.ui.account;

import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.api.AccountApi;
import com.owain.chinmanager.api.LicenseApi;
import static com.owain.chinmanager.ui.ChinManagerPanel.BACKGROUND_COLOR;
import static com.owain.chinmanager.ui.ChinManagerPanel.NORMAL_FONT;
import static com.owain.chinmanager.ui.ChinManagerPanel.PANEL_BACKGROUND_COLOR;
import static com.owain.chinmanager.ui.ChinManagerPanel.SMALL_FONT;
import com.owain.chinmanager.ui.utils.AbstractButtonSource;
import com.owain.chinmanager.ui.utils.DocumentEventSource;
import com.owain.chinmanager.ui.utils.GridBagHelper;
import com.owain.chinmanager.ui.utils.JMultilineLabel;
import com.owain.chinmanager.ui.utils.Separator;
import com.owain.chinmanager.ui.utils.SwingScheduler;
import com.owain.chinmanager.utils.ConfigGroup;
import com.owain.chinmanager.websockets.WebsocketManager;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.ClientUI;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;
import net.runelite.client.util.LinkBrowser;

@Slf4j
public class WebAccountPanel extends JPanel
{
	public static final CompositeDisposable DISPOSABLES = new CompositeDisposable();

	private final SwingScheduler swingScheduler;
	private final ConfigManager configManager;
	private final AccountApi accountApi;
	private final LicenseApi licenseApi;
	private final WebsocketManager websocketManager;
	private final JPanel contentPanel = new JPanel(new GridBagLayout());
	private final JMultilineLabel errorLabel = new JMultilineLabel();
	private final JMultilineLabel authErrorLabel = new JMultilineLabel();

	@Inject
	WebAccountPanel(
		SwingScheduler swingScheduler,
		ChinManagerPlugin chinManagerPlugin,
		AccountApi accountApi,
		LicenseApi licenseApi,
		WebsocketManager websocketManager
	)
	{
		errorLabel.setText("Incorrect username / password combination");
		errorLabel.setFont(SMALL_FONT);
		errorLabel.setDisabledTextColor(Color.WHITE);

		authErrorLabel.setText("Incorrect authenticator code");
		authErrorLabel.setFont(SMALL_FONT);
		authErrorLabel.setDisabledTextColor(Color.WHITE);

		this.swingScheduler = swingScheduler;

		this.configManager = chinManagerPlugin.getConfigManager();

		this.accountApi = accountApi;
		this.licenseApi = licenseApi;

		this.websocketManager = websocketManager;

		if (configManager.getConfiguration(ChinManagerPlugin.CONFIG_GROUP, "cookies") == null)
		{
			configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "cookies", "");
		}

		chinManagerPlugin.getEventBus().register(this);

		setLayout(new BorderLayout());
		setBackground(PANEL_BACKGROUND_COLOR);

		init();
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH, super.getPreferredSize().height);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!configChanged.getKey().equals("token"))
		{
			return;
		}

		contentPanel();
	}

	private void init()
	{
		contentPanel.setBorder(new EmptyBorder(5, 10, 0, 10));
		contentPanel();
		add(contentPanel, BorderLayout.CENTER);
	}

	private void contentPanel()
	{
		accountApi
			.checkLogin()
			.subscribeOn(Schedulers.io())
			.take(1)
			.observeOn(swingScheduler)
			.subscribe((checkLogin) ->
			{
				websocketManager.token = checkLogin;
				if (checkLogin != null && !checkLogin.isEmpty())
				{
					loggedIn();
				}
				else
				{
					loggedOut();
				}
			});
	}

	private void loggedIn()
	{
		licenseApi.getLicenses()
			.subscribeOn(Schedulers.io())
			.take(1)
			.observeOn(swingScheduler)
			.subscribe((licenses) ->
			{
				contentPanel.removeAll();

				int counter = 0;

				if (licenses.size() == 0)
				{
					JMultilineLabel description = new JMultilineLabel();
					description.setText("There are no active license linked to your account.");
					description.setFont(SMALL_FONT);
					description.setDisabledTextColor(Color.WHITE);

					GridBagHelper.addComponent(contentPanel,
						description,
						0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 0, 10, 0));

					counter++;
				}
				else
				{
					GridBagHelper.addComponent(contentPanel,
						new JLabel("Licenses:"),
						0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);

					counter++;

					GridBagHelper.addComponent(contentPanel,
						new Separator(),
						0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(10, 0, 5, 0));

					counter++;

					for (Map.Entry<String, Map<String, String>> licenseMap : licenses.entrySet())
					{
						JPanel pluginPanel = new JPanel(new BorderLayout());
						JPanel titleWrapper = new JPanel(new BorderLayout());
						titleWrapper.setBackground(BACKGROUND_COLOR);
						titleWrapper.setBorder(new CompoundBorder(
							BorderFactory.createMatteBorder(0, 0, 1, 0, PANEL_BACKGROUND_COLOR),
							BorderFactory.createLineBorder(BACKGROUND_COLOR)
						));

						JPanel titleActions = new JPanel(new BorderLayout(3, 0));
						titleActions.setBackground(BACKGROUND_COLOR);
						titleActions.setBorder(new EmptyBorder(0, 0, 0, 8));

						String daysLeft = licenseMap.getValue().values().stream().findFirst().orElse("Error");

						if (!daysLeft.equals("Lifetime") && !daysLeft.equals("Error"))
						{
							daysLeft += " left";
						}

						JLabel status = new JLabel();
						status.setText(daysLeft);
						status.setFont(SMALL_FONT.deriveFont(16f));
						status.setForeground(Color.GREEN);

						titleActions.add(status, BorderLayout.EAST);

						JLabel title = new JLabel();
						title.setText(licenseMap.getKey());
						title.setFont(NORMAL_FONT);
						title.setPreferredSize(new Dimension(0, 24));
						title.setForeground(Color.WHITE);
						title.setBorder(new EmptyBorder(0, 8, 0, 0));

						titleWrapper.add(title, BorderLayout.CENTER);
						titleWrapper.add(titleActions, BorderLayout.EAST);

						pluginPanel.add(titleWrapper, BorderLayout.NORTH);

						String license = licenseMap.getValue().keySet().stream().findFirst().orElse("");
						String currentLicense = configManager.getConfiguration(ConfigGroup.getConfigGroup(licenseMap.getKey()), "token");

						if (!license.equals(currentLicense) && currentLicense != null && !currentLicense.equals(""))
						{
							JPanel mismatchPanel = new JPanel(new BorderLayout());
							mismatchPanel.setBackground(BACKGROUND_COLOR);
							JMultilineLabel description = new JMultilineLabel();

							description.setText("Your local license differs from the license linked to your account!");
							description.setFont(SMALL_FONT);
							description.setDisabledTextColor(Color.WHITE);
							description.setBackground(BACKGROUND_COLOR);
							description.setBorder(BorderFactory.createEmptyBorder(8, 8, 10, 8));

							mismatchPanel.add(description, BorderLayout.CENTER);

							final JButton syncLicense = new JButton();
							syncLicense.setText("Use remote license");

							DISPOSABLES.add(
								AbstractButtonSource.fromActionOf(syncLicense, swingScheduler)
									.subscribe((e) -> configManager.setConfiguration(ConfigGroup.getConfigGroup(licenseMap.getKey()), "token", license))
							);

							mismatchPanel.add(syncLicense, BorderLayout.SOUTH);

							pluginPanel.add(mismatchPanel, BorderLayout.CENTER);
						}
						else if (currentLicense == null || currentLicense.equals(""))
						{
							configManager.setConfiguration(ConfigGroup.getConfigGroup(licenseMap.getKey()), "token", license);
						}

						GridBagHelper.addComponent(contentPanel,
							pluginPanel,
							0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(5, 0, 0, 0));

						counter++;
					}
				}

				final JButton refresh = new JButton();
				refresh.setText("Refresh");

				GridBagHelper.addComponent(contentPanel,
					refresh,
					0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(10, 0, 0, 0));

				counter++;

				final JButton signOut = new JButton();
				signOut.setText("Sign out");

				GridBagHelper.addComponent(contentPanel,
					signOut,
					0, counter, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(10, 0, 0, 0));

				contentPanel.revalidate();
				contentPanel.repaint();

				DISPOSABLES.addAll(
					AbstractButtonSource.fromActionOf(refresh, swingScheduler)
						.subscribe((e) -> loggedIn()),

					AbstractButtonSource.fromActionOf(signOut, swingScheduler)
						.subscribe((e) -> {
							configManager.setConfiguration(ChinManagerPlugin.CONFIG_GROUP, "cookies", "");
							contentPanel();
						})
				);
			});
	}

	private void loggedOut()
	{
		contentPanel.removeAll();

		JMultilineLabel description = new JMultilineLabel();
		description.setText("Login with your chinplugins.xyz account. This will enabled automatic syncing of your licenses and will display the current status of your licenses.");
		description.setFont(SMALL_FONT);
		description.setDisabledTextColor(Color.WHITE);

		GridBagHelper.addComponent(contentPanel,
			description,
			0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 10, 0));

		GridBagHelper.addComponent(contentPanel,
			new Separator(),
			0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 10, 0));

		JPanel discordTitlePanel = new JPanel(new BorderLayout());

		JLabel discordTitle = new JLabel();
		final JButton discordSignIn = new JButton();

		discordTitle.setText("Sign in with:");
		discordTitle.setForeground(Color.WHITE);

		discordSignIn.setText("Discord");
		DISPOSABLES.add(
			AbstractButtonSource.fromActionOf(discordSignIn, swingScheduler)
				.subscribe((e) -> {
					LinkBrowser.browse("https://chinplugins.xyz/api/discord/OpenOSRS");

					JTextField key = new JTextField();
					Object[] message = {
						"key:", key
					};

					int option =
						JOptionPane.showConfirmDialog(ClientUI.getFrame(), message, "Sign in", JOptionPane.OK_CANCEL_OPTION);

					if (option != JOptionPane.OK_OPTION || key.getText().equals(""))
					{
						return;
					}

					oneTimeLogin(key, "");
				})
		);

		discordSignIn.setBorder(new EmptyBorder(3, 0, 3, 0));

		discordTitlePanel.add(discordTitle, BorderLayout.WEST);
		discordTitlePanel.add(discordSignIn, BorderLayout.EAST);

		GridBagHelper.addComponent(contentPanel,
			discordTitlePanel,
			0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 10, 0));

		GridBagHelper.addComponent(contentPanel,
			new Separator(),
			0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 10, 0));

		GridBagHelper.addComponent(contentPanel,
			new JLabel("Alternatively", SwingConstants.CENTER),
			0, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(20, 0, 20, 0));

		GridBagHelper.addComponent(contentPanel,
			new Separator(),
			0, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 10, 0));

		JPanel chinTitlePanel = new JPanel(new BorderLayout());

		JLabel chinTitle = new JLabel();
		final JButton chinSignup = new JButton();

		chinTitle.setText("No account yet?");
		chinTitle.setForeground(Color.WHITE);

		chinSignup.setText("Sign up");
		DISPOSABLES.add(
			AbstractButtonSource.fromActionOf(chinSignup, swingScheduler)
				.subscribe((e) -> LinkBrowser.browse("https://chinplugins.xyz/register"))
		);

		chinSignup.setBorder(new EmptyBorder(3, 0, 3, 0));

		chinTitlePanel.add(chinTitle, BorderLayout.WEST);
		chinTitlePanel.add(chinSignup, BorderLayout.EAST);

		GridBagHelper.addComponent(contentPanel,
			chinTitlePanel,
			0, 6, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 10, 0));

		GridBagHelper.addComponent(contentPanel,
			new Separator(),
			0, 7, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 10, 0));

		GridBagHelper.addComponent(contentPanel,
			new JLabel("Username"),
			0, 8, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);

		final JTextField usernameField = new JTextField();
		GridBagHelper.addComponent(contentPanel,
			usernameField,
			0, 9, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);

		GridBagHelper.addComponent(contentPanel,
			new JLabel("Password"),
			0, 10, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);

		final JPasswordField passwordField = new JPasswordField();
		GridBagHelper.addComponent(contentPanel,
			passwordField,
			0, 11, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);

		final JLabel authenticatorLabel = new JLabel("Authenticator code");
		authenticatorLabel.setVisible(false);
		GridBagHelper.addComponent(contentPanel,
			authenticatorLabel,
			0, 12, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);

		final JTextField authenticatorField = new JTextField();
		authenticatorField.setVisible(false);
		GridBagHelper.addComponent(contentPanel,
			authenticatorField,
			0, 13, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);

		errorLabel.setVisible(false);
		GridBagHelper.addComponent(contentPanel,
			errorLabel,
			0, 14, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);

		authErrorLabel.setVisible(false);
		GridBagHelper.addComponent(contentPanel,
			authErrorLabel,
			0, 15, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);

		Observable<ChangeEvent> usernameDocumentEventObservable = DocumentEventSource.fromDocumentEventsOf(
			usernameField.getDocument(), swingScheduler
		);

		Observable<ChangeEvent> passwordDocumentEventObservable = DocumentEventSource.fromDocumentEventsOf(
			passwordField.getDocument(), swingScheduler
		);

		Observable<ChangeEvent> authenticatorDocumentEventObservable = DocumentEventSource.fromDocumentEventsOf(
			authenticatorField.getDocument(), swingScheduler
		);

		DISPOSABLES.add(
			Observable.merge(
				usernameDocumentEventObservable,
				passwordDocumentEventObservable,
				authenticatorDocumentEventObservable
			).subscribe((e) -> {
				final String username = usernameField.getText();
				final String password = String.valueOf(passwordField.getPassword());

				if (username.contains("@") && username.contains(".") && !password.equals(""))
				{
					accountApi
						.login(usernameField.getText(), String.valueOf(passwordField.getPassword()))
						.subscribeOn(Schedulers.io())
						.take(1)
						.observeOn(swingScheduler)
						.subscribe((login) -> {
							switch (login)
							{
								case -1:
									errorLabel.setVisible(true);
									authErrorLabel.setVisible(false);

									authenticatorLabel.setVisible(false);
									authenticatorField.setVisible(false);
									break;

								case 0:
									errorLabel.setVisible(false);

									authenticatorLabel.setVisible(true);
									authenticatorField.setVisible(true);

									if (authenticatorField.getText().length() == 6)
									{
										accountApi
											.loginAuth(usernameField.getText(), String.valueOf(passwordField.getPassword()), authenticatorField.getText())
											.subscribeOn(Schedulers.io())
											.take(1)
											.observeOn(swingScheduler)
											.subscribe((auth) -> {
												if (auth)
												{
													contentPanel();
												}
												else
												{
													errorLabel.setVisible(false);
													authErrorLabel.setVisible(true);
												}
											});
									}

									break;

								case 1:
									contentPanel();
									break;
							}
						});
				}
				else
				{
					errorLabel.setVisible(false);
					authErrorLabel.setVisible(false);
				}
			}, (e) -> log.error("[WebAccountPanel] Something went wrong", e))
		);

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	private void oneTimeLogin(JTextField key, String code)
	{
		accountApi
			.oneTime(key.getText(), code)
			.subscribeOn(Schedulers.io())
			.take(1)
			.observeOn(swingScheduler)
			.subscribe((login) -> {
				switch (login)
				{
					case -1:
						JTextField codeField = new JTextField();
						Object[] message = {
							"key:", key,
							"Authenticator code:", codeField
						};

						int option =
							JOptionPane.showConfirmDialog(ClientUI.getFrame(), message, "Sign in", JOptionPane.OK_CANCEL_OPTION);

						if (option != JOptionPane.OK_OPTION || key.getText().equals(""))
						{
							return;
						}

						oneTimeLogin(key, codeField.getText());

						break;
					case 0:
						JOptionPane.showMessageDialog(ClientUI.getFrame(), "Something went wrong, please try again later", "Error!",
							JOptionPane.ERROR_MESSAGE);
						break;
					case 1:
						contentPanel();
						break;
					case 2:
						JOptionPane.showMessageDialog(ClientUI.getFrame(), "Invalid authenticator code", "Error!",
							JOptionPane.ERROR_MESSAGE);
						break;
				}
			});
	}
}
