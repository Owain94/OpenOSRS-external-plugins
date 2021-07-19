/*
 * Copyright (c) 2020, Runemoro <https://github.com/TheStonedTurtle>
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
package com.owain.automation.pathfinding;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

public abstract class SplitFlagMap
{
	private static final int MAXIMUM_SIZE = 20 * 1024 * 1024;
	private final int regionSize;
	private final LoadingCache<Position, FlagMap> regionMaps;
	private final int flagCount;

	public static byte[] readAllBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];

		while (true)
		{
			int read = in.read(buffer, 0, buffer.length);

			if (read == -1)
			{
				return result.toByteArray();
			}

			result.write(buffer, 0, read);
		}
	}

	public SplitFlagMap(int regionSize, Map<Position, byte[]> compressedRegions, int flagCount)
	{
		this.regionSize = regionSize;
		this.flagCount = flagCount;
		regionMaps = CacheBuilder
			.newBuilder()
			.weigher((Weigher<Position, FlagMap>) (k, v) -> v.flags.size() / 8)
			.maximumWeight(MAXIMUM_SIZE)
			.build(CacheLoader.from(position -> {
				byte[] compressedRegion = compressedRegions.get(position);

				if (compressedRegion == null)
				{
					return new FlagMap(position.x * regionSize, position.y * regionSize, (position.x + 1) * regionSize - 1, (position.y + 1) * regionSize - 1, this.flagCount);
				}

				try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(compressedRegion)))
				{
					return new FlagMap(readAllBytes(in), this.flagCount);
				}
				catch (IOException e)
				{
					throw new UncheckedIOException(e);
				}
			}));
	}

	public boolean get(int x, int y, int z, int flag)
	{
		try
		{
			return regionMaps.get(new Position(x / regionSize, y / regionSize)).get(x, y, z, flag);
		}
		catch (ExecutionException e)
		{
			throw new UncheckedExecutionException(e);
		}
	}

	public static class Position
	{
		public final int x;
		public final int y;

		public Position(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof Position &&
				((Position) o).x == x &&
				((Position) o).y == y;
		}

		@Override
		public int hashCode()
		{
			return x * 31 + y;
		}

		@Override
		public String toString()
		{
			return "(" + x + ", " + y + ")";
		}
	}
}
