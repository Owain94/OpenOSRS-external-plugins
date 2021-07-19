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

import java.nio.ByteBuffer;
import java.util.BitSet;

public class FlagMap
{
	public static final int PLANE_COUNT = 4;
	protected final BitSet flags;
	public final int minX;
	public final int minY;
	public final int maxX;
	public final int maxY;
	private final int width;
	private final int height;
	private final int flagCount;

	public FlagMap(int minX, int minY, int maxX, int maxY, int flagCount)
	{
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		this.flagCount = flagCount;
		width = (maxX - minX + 1);
		height = (maxY - minY + 1);
		flags = new BitSet(width * height * PLANE_COUNT * flagCount);
	}

	public FlagMap(byte[] bytes, int flagCount)
	{
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		minX = buffer.getInt();
		minY = buffer.getInt();
		maxX = buffer.getInt();
		maxY = buffer.getInt();
		this.flagCount = flagCount;
		width = (maxX - minX + 1);
		height = (maxY - minY + 1);
		flags = BitSet.valueOf(buffer);
	}

	public byte[] toBytes()
	{
		byte[] bytes = new byte[16 + flags.size()];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.putInt(minX);
		buffer.putInt(minY);
		buffer.putInt(maxX);
		buffer.putInt(maxY);
		buffer.put(flags.toByteArray());
		return bytes;
	}

	public boolean get(int x, int y, int z, int flag)
	{
		if (x < minX || x > maxX || y < minY || y > maxY || z < 0 || z > PLANE_COUNT - 1)
		{
			return false;
		}

		return flags.get(index(x, y, z, flag));
	}

	public void set(int x, int y, int z, int flag, boolean value)
	{
		flags.set(index(x, y, z, flag), value);
	}

	private int index(int x, int y, int z, int flag)
	{
		if (x < minX || x > maxX || y < minY || y > maxY || z < 0 || z > PLANE_COUNT - 1 || flag < 0 || flag > flagCount - 1)
		{
			throw new IndexOutOfBoundsException(x + " " + y + " " + z);
		}

		return (z * width * height + (y - minY) * width + (x - minX)) * flagCount + flag;
	}
}
