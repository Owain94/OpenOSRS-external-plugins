/*
 * Copyright (c) 2019, dillydill123 <https://github.com/dillydill123>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.owain.chinmanager.ui.gear;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ui.utils.SwingScheduler;
import com.owain.chinmanager.utils.Plugins;
import io.reactivex.rxjava3.disposables.Disposable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.ClientUI;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;
import net.runelite.client.util.AsyncBufferedImage;

public class EquipmentPanel extends JPanel
{
	public static final List<Disposable> DISPOSABLES = new ArrayList<>();
	private static final int NUM_EQUIPMENT_ITEMS = 14;
	protected final ItemManager itemManager;
	private final Client client;
	private final ClientThread clientThread;
	private final ChatboxItemSearch chatboxItemSearch;
	private final ChatboxPanelManager chatboxPanelManager;
	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	@Getter(AccessLevel.PROTECTED)
	private final JPanel containerSlotsPanel;
	private final JPanel subPanel;
	private final JLabel noBanking;
	private final JButton copyEquipmentButton;
	private final JComboBox<String> pluginList;
	private HashMap<EquipmentInventorySlot, EquipmentSlot> equipmentSlots;

	@Inject
	EquipmentPanel(
		Client client,
		ClientThread clientThread,
		ItemManager itemManager,
		ChatboxPanelManager chatboxPanelManager,
		ChatboxItemSearch chatboxItemSearch,
		ChinManager chinManager,
		ChinManagerPlugin chinManagerPlugin,
		SwingScheduler swingScheduler
	)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.itemManager = itemManager;
		this.chatboxPanelManager = chatboxPanelManager;
		this.chatboxItemSearch = chatboxItemSearch;
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;

		chinManagerPlugin.loadEquipmentConfig();

		JPanel containerPanel = new JPanel();

		containerSlotsPanel = new JPanel();
		containerSlotsPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
		setupContainerPanel(containerSlotsPanel);

		pluginList = new JComboBox<>();
		pluginList.setPrototypeDisplayValue("########");
		pluginList.addActionListener((change) -> refreshCurrentSetup());

		final JPanel pluginListPanel = new JPanel();
		pluginListPanel.add(pluginList);

		copyEquipmentButton = new JButton("Copy in-game equipment");
		copyEquipmentButton.addActionListener((event) -> updateEquipmentFromGame());

		final JPanel copyEquipmentPanel = new JPanel();
		copyEquipmentPanel.add(copyEquipmentButton);

		noBanking = new JLabel("This plugin does not have gear support!");
		noBanking.setHorizontalAlignment(SwingConstants.CENTER);

		subPanel = new JPanel(new BorderLayout());
		subPanel.add(noBanking, BorderLayout.NORTH);
		subPanel.add(containerSlotsPanel, BorderLayout.CENTER);

		containerPanel.setLayout(new BorderLayout());
		containerPanel.add(pluginListPanel, BorderLayout.NORTH);
		containerPanel.add(subPanel, BorderLayout.CENTER);

		add(containerPanel);
		refreshCurrentSetup();

		DISPOSABLES.add(
			chinManager
				.getManagerPluginObservable()
				.observeOn(swingScheduler)
				.subscribe(
					(plugins) -> updatePluginList(chinManager.getManagerPlugins()))
		);

		updatePluginList(chinManager.getManagerPlugins());
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH, super.getPreferredSize().height);
	}

	private void updatePluginList(Set<Plugin> plugins)
	{
		pluginList.removeAllItems();
		plugins.stream().map(Plugin::getName).forEach((pluginName) -> {
			pluginList.addItem(pluginName);

			if (ChinManagerPlugin.getEquipmentList().stream().noneMatch((setup) -> setup.getName().equals(Plugins.sanitizedName(pluginName))))
			{
				ChinManagerPlugin.getEquipmentList().add(new Equipment(emptyContainer(), Plugins.sanitizedName(pluginName)));
				chinManagerPlugin.updateConfig();
			}
		});

		revalidate();
		repaint();
	}

	protected void addUpdateFromContainerMouseListenerToSlot(final EquipmentSlot slot)
	{
		setComponentPopupMenuToSlot(slot);
		JPopupMenu popupMenu = slot.getComponentPopupMenu();

		JMenuItem updateFromContainer = new JMenuItem("Update Slot from your equipment");
		popupMenu.add(updateFromContainer);
		updateFromContainer.addActionListener(e ->
			updateSlotFromContainer(slot));
	}

	protected void addUpdateFromSearchMouseListenerToSlot(final EquipmentSlot slot)
	{
		setComponentPopupMenuToSlot(slot);
		JPopupMenu popupMenu = slot.getComponentPopupMenu();
		JMenuItem updateFromSearch = new JMenuItem("Update Slot from Search");
		popupMenu.add(updateFromSearch);
		updateFromSearch.addActionListener(e ->
			updateSlotFromSearch(slot));
	}

	protected void addRemoveMouseListenerToSlot(final EquipmentSlot slot)
	{
		setComponentPopupMenuToSlot(slot);
		JPopupMenu popupMenu = slot.getComponentPopupMenu();
		JMenuItem removeSlot = new JMenuItem("Remove Item from Slot");
		popupMenu.add(removeSlot);
		removeSlot.addActionListener(e ->
			removeItemFromSlot(slot));
	}

	private void setComponentPopupMenuToSlot(final EquipmentSlot slot)
	{
		if (slot.getComponentPopupMenu() == null)
		{
			JPopupMenu newMenu = new JPopupMenu();
			slot.setComponentPopupMenu(newMenu);
			slot.getImageLabel().setComponentPopupMenu(newMenu);
		}
	}

	protected void setContainerSlot(final EquipmentSlot containerSlot, final Equipment setup, final EquipmentItem item, boolean locked)
	{
		containerSlot.setParentSetup(setup);
		containerSlot.setLocked(locked);

		if (item.getId() == -1)
		{
			containerSlot.setImageLabel(null, null);
			return;
		}

		int itemId = item.getId();
		int quantity = item.getQuantity();
		final String itemName = item.getName();
		AsyncBufferedImage itemImg = itemManager.getImage(itemId, quantity, quantity > 1);
		String toolTip = itemName;
		if (quantity > 1)
		{
			toolTip += " (" + quantity + ")";
		}
		containerSlot.setImageLabel(toolTip, itemImg);
	}

	public void setupContainerPanel(final JPanel containerSlotsPanel)
	{
		this.equipmentSlots = new HashMap<>();
		for (EquipmentInventorySlot slot : EquipmentInventorySlot.values())
		{
			final EquipmentSlot setupSlot = new EquipmentSlot(slot.getSlotIdx());
			addUpdateFromContainerMouseListenerToSlot(setupSlot);
			addUpdateFromSearchMouseListenerToSlot(setupSlot);
			addRemoveMouseListenerToSlot(setupSlot);
			equipmentSlots.put(slot, setupSlot);
		}

		final GridLayout gridLayout = new GridLayout(5, 3, 1, 1);
		containerSlotsPanel.setLayout(gridLayout);

		containerSlotsPanel.add(new EquipmentSlot(-1));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.HEAD));
		containerSlotsPanel.add(new EquipmentSlot(-1));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.CAPE));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.AMULET));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.AMMO));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.WEAPON));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.BODY));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.SHIELD));
		containerSlotsPanel.add(new EquipmentSlot(-1));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.LEGS));
		containerSlotsPanel.add(new EquipmentSlot(-1));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.GLOVES));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.BOOTS));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.RING));
	}

	public void setSlots(final Equipment setup)
	{
		for (final EquipmentInventorySlot slot : EquipmentInventorySlot.values())
		{
			int i = slot.getSlotIdx();
			setContainerSlot(equipmentSlots.get(slot), setup, setup.getEquipment().get(i), false);
		}

		validate();
		repaint();
	}

	private void updateEquipmentFromGame()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			JOptionPane.showMessageDialog(ClientUI.getFrame(),
				"You must be logged in to update from your equipment.",
				ChinManagerPlugin.PLUGIN_NAME,
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		clientThread.invokeLater(() ->
		{
			for (final EquipmentSlot slot : equipmentSlots.values())
			{
				final ArrayList<EquipmentItem> container = getContainerFromSlot(slot);

				final ArrayList<EquipmentItem> playerContainer = getNormalizedContainer();
				final EquipmentItem newItem = playerContainer.get(slot.getIndexInSlot());

				container.set(slot.getIndexInSlot(), newItem);
			}

			chinManagerPlugin.updateConfig();
			refreshCurrentSetup();
		});

	}

	private void updateSlotFromContainer(final EquipmentSlot slot)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			JOptionPane.showMessageDialog(ClientUI.getFrame(),
				"You must be logged in to update from your equipment.",
				ChinManagerPlugin.PLUGIN_NAME,
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		final ArrayList<EquipmentItem> container = getContainerFromSlot(slot);

		clientThread.invokeLater(() ->
		{
			final ArrayList<EquipmentItem> playerContainer = getNormalizedContainer();
			final EquipmentItem newItem = playerContainer.get(slot.getIndexInSlot());

			container.set(slot.getIndexInSlot(), newItem);
			chinManagerPlugin.updateConfig();
			refreshCurrentSetup();
		});
	}

	private void updateSlotFromSearch(final EquipmentSlot slot)
	{

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			JOptionPane.showMessageDialog(ClientUI.getFrame(),
				"You must be logged in to search.",
				ChinManagerPlugin.PLUGIN_NAME,
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		chatboxItemSearch
			.tooltipText("Set slot to")
			.onItemSelected((itemId) ->
				clientThread.invokeLater(() ->
				{
					int finalId = itemManager.canonicalize(itemId);

					if (itemManager.getItemComposition(finalId).isStackable())
					{
						final int finalIdCopy = finalId;
						chatboxPanelManager.openTextInput("Enter amount")
							.addCharValidator(arg -> arg >= 48 && arg <= 57)
							.onDone((input) ->
							{
								clientThread.invokeLater(() ->
								{
									String inputParsed = input;
									if (inputParsed.length() > 10)
									{
										inputParsed = inputParsed.substring(0, 10);
									}

									long quantityLong = Long.parseLong(inputParsed);
									int quantity = (int) Math.min(quantityLong, Integer.MAX_VALUE);
									quantity = Math.max(quantity, 1);

									final String itemName = itemManager.getItemComposition(finalIdCopy).getName();
									final EquipmentItem newItem = new EquipmentItem(finalIdCopy, itemName, quantity);
									final ArrayList<EquipmentItem> container = getContainerFromSlot(slot);

									container.set(slot.getIndexInSlot(), newItem);
									chinManagerPlugin.updateConfig();
									refreshCurrentSetup();

								});
							}).build();
					}
					else
					{
						final String itemName = itemManager.getItemComposition(finalId).getName();
						final EquipmentItem newItem = new EquipmentItem(finalId, itemName, 1);
						final ArrayList<EquipmentItem> container = getContainerFromSlot(slot);
						container.set(slot.getIndexInSlot(), newItem);

						chinManagerPlugin.updateConfig();
						refreshCurrentSetup();
					}

				}))
			.build();
	}

	private void removeItemFromSlot(final EquipmentSlot slot)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			JOptionPane.showMessageDialog(ClientUI.getFrame(),
				"You must be logged in to remove item from the slot.",
				ChinManagerPlugin.PLUGIN_NAME,
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		clientThread.invokeLater(() ->
		{
			final ArrayList<EquipmentItem> container = getContainerFromSlot(slot);

			final EquipmentItem dummyItem = new EquipmentItem(-1, "", 0);

			container.set(slot.getIndexInSlot(), dummyItem);
			chinManagerPlugin.updateConfig();
			refreshCurrentSetup();
		});
	}

	private void refreshCurrentSetup()
	{
		String selectedPlugin = Plugins.sanitizedName(String.valueOf(pluginList.getSelectedItem()));
		Map<Plugin, Map<String, String>> pluginsConfig = chinManager.getPluginConfig();

		if (pluginsConfig.containsKey(chinManager.getPlugin(selectedPlugin)))
		{
			Map<String, String> pluginConfig = pluginsConfig.get(chinManager.getPlugin(selectedPlugin));

			noBanking.setVisible(true);

			subPanel.removeAll();

			if (pluginConfig.containsKey("banking") && Boolean.parseBoolean(pluginConfig.get("banking")))
			{
				subPanel.add(containerSlotsPanel, BorderLayout.NORTH);
				subPanel.add(copyEquipmentButton, BorderLayout.SOUTH);
			}
			else
			{
				subPanel.add(noBanking, BorderLayout.NORTH);
			}

			subPanel.revalidate();
			subPanel.repaint();
		}
		else
		{
			return;
		}

		if (ChinManagerPlugin.getEquipmentList().stream().noneMatch((setup) -> setup.getName().equals(selectedPlugin)))
		{
			ChinManagerPlugin.getEquipmentList().add(new Equipment(emptyContainer(), selectedPlugin));
			chinManagerPlugin.updateConfig();
		}

		Equipment equipmentSetup = ChinManagerPlugin.getEquipmentList().stream().filter((setup) -> setup.getName().equals(selectedPlugin)).findFirst().get();
		setSlots(equipmentSetup);

		if (chinManager.getRequiredItems().containsKey(chinManager.getPlugin(selectedPlugin)))
		{
			Map<Integer, Map<String, String>> requiredItems = chinManager.getRequiredItems().get(chinManager.getPlugin(selectedPlugin));

			for (Map.Entry<Integer, Map<String, String>> i : requiredItems.entrySet())
			{
				Map<String, String> item = i.getValue();

				EquipmentSlot containerSlot = equipmentSlots.values().stream().filter((slot) -> slot.getIndexInSlot() == i.getKey()).findFirst().get();
				ArrayList<EquipmentItem> container = getContainerFromSlot(containerSlot);


				EquipmentItem equipmentItem = new EquipmentItem(Integer.parseInt(item.get("id"), 10), item.get("name"), Integer.parseInt(item.get("quantity"), 10));
				container.set(containerSlot.getIndexInSlot(), equipmentItem);

				setContainerSlot(containerSlot, equipmentSetup, equipmentItem, true);
			}

			chinManagerPlugin.updateConfig();
		}

		validate();
		repaint();
	}

	private ArrayList<EquipmentItem> emptyContainer()
	{
		ArrayList<EquipmentItem> newContainer = new ArrayList<>();

		for (int i = 0; i < NUM_EQUIPMENT_ITEMS; i++)
		{
			newContainer.add(new EquipmentItem(-1, "", 0));
		}

		return newContainer;
	}

	private ArrayList<EquipmentItem> getNormalizedContainer()
	{
		final ItemContainer container = client.getItemContainer(InventoryID.EQUIPMENT);

		ArrayList<EquipmentItem> newContainer = new ArrayList<>();

		Item[] items = null;
		if (container != null)
		{
			items = container.getItems();
		}

		for (int i = 0; i < NUM_EQUIPMENT_ITEMS; i++)
		{
			if (items == null || i >= items.length)
			{
				newContainer.add(new EquipmentItem(-1, "", 0));
			}
			else
			{
				final Item item = items[i];
				String itemName = "";

				if (client.isClientThread())
				{
					itemName = itemManager.getItemComposition(item.getId()).getName();
				}
				newContainer.add(new EquipmentItem(item.getId(), itemName, item.getQuantity()));
			}
		}

		return newContainer;
	}

	private ArrayList<EquipmentItem> getContainerFromSlot(final EquipmentSlot slot)
	{
		return slot.getParentSetup().getEquipment();
	}
}
