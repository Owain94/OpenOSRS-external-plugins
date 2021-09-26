package com.owain.chinmanager.utils;

import io.reactivex.rxjava3.annotations.NonNull;
import java.util.PrimitiveIterator;
import java.util.concurrent.ThreadLocalRandom;

public final class IntRandomNumberGenerator
{
	private final PrimitiveIterator.@NonNull OfInt randomIterator;

	/**
	 * Initialize a new random number generator that generates
	 * random numbers in the range [min, max]
	 *
	 * @param min - the min value (inclusive)
	 * @param max - the max value (inclusive)
	 */
	public IntRandomNumberGenerator(int min, int max)
	{
		randomIterator = ThreadLocalRandom.current().ints(min, max + 1).iterator();
	}

	/**
	 * Returns a random number in the range (min, max)
	 *
	 * @return a random number in the range (min, max)
	 */
	public int nextInt()
	{
		return randomIterator.nextInt();
	}
}
