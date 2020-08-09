package com.owain.chinbreakhandler;

import static com.owain.chinbreakhandler.ChinBreakHandlerPlugin.sanitizedName;
import com.owain.chinbreakhandler.ui.utils.IntRandomNumberGenerator;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("unused")
@Singleton
public class ChinBreakHandler
{
	private final ConfigManager configManager;

	private final Map<Plugin, Boolean> plugins = new TreeMap<>((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
	private final PublishSubject<Map<Plugin, Boolean>> pluginsSubject = PublishSubject.create();

	private final Set<Plugin> activePlugins = new HashSet<>();
	private final PublishSubject<Set<Plugin>> activeSubject = PublishSubject.create();

	private final Map<Plugin, Instant> plannedBreaks = new HashMap<>();
	private final PublishSubject<Map<Plugin, Instant>> plannedBreaksSubject = PublishSubject.create();

	private final Map<Plugin, Instant> activeBreaks = new HashMap<>();
	private final PublishSubject<Map<Plugin, Instant>> activeBreaksSubject = PublishSubject.create();
	private final PublishSubject<Pair<Plugin, Instant>> currentActiveBreaksSubject = PublishSubject.create();

	private final Map<Plugin, Instant> startTimes = new HashMap<>();
	private final Map<Plugin, Integer> amountOfBreaks = new HashMap<>();

	private final PublishSubject<Plugin> logoutActionSubject = PublishSubject.create();

	public final PublishSubject<ConfigChanged> configChanged = PublishSubject.create();

	@Inject
	ChinBreakHandler(ConfigManager configManager)
	{
		this.configManager = configManager;
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
	}

	public @NonNull Observable<Map<Plugin, Boolean>> getPluginObservable()
	{
		return pluginsSubject.hide();
	}

	public Set<Plugin> getActivePlugins()
	{
		return activePlugins;
	}

	public void startPlugin(Plugin plugin)
	{
		activePlugins.add(plugin);
		activeSubject.onNext(activePlugins);

		startTimes.put(plugin, Instant.now());
		amountOfBreaks.put(plugin, 0);
	}

	public void stopPlugin(Plugin plugin)
	{
		activePlugins.remove(plugin);
		activeSubject.onNext(activePlugins);

		removePlannedBreak(plugin);
		stopBreak(plugin);

		startTimes.remove(plugin);
		amountOfBreaks.remove(plugin);
	}

	public @NonNull Observable<Set<Plugin>> getActiveObservable()
	{
		return activeSubject.hide();
	}

	public Map<Plugin, Instant> getPlannedBreaks()
	{
		return plannedBreaks;
	}

	public void planBreak(Plugin plugin, Instant instant)
	{
		plannedBreaks.put(plugin, instant);
		plannedBreaksSubject.onNext(plannedBreaks);
	}

	public void removePlannedBreak(Plugin plugin)
	{
		plannedBreaks.remove(plugin);
		plannedBreaksSubject.onNext(plannedBreaks);
	}

	public @NonNull Observable<Map<Plugin, Instant>> getPlannedBreaksObservable()
	{
		return plannedBreaksSubject.hide();
	}

	public boolean isBreakPlanned(Plugin plugin)
	{
		return plannedBreaks.containsKey(plugin);
	}

	public Instant getPlannedBreak(Plugin plugin)
	{
		return plannedBreaks.get(plugin);
	}

	public boolean shouldBreak(Plugin plugin)
	{
		if (!plannedBreaks.containsKey(plugin))
		{
			return false;
		}

		return Instant.now().isAfter(getPlannedBreak(plugin));
	}

	public Map<Plugin, Instant> getActiveBreaks()
	{
		return activeBreaks;
	}

	public void startBreak(Plugin plugin)
	{
		int from = Integer.parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakfrom"));
		int to = Integer.parseInt(configManager.getConfiguration(ChinBreakHandlerPlugin.CONFIG_GROUP, sanitizedName(plugin) + "-breakto"));

		int random = new IntRandomNumberGenerator(from, to).nextInt();

		removePlannedBreak(plugin);

		Instant breakUntil = Instant.now().plus(random, ChronoUnit.MINUTES);

		activeBreaks.put(plugin, Instant.now().plus(random, ChronoUnit.MINUTES));
		activeBreaksSubject.onNext(activeBreaks);

		currentActiveBreaksSubject.onNext(Pair.of(plugin, breakUntil));

		if (amountOfBreaks.containsKey(plugin))
		{
			amountOfBreaks.put(plugin, amountOfBreaks.get(plugin) + 1);
		}
		else
		{
			amountOfBreaks.put(plugin, 1);
		}
	}

	public void startBreak(Plugin plugin, Instant instant)
	{
		removePlannedBreak(plugin);

		activeBreaks.put(plugin, instant);
		activeBreaksSubject.onNext(activeBreaks);

		currentActiveBreaksSubject.onNext(Pair.of(plugin, instant));
	}

	public void stopBreak(Plugin plugin)
	{
		activeBreaks.remove(plugin);
		activeBreaksSubject.onNext(activeBreaks);
	}

	public @NonNull Observable<Map<Plugin, Instant>> getActiveBreaksObservable()
	{
		return activeBreaksSubject.hide();
	}

	public @NonNull Observable<Pair<Plugin, Instant>> getCurrentActiveBreaksObservable()
	{
		return currentActiveBreaksSubject.hide();
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

	public Map<Plugin, Instant> getStartTimes()
	{
		return startTimes;
	}

	public Map<Plugin, Integer> getAmountOfBreaks()
	{
		return amountOfBreaks;
	}
}
