package com.owain.chinmanager.ui.utils;

import io.reactivex.rxjava3.annotations.NonNull;
import java.awt.Container;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JComponent;

public class Components
{
	public static <T extends JComponent> List<T> findComponents(
		final @NonNull Container container,
		final @NonNull Class<T> componentType
	)
	{
		return Stream.concat(
			Arrays.stream(container.getComponents())
				.filter(componentType::isInstance)
				.map(componentType::cast),
			Arrays.stream(container.getComponents())
				.filter(Container.class::isInstance)
				.map(Container.class::cast)
				.flatMap(c -> findComponents(c, componentType).stream())
		).collect(Collectors.toList());
	}
}
