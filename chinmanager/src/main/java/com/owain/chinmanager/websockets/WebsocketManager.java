package com.owain.chinmanager.websockets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.openosrs.client.OpenOSRS;
import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import static com.owain.chinmanager.api.AccountApi.DEBUG;
import com.owain.chinmanager.ui.plugins.status.InfoPanel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.Plugin;
import static net.runelite.http.api.RuneLiteAPI.GSON;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class WebsocketManager extends WebSocketListener
{
	public static final List<Disposable> DISPOSABLES = new ArrayList<>();

	private final ChinManagerPlugin chinManagerPlugin;
	private final @NonNull ChinManager chinManager;
	private final InfoPanel infoPanel;
	@Nullable
	public String token = null;
	private int ticksWithoutFriend = 0;
	private @NonNull Boolean isSocketConnected = false;
	private @NonNull Boolean broadcasting = false;

	@Nullable
	private WebSocket socket;

	@Inject
	WebsocketManager(ChinManagerPlugin chinManagerPlugin, @NonNull ChinManager chinManager, InfoPanel infoPanel)
	{
		this.chinManagerPlugin = chinManagerPlugin;
		this.chinManager = chinManager;
		this.infoPanel = infoPanel;

		DISPOSABLES.addAll(
			List.of(
				chinManager
					.getActiveObservable()
					.subscribe(this::activePlugins),
				Observable
					.interval(500, TimeUnit.MILLISECONDS)
					.observeOn(Schedulers.io())
					.subscribe(this::milliseconds)
			)
		);
	}

	private static @NonNull Request getWebsocketUrl()
	{
		Request.Builder request = new Request.Builder();

		if (DEBUG)
		{
			return request
				.url("ws://localhost:5001")
				.build();
		}
		else
		{
			return request
				.url("wss://chinplugins.xyz/status")
				.build();
		}
	}

	private void milliseconds(Long aLong)
	{
		if (!broadcasting)
		{
			return;
		}

		ticksWithoutFriend++;

		if (ticksWithoutFriend > 6)
		{
			broadcasting = false;
			ticksWithoutFriend = 0;
		}

		status();
		location();

		inventory(InventoryID.INVENTORY);
		inventory(InventoryID.EQUIPMENT);
	}

	private void status()
	{
		JsonObject jsonData = new JsonObject();
		jsonData.addProperty("event", "plugin-status");
		JsonObject jsonGeneralData = new JsonObject();
		JsonArray jsonArray = new JsonArray();

		jsonGeneralData.addProperty("breaking", infoPanel.breaking);
		jsonGeneralData.addProperty("breakingtime", infoPanel.breakingTimeLabel.getText());
		jsonGeneralData.addProperty("runtime", infoPanel.runtimeLabel.getText());
		jsonGeneralData.addProperty("ingametime", infoPanel.inGameLabel.getText());
		jsonGeneralData.addProperty("breaktime", infoPanel.breakTimeLabel.getText());
		jsonGeneralData.addProperty("breaks", infoPanel.breaksLabel.getText());
		jsonGeneralData.addProperty("plannedbreakshowing", InfoPanel.breakShowing);
		jsonGeneralData.addProperty("plannedbreak", infoPanel.scheduledTimeLabel.getText());

		Plugin next = chinManager.getNextActive();
		if (next == null)
		{
			jsonGeneralData.addProperty("next", "");
		}
		else
		{
			jsonGeneralData.addProperty("next", chinManager.getNextActive().getName());
		}

		Plugin currentlyActive = chinManager.getCurrentlyActive();
		if (currentlyActive == null)
		{
			jsonGeneralData.addProperty("active", "");
		}
		else
		{
			jsonGeneralData.addProperty("active", chinManager.getCurrentlyActive().getName());
		}

		for (Plugin plugin : chinManager.getActivePlugins())
		{
			if (chinManager.getExtraData().containsKey(plugin))
			{
				JsonObject pluginData = GSON.toJsonTree(chinManager.getExtraData().get(plugin)).getAsJsonObject();
				pluginData.addProperty("plugin", plugin.getName());

				jsonArray.add(pluginData);
			}
		}

		JsonObject jsonDataData = new JsonObject();
		jsonData.add("data", jsonDataData);

		jsonDataData.addProperty("client", OpenOSRS.uuid);
		jsonDataData.add("status", jsonGeneralData);
		jsonDataData.add("plugins", jsonArray);

		sendMessage(jsonData);
	}

	private void inventory(@NonNull InventoryID inventoryID)
	{
		if (chinManagerPlugin.getClient().getLocalPlayer() == null)
		{
			return;
		}

		Client client = chinManagerPlugin.getClient();

		Item[] items = invokeAndWait(() -> {
			ItemContainer itemContainer = client.getItemContainer(inventoryID);
			if (itemContainer != null)
			{
				if (inventoryID == InventoryID.INVENTORY)
				{
					return IntStream.range(0, 28).mapToObj(itemContainer::getItem).toArray(Item[]::new);
				}
				else if (inventoryID == InventoryID.EQUIPMENT)
				{
					ArrayList<Item> equipmentItems = new ArrayList<>();
					for (final EquipmentInventorySlot slot : EquipmentInventorySlot.values())
					{
						int i = slot.getSlotIdx();
						equipmentItems.add(itemContainer.getItem(i));
					}

					return equipmentItems.toArray(new Item[0]);
				}
			}
			return null;
		});

		if (items == null)
		{
			return;
		}

		JsonObject jsonData = new JsonObject();
		jsonData.addProperty("event", inventoryID.name().toLowerCase());
		JsonArray jsonContainers = new JsonArray();

		for (Item item : items)
		{
			JsonObject itemJson = new JsonObject();

			if (item != null)
			{
				itemJson.addProperty("id", item.getId());
				itemJson.addProperty("quantity", item.getQuantity());
			}
			else
			{
				itemJson.addProperty("id", -1);
				itemJson.addProperty("quantity", 0);
			}

			jsonContainers.add(itemJson);
		}

		JsonObject jsonDataData = new JsonObject();
		jsonData.add("data", jsonDataData);

		jsonDataData.addProperty("client", OpenOSRS.uuid);
		jsonDataData.add("container", jsonContainers);

		sendMessage(jsonData);
	}

	private void location()
	{
		if (chinManagerPlugin.getClient().getLocalPlayer() == null)
		{
			return;
		}

		Client client = chinManagerPlugin.getClient();

		WorldPoint worldPoint = invokeAndWait(() -> {
			if (client.isInInstancedRegion())
			{
				return WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
			}
			else
			{
				return client.getLocalPlayer().getWorldLocation();
			}
		});

		if (worldPoint == null)
		{
			return;
		}

		JsonObject jsonData = new JsonObject();
		jsonData.addProperty("event", "location");
		JsonObject jsonLocation = new JsonObject();
		jsonLocation.addProperty("x", worldPoint.getX());
		jsonLocation.addProperty("y", worldPoint.getY());
		jsonLocation.addProperty("z", worldPoint.getPlane());
		jsonLocation.addProperty("client", OpenOSRS.uuid);
		jsonData.add("data", jsonLocation);

		sendMessage(jsonData);
	}

	private <T> T invokeAndWait(@NonNull Callable<T> r)
	{
		try
		{
			AtomicReference<T> ref = new AtomicReference<>();
			Semaphore semaphore = new Semaphore(0);
			chinManagerPlugin.getClientThread().invokeLater(() -> {
				try
				{

					ref.set(r.call());
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
				finally
				{
					semaphore.release();
				}
			});
			semaphore.acquire();
			return ref.get();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private void activePlugins(@NonNull SortedSet<Plugin> plugins)
	{
		if (token == null || token.isEmpty())
		{
			closeSocket();
		}
		else if (plugins.size() == 0)
		{
			closeSocket();
		}
		else if (plugins.size() > 0)
		{
			createSocket();
		}
	}

	private void createSocket()
	{
		log.debug("Create socket");
		if (!isSocketConnected)
		{
			socket = chinManagerPlugin.getOkHttpClient().newWebSocket(getWebsocketUrl(), this);
			log.debug("Socket created");
		}
		else
		{
			log.debug("Socket did already exist");
		}
	}

	private void sendMessage(@NonNull JsonObject message)
	{
		if (!isSocketConnected || socket == null || token == null || token.isEmpty())
		{
			return;
		}

		log.debug("Send message: {}", message.toString());

		socket.send(message.toString());
	}

	private void sendAuth()
	{
		if (!isSocketConnected || socket == null || token == null || token.isEmpty())
		{
			return;
		}

		JsonObject payloadObject = new JsonObject();
		JsonObject dataObject = new JsonObject();

		payloadObject.addProperty("event", "auth");
		payloadObject.add("data", dataObject);

		dataObject.addProperty("token", token);
		dataObject.addProperty("type", "plugin");

		socket.send(payloadObject.toString());
	}

	private void closeSocket()
	{
		if (socket != null && isSocketConnected)
		{
			this.socket.close(1000, null);
		}
	}

	@Override
	public void onClosed(WebSocket webSocket, int code, String reason)
	{
		isSocketConnected = false;
		socket = null;
		broadcasting = false;
	}

	@Override
	public void onFailure(WebSocket webSocket, Throwable t, Response response)
	{
		isSocketConnected = false;
		socket = null;
		broadcasting = false;
		createSocket();
	}

	@Override
	public void onMessage(WebSocket webSocket, String text)
	{
		JsonObject message = GSON.fromJson(text, JsonObject.class);

		log.debug("Msg: {}", message);

		if (message.has("event"))
		{
			String event = message.get("event").getAsString();

			switch (event)
			{
				case "auth":
					sendAuth();
					break;
				case "broadcast":
					broadcasting = true;
					break;
				case "stopbroadcast":
					broadcasting = false;
					break;
			}
		}
	}

	@Override
	public void onOpen(WebSocket webSocket, Response response)
	{
		isSocketConnected = true;
		broadcasting = false;
	}
}
