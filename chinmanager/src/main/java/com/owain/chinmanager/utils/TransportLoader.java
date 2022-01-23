package com.owain.chinmanager.utils;

import com.owain.chinmanager.models.MagicMushtree;
import com.owain.chinmanager.models.SpiritTree;
import com.owain.chinmanager.models.Transport;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.WidgetInfo;

public class TransportLoader
{
	private static final int BUILD_DELAY_SECONDS = 5;
	private static Instant lastBuild = Instant.now().minusSeconds(6);
	private static List<Transport> LAST_TRANSPORT_LIST = Collections.emptyList();

	public static final List<SpiritTree> SPIRIT_TREES = List.of(
		new SpiritTree(new WorldPoint(2542, 3170, 0), "Tree gnome Village"),
		new SpiritTree(new WorldPoint(2461, 3444, 0), "Gnome Stronghold"),
		new SpiritTree(new WorldPoint(2555, 3259, 0), "Battlefield of Khazard"),
		new SpiritTree(new WorldPoint(3185, 3508, 0), "Grand Exchange"),
		new SpiritTree(new WorldPoint(2488, 2850, 0), "Feldip Hills")
	);

	public static final List<MagicMushtree> MUSHTREES = List.of(
		new MagicMushtree(new WorldPoint(3676, 3871, 0), WidgetInfo.FOSSIL_MUSHROOM_MEADOW),
		new MagicMushtree(new WorldPoint(3764, 3879, 0), WidgetInfo.FOSSIL_MUSHROOM_HOUSE),
		new MagicMushtree(new WorldPoint(3676, 3755, 0), WidgetInfo.FOSSIL_MUSHROOM_SWAMP),
		new MagicMushtree(new WorldPoint(3760, 3758, 0), WidgetInfo.FOSSIL_MUSHROOM_VALLEY)
	);

	public static List<Transport> buildTransports()
	{
		if (lastBuild.plusSeconds(BUILD_DELAY_SECONDS).isAfter(Instant.now()))
		{
			return List.copyOf(LAST_TRANSPORT_LIST);
		}

		lastBuild = Instant.now();
		List<Transport> transports = new ArrayList<>();
		try
		{
			InputStream txt = TransportLoader.class.getResourceAsStream("/transports.txt");

			if (txt == null)
			{
				throw new IOException("Could not find transports.txt");
			}

			String[] lines = new String(txt.readAllBytes()).split("\n");
			for (String l : lines)
			{
				String line = l.trim();
				if (line.startsWith("#") || line.isEmpty())
				{
					continue;
				}

				transports.add(parseTransportLine(line));
			}

		}
		catch (IOException e)
		{
			// ignore
		}

		transports.add(new Transport(
			new WorldPoint(3267, 3228, 0),
			new WorldPoint(3268, 3228, 0)
		));
		transports.add(new Transport(
			new WorldPoint(3268, 3228, 0),
			new WorldPoint(3267, 3228, 0)
		));
		transports.add(new Transport(
			new WorldPoint(3267, 3227, 0),
			new WorldPoint(3268, 3227, 0)
		));
		transports.add(new Transport(
			new WorldPoint(3268, 3227, 0),
			new WorldPoint(3267, 3227, 0)
		));

		transports.add(new Transport(new WorldPoint(3213, 3221, 0), new WorldPoint(3212, 3221, 0)));
		transports.add(new Transport(new WorldPoint(3212, 3221, 0), new WorldPoint(3213, 3221, 0)));
		transports.add(new Transport(new WorldPoint(3213, 3222, 0), new WorldPoint(3212, 3222, 0)));
		transports.add(new Transport(new WorldPoint(3212, 3222, 0), new WorldPoint(3213, 3222, 0)));
		transports.add(new Transport(new WorldPoint(3207, 3218, 0), new WorldPoint(3207, 3217, 0)));
		transports.add(new Transport(new WorldPoint(3207, 3217, 0), new WorldPoint(3207, 3218, 0)));

		transports.add(new Transport(new WorldPoint(3142, 3513, 0), new WorldPoint(3137, 3516, 0)));
		transports.add(new Transport(new WorldPoint(3137, 3516, 0), new WorldPoint(3142, 3513, 0)));

		// Glarial's tomb
		transports.add(new Transport(new WorldPoint(2557, 3444, 0), new WorldPoint(2555, 9844, 0)));
		transports.add(new Transport(new WorldPoint(2557, 3445, 0), new WorldPoint(2555, 9844, 0)));
		transports.add(new Transport(new WorldPoint(2558, 3443, 0), new WorldPoint(2555, 9844, 0)));
		transports.add(new Transport(new WorldPoint(2559, 3443, 0), new WorldPoint(2555, 9844, 0)));
		transports.add(new Transport(new WorldPoint(2560, 3444, 0), new WorldPoint(2555, 9844, 0)));
		transports.add(new Transport(new WorldPoint(2560, 3445, 0), new WorldPoint(2555, 9844, 0)));
		transports.add(new Transport(new WorldPoint(2558, 3446, 0), new WorldPoint(2555, 9844, 0)));
		transports.add(new Transport(new WorldPoint(2559, 3446, 0), new WorldPoint(2555, 9844, 0)));

		// Waterfall Island
		transports.add(new Transport(new WorldPoint(2512, 3476, 0), new WorldPoint(2513, 3468, 0)));
		transports.add(new Transport(new WorldPoint(2512, 3466, 0), new WorldPoint(2511, 3463, 0)));

		transports.add(new Transport(new WorldPoint(1782, 3458, 0), new WorldPoint(1778, 3417, 0)));

		transports.add(new Transport(new WorldPoint(1779, 3418, 0), new WorldPoint(1784, 3458, 0)));

		transports.add(new Transport(new WorldPoint(3054, 3245, 0), new WorldPoint(1824, 3691, 0)));

		transports.add(new Transport(new WorldPoint(3054, 3245, 0), new WorldPoint(1824, 3695, 1)));

		// Paterdomus
		transports.add(new Transport(new WorldPoint(3405, 3506, 0), new WorldPoint(3405, 9906, 0)));
		transports.add(new Transport(new WorldPoint(3423, 3485, 0), new WorldPoint(3440, 9887, 0)));
		transports.add(new Transport(new WorldPoint(3422, 3484, 0), new WorldPoint(3440, 9887, 0)));

		// Port Piscarilius
		transports.add(new Transport(new WorldPoint(1824, 3691, 0), new WorldPoint(3055, 3242, 1)));

		// Spirit Trees
		for (SpiritTree source : SPIRIT_TREES)
		{
			for (var target : SPIRIT_TREES)
			{
				transports.add(new Transport(source.getPosition(), target.getPosition()));
			}
		}

		// Magic Mushtrees
		for (var source : MUSHTREES)
		{
			for (var target : MUSHTREES)
			{
				transports.add(new Transport(source.getPosition(), target.getPosition()));
			}
		}

		// Gnome stronghold
		transports.add(new Transport(new WorldPoint(2461, 3382, 0), new WorldPoint(2461, 3385, 0)));

		// Tree Gnome Village
		transports.add(new Transport(new WorldPoint(2504, 3192, 0), new WorldPoint(2515, 3159, 0)));
		transports.add(new Transport(new WorldPoint(2515, 3159, 0), new WorldPoint(2504, 3192, 0)));


		// Entrana
		transports.add(new Transport(new WorldPoint(3041, 3237, 0), new WorldPoint(2834, 3331, 1)));
		transports.add(new Transport(new WorldPoint(2834, 3335, 0), new WorldPoint(3048, 3231, 1)));
		transports.add(new Transport(new WorldPoint(2821, 3374, 0), new WorldPoint(2822, 9774, 0)));

		// Fossil Island
		transports.add(new Transport(new WorldPoint(3362, 3445, 0), new WorldPoint(3724, 3808, 0)));

		transports.add(new Transport(new WorldPoint(3724, 3808, 0), new WorldPoint(3362, 3445, 0)));

		return List.copyOf(LAST_TRANSPORT_LIST = transports);
	}

	public static Transport parseTransportLine(String line)
	{
		String[] split = line.split(" ");
		return new Transport(
			new WorldPoint(
				Integer.parseInt(split[0]),
				Integer.parseInt(split[1]),
				Integer.parseInt(split[2])
			),
			new WorldPoint(
				Integer.parseInt(split[3]),
				Integer.parseInt(split[4]),
				Integer.parseInt(split[5])
			)
		);
	}
}
