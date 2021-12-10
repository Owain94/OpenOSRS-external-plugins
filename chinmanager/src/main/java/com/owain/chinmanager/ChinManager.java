package com.owain.chinmanager;

import com.owain.chinmanager.ui.gear.Equipment;
import com.owain.chinmanager.ui.gear.EquipmentItem;
import com.owain.chinmanager.utils.MapUtil;
import com.owain.chinmanager.utils.Plugins;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("unused")
@Singleton
@Slf4j
public class ChinManager
{
	private final Comparator<Plugin> pluginComparable = new Comparator<>()
	{
		@Override
		public int compare(Plugin plugin1, Plugin plugin2)
		{
			Long p1 = (long) Integer.parseInt(pluginConfig.get(plugin1).get("combiningPriority"));
			Long p2 = (long) Integer.parseInt(pluginConfig.get(plugin2).get("combiningPriority"));

			return p2.compareTo(p1);
		}
	};

	private final Set<Plugin> managerPlugins = new TreeSet<>((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
	private final PublishSubject<Set<Plugin>> managerPluginsSubject = PublishSubject.create();

	private final Map<Plugin, Boolean> plugins = new TreeMap<>((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
	private final PublishSubject<Map<Plugin, Boolean>> pluginsSubject = PublishSubject.create();

	private final SortedSet<Plugin> activePlugins;
	private final PublishSubject<SortedSet<Plugin>> activePluginsSubject = PublishSubject.create();

	private final Set<Plugin> stoppedPlugins = new HashSet<>();
	private final PublishSubject<Set<Plugin>> stoppedPluginsSubject = PublishSubject.create();

	private Plugin currentlyActive = null;
	private final PublishSubject<String> currentlyActiveSubject = PublishSubject.create();

	private final Map<Plugin, Pair<Instant, Integer>> plannedBreaks = new HashMap<>();
	private final PublishSubject<Map<Plugin, Pair<Instant, Integer>>> plannedBreaksSubject = PublishSubject.create();

	private final Set<Plugin> handover = new HashSet<>();
	private final PublishSubject<Set<Plugin>> handoverSubject = PublishSubject.create();

	private final Map<String, String> warnings = new HashMap<>();
	private final PublishSubject<Map<String, String>> warningsSubject = PublishSubject.create();

	private boolean isBanking = false;
	private Plugin bankingPlugin;
	private final PublishSubject<Plugin> bankingSubject = PublishSubject.create();

	private boolean isTeleporting = false;
	private Location teleportingLocation = null;
	private final PublishSubject<Plugin> teleportingSubject = PublishSubject.create();

	private final Map<Plugin, Map<String, String>> pluginConfig = new HashMap<>();
	private final Map<Plugin, Location> startLocations = new HashMap<>();

	private final Map<Plugin, Map<Integer, Map<String, String>>> requiredItems = new HashMap<>();

	private final Map<Plugin, Instant> activeBreaks = new HashMap<>();
	private final PublishSubject<Map<Plugin, Instant>> activeBreaksSubject = PublishSubject.create();

	private final Map<Plugin, Instant> startTimes = new HashMap<>();
	private int amountOfBreaks = 0;

	private final PublishSubject<Plugin> logoutActionSubject = PublishSubject.create();
	private final PublishSubject<Plugin> hopSubject = PublishSubject.create();

	public final PublishSubject<ConfigChanged> configChanged = PublishSubject.create();
	public final PublishSubject<GameStateChanged> gameStateChanged = PublishSubject.create();

	private final Map<Plugin, Map<String, String>> extraData = new HashMap<>();
	private final PublishSubject<Map<Plugin, Map<String, String>>> extraDataSubject = PublishSubject.create();

	private final Map<Plugin, Set<Integer>> bankItems = new HashMap<>();

	ChinManager()
	{
		this.activePlugins = new TreeSet<>(pluginComparable);
	}

	public Set<Plugin> getManagerPlugins()
	{
		return managerPlugins;
	}

	public void registerManagerPlugin(Plugin plugin)
	{
		managerPlugins.add(plugin);
		managerPluginsSubject.onNext(managerPlugins);
	}

	public void unregisterManagerPlugin(Plugin plugin)
	{
		managerPlugins.remove(plugin);
		managerPluginsSubject.onNext(managerPlugins);

		reset(plugin);
	}

	public @NonNull Observable<Set<Plugin>> getManagerPluginObservable()
	{
		return managerPluginsSubject.hide();
	}

	public Map<Plugin, Boolean> getPlugins()
	{
		return plugins;
	}

	public void registerPlugin(Plugin plugin)
	{
		registerPlugin(plugin, true);
	}

	public void registerPlugin(Plugin plugin, boolean configurable)
	{
		plugins.put(plugin, configurable);
		pluginsSubject.onNext(plugins);
	}

	public void unregisterPlugin(Plugin plugin)
	{
		plugins.remove(plugin);
		pluginsSubject.onNext(plugins);

		reset(plugin);
	}

	public void reset(Plugin plugin)
	{
		activePlugins.remove(plugin);
		activePluginsSubject.onNext(activePlugins);

		activeBreaks.remove(plugin);
		activeBreaksSubject.onNext(activeBreaks);

		plannedBreaks.remove(plugin);
		plannedBreaksSubject.onNext(plannedBreaks);

		handover.remove(plugin);
		handoverSubject.onNext(handover);

		startTimes.remove(plugin);
	}

	public @NonNull Observable<Map<Plugin, Boolean>> getPluginObservable()
	{
		return pluginsSubject.hide();
	}

	public Set<Plugin> getActivePlugins()
	{
		return activePlugins
			.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	@Nullable
	public Plugin getPlugin(String pluginName)
	{
		for (Plugin plugin : managerPlugins)
		{
			if (Plugins.sanitizedName(plugin).equals(Plugins.sanitizedName(pluginName)))
			{
				return plugin;
			}
		}

		return null;
	}

	public void startPlugin(Plugin plugin)
	{
		activePlugins.add(plugin);
		activePluginsSubject.onNext(activePlugins);

		startTimes.put(plugin, Instant.now());
	}

	public void startPlugins(List<Plugin> plugins)
	{
		for (Plugin plugin : plugins)
		{
			activePlugins.add(plugin);
			startTimes.put(plugin, Instant.now());
		}

		activePluginsSubject.onNext(activePlugins);
	}

	public void stopPlugin(Plugin plugin)
	{
		stoppedPlugins.add(plugin);
		stoppedPluginsSubject.onNext(stoppedPlugins);

		reset(plugin);
	}

	public void removeStoppedPlugin(Plugin plugin)
	{
		stoppedPlugins.remove(plugin);
		stoppedPluginsSubject.onNext(stoppedPlugins);
	}

	public Set<Plugin> getStoppedPlugins()
	{
		return stoppedPlugins;
	}

	public @NonNull Observable<Set<Plugin>> getStoppedPluginsObservable()
	{
		return stoppedPluginsSubject.hide();
	}

	public void removePlannedBreak(Plugin plugin)
	{
		plannedBreaks.remove(plugin);
		plannedBreaksSubject.onNext(plannedBreaks);
	}

	public void removeHandover(Plugin plugin)
	{
		handover.remove(plugin);
		handoverSubject.onNext(handover);
	}

	public @NonNull Observable<SortedSet<Plugin>> getActiveObservable()
	{
		return activePluginsSubject.hide();
	}

	public Map<Plugin, Pair<Instant, Integer>> getPlannedBreaks()
	{
		return plannedBreaks;
	}

	public void updatePlannedBreaks()
	{
		for (Map.Entry<Plugin, Pair<Instant, Integer>> i : Map.copyOf(plannedBreaks).entrySet())
		{
			Plugin plugin = i.getKey();
			Pair<Instant, Integer> breakPair = i.getValue();

			if (currentlyActive == plugin)
			{
				continue;
			}

			plannedBreaks.put(plugin, Pair.of(breakPair.getLeft().plus(250, ChronoUnit.MILLIS), breakPair.getRight()));
		}
	}

	public void updatePlannedBreakValue(Plugin plugin, Integer duration)
	{
		if (plannedBreaks.containsKey(plugin))
		{
			Pair<Instant, Integer> plannedBreak = plannedBreaks.get(plugin);
			plannedBreaks.put(plugin, Pair.of(plannedBreak.getLeft(), duration));
		}
	}

	public void planBreak(Plugin plugin, Instant when, int duration)
	{
		plannedBreaks.put(plugin, Pair.of(when, duration));
		plannedBreaksSubject.onNext(plannedBreaks);
	}

	public @NonNull Observable<Map<Plugin, Pair<Instant, Integer>>> getPlannedBreaksObservable()
	{
		return plannedBreaksSubject.hide();
	}

	public boolean isBreakPlanned(Plugin plugin)
	{
		return plannedBreaks.containsKey(plugin);
	}

	public Pair<Instant, Integer> getPlannedBreak(Plugin plugin)
	{
		return plannedBreaks.get(plugin);
	}

	public boolean shouldBreak(Plugin plugin)
	{
		if (!plannedBreaks.containsKey(plugin))
		{
			return false;
		}

		return Instant.now().isAfter(getPlannedBreak(plugin).getLeft());
	}

	public void planHandover(Plugin plugin)
	{
		handover.add(plugin);
		handoverSubject.onNext(handover);
	}

	public @NonNull Observable<Set<Plugin>> getHandoverObservable()
	{
		return handoverSubject.hide();
	}

	public void handover()
	{
		Plugin next = handover.stream().findFirst().orElse(null);
		if (next == null)
		{
			return;
		}

		handover.remove(next);

		setCurrentlyActive(next);
	}

	public boolean shouldHandover()
	{
		return !handover.isEmpty();
	}

	public Set<Plugin> getHandover()
	{
		return handover;
	}

	public List<EquipmentItem> getGearForPlugin(Plugin plugin)
	{
		Equipment equipment = ChinManagerPlugin.getEquipmentList().stream().filter((setup) -> setup.getName().equals(Plugins.sanitizedName(plugin))).findFirst().orElse(null);

		if (equipment == null)
		{
			return List.of();
		}

		return equipment.getEquipment();
	}

	public boolean isBanking()
	{
		return isBanking;
	}

	public void bankCurrent()
	{
		isBanking = true;

		bankingPlugin = currentlyActive;
		bankingSubject.onNext(bankingPlugin);
	}

	public void bankNext(Plugin plugin, Instant instant)
	{
		Plugin next = getNextActive(plugin, instant);
		if (next == getCurrentlyActive() || next == null)
		{
			return;
		}

		isBanking = true;

		bankingPlugin = next;
		bankingSubject.onNext(bankingPlugin);
	}

	public void bankingDone()
	{
		isBanking = false;
	}

	public Plugin bankingPlugin()
	{
		return bankingPlugin;
	}

	public @NonNull Observable<Plugin> getBankingObservable()
	{
		return bankingSubject.hide();
	}

	public boolean isTeleporting()
	{
		return isTeleporting;
	}

	public void teleport(Location location)
	{
		isTeleporting = true;
		teleportingLocation = location;
		teleportingSubject.onNext(currentlyActive);
	}

	public void teleportingDone()
	{
		teleportingLocation = null;
		isTeleporting = false;
	}

	public Location getTeleportingLocation()
	{
		return teleportingLocation;
	}

	public @NonNull Observable<Plugin> getTeleportingObservable()
	{
		return teleportingSubject.hide();
	}

	public void setPluginConfig(Plugin plugin, Map<String, String> data)
	{
		pluginConfig.putIfAbsent(plugin, new LinkedHashMap<>());

		Map.copyOf(data).forEach(
			(key, value) -> pluginConfig.get(plugin).merge(key, value, (existingData, newData) -> newData)
		);
	}

	public Map<Plugin, Map<String, String>> getPluginConfig()
	{
		return pluginConfig;
	}

	public void setStartLocation(Plugin plugin, Location location)
	{
		startLocations.put(plugin, location);
	}

	public Map<Plugin, Location> getStartLocations()
	{
		return startLocations;
	}

	public Location getStartLocation(Plugin plugin)
	{
		return startLocations.get(plugin);
	}

	public void setRequiredItems(Plugin plugin, Map<Integer, Map<String, String>> data)
	{
		requiredItems.put(plugin, data);
	}

	public Map<Plugin, Map<Integer, Map<String, String>>> getRequiredItems()
	{
		return requiredItems;
	}

	public Map<Plugin, Instant> getActiveBreaks()
	{
		return activeBreaks;
	}

	public void startBreak(Plugin plugin)
	{
		Instant breakUntil = Instant.now().plus(plannedBreaks.get(plugin).getRight(), ChronoUnit.SECONDS);
		removePlannedBreak(plugin);

		activeBreaks.put(plugin, breakUntil);
		activeBreaksSubject.onNext(activeBreaks);
	}

	public void startBreak(Plugin plugin, Instant instant)
	{
		removePlannedBreak(plugin);

		activeBreaks.put(plugin, instant);
		activeBreaksSubject.onNext(activeBreaks);
	}

	public boolean isCurrentlyActive(Plugin plugin)
	{
		return plugin == currentlyActive;
	}

	public Plugin getCurrentlyActive()
	{
		return currentlyActive;
	}

	public void setCurrentlyActive(Plugin plugin)
	{
		currentlyActive = plugin;
		currentlyActiveSubject.onNext(Plugins.sanitizedName(plugin));

		handover.remove(plugin);

		activeBreaks.remove(plugin);
	}

	public boolean isNextSelf(Plugin plugin, Instant instant)
	{
		Plugin next = getNextActive(plugin, instant);

		return next == null || next == currentlyActive;
	}

	@Nullable
	public Plugin getNextActive()
	{
		return getNextActive(null, null);
	}

	@Nullable
	public Plugin getNextActive(Plugin plugin, Instant instant)
	{
		if (activePlugins.size() == 1)
		{
			return activePlugins.first();
		}
		else if (shouldHandover())
		{
			return handover.stream().findFirst().orElse(null);
		}
		else
		{
			Plugin next = null;
			for (Plugin activePlugin : activePlugins)
			{
				if (!activeBreaks.containsKey(activePlugin) && activePlugin != currentlyActive)
				{
					next = activePlugin;
					break;
				}
			}

			if (next == null)
			{
				Map<Plugin, Instant> breaksMap = new HashMap<>(Map.copyOf(activeBreaks));
				if (plugin != null && instant != null)
				{
					breaksMap.put(plugin, instant);
				}
				Map<Plugin, Instant> sorted = MapUtil.sortByValue(breaksMap, Map.Entry.comparingByValue());

				if (sorted.isEmpty())
				{
					for (Plugin activePlugin : activePlugins)
					{
						if (currentlyActive == null || !activeBreaks.containsKey(activePlugin))
						{
							return activePlugin;
						}
					}
				}
				else
				{
					return MapUtil.getFirst(MapUtil.sortByValue(breaksMap, Map.Entry.comparingByValue())).getKey();
				}
			}
			else
			{
				return next;
			}
		}

		return null;
	}

	public @NonNull Observable<String> getCurrentlyActiveObservable()
	{
		return currentlyActiveSubject.hide();
	}

	public void stopBreak(Plugin plugin)
	{
		activeBreaks.remove(plugin);
		activeBreaksSubject.onNext(activeBreaks);
	}

	public Map<Plugin, Map<String, String>> getExtraData()
	{
		return extraData;
	}

	public void setExtraData(Plugin plugin, String key, String value)
	{
		extraData.putIfAbsent(plugin, new LinkedHashMap<>());
		extraData.get(plugin).put(key, value);

		extraDataSubject.onNext(extraData);
	}

	public void setExtraData(Plugin plugin, Map<String, String> data)
	{
		extraData.putIfAbsent(plugin, new LinkedHashMap<>());

		for (Map.Entry<String, String> i : data.entrySet())
		{
			extraData.get(plugin).put(i.getKey(), i.getValue());
		}

		extraDataSubject.onNext(extraData);
	}

	public void removeExtraData(Plugin plugin, String key)
	{
		if (!extraData.containsKey(plugin))
		{
			return;
		}

		extraData.get(plugin).remove(key);
		extraDataSubject.onNext(extraData);
	}

	public void resetExtraData(Plugin plugin)
	{
		extraData.remove(plugin);
		extraDataSubject.onNext(extraData);
	}

	public Map<String, String> getWarnings()
	{
		return warnings;
	}

	public @NonNull Observable<Map<String, String>> getWarningsObservable()
	{
		return warningsSubject.hide();
	}

	public void addWarning(String header, String message)
	{
		if (warnings.containsKey(header))
		{
			return;
		}

		warnings.put(header, message);
		warningsSubject.onNext(warnings);
	}

	public void removeWarning(String header)
	{
		warnings.remove(header);
		warningsSubject.onNext(warnings);
	}

	public void resetWarnings()
	{
		warnings.clear();
		warningsSubject.onNext(warnings);
	}

	public @NonNull Observable<Map<Plugin, Map<String, String>>> getExtraDataObservable()
	{
		return extraDataSubject.hide();
	}

	public @NonNull Observable<Map<Plugin, Instant>> getActiveBreaksObservable()
	{
		return activeBreaksSubject.hide();
	}

	public boolean isBreakActive(Plugin plugin)
	{
		return activeBreaks.containsKey(plugin);
	}

	public Instant getActiveBreak(Plugin plugin)
	{
		return activeBreaks.get(plugin);
	}

	public void logoutNow(Plugin plugin)
	{
		logoutActionSubject.onNext(plugin);
	}

	public @NonNull Observable<Plugin> getlogoutActionObservable()
	{
		return logoutActionSubject.hide();
	}

	public void hopNow(Plugin plugin)
	{
		hopSubject.onNext(plugin);
	}

	public @NonNull Observable<Plugin> hopNowObservable()
	{
		return hopSubject.hide();
	}

	public Map<Plugin, Instant> getStartTimes()
	{
		return startTimes;
	}

	public void addAmountOfBreaks()
	{
		amountOfBreaks++;
	}

	public int getAmountOfBreaks()
	{
		return amountOfBreaks;
	}

	public void setAmountOfBreaks(int breaks)
	{
		amountOfBreaks = breaks;
	}

	public void addBankItems(Plugin plugin, Set<Integer> items)
	{
		if (bankItems.containsKey(plugin))
		{
			bankItems.put(plugin, Stream.concat(bankItems.get(plugin).stream(), items.stream())
				.collect(Collectors.toSet()));
		}
		else
		{
			bankItems.put(plugin, items);
		}
	}

	public Map<Plugin, Set<Integer>> getBankItems()
	{
		return bankItems;
	}
}
