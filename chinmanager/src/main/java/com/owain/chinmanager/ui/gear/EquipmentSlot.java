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

import io.reactivex.rxjava3.annotations.NonNull;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import org.jetbrains.annotations.Nullable;

public class EquipmentSlot extends JPanel
{
	@Getter(AccessLevel.PACKAGE)
	private final @NonNull JLabel imageLabel;
	@Getter(AccessLevel.PACKAGE)
	private final int indexInSlot;
	@Nullable
	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private Equipment parentSetup;

	@Setter(AccessLevel.PACKAGE)
	private boolean locked;

	public EquipmentSlot(int indexInSlot)
	{
		this.imageLabel = new JLabel();
		this.parentSetup = null;
		this.indexInSlot = indexInSlot;
		imageLabel.setVerticalAlignment(SwingConstants.CENTER);
		imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(42, 42));
		add(imageLabel, BorderLayout.CENTER);
	}

	@Override
	protected void paintComponent(@NonNull Graphics g)
	{

		super.paintComponent(g);

		BufferedImage bg;

		if (locked)
		{
			bg = ImageUtil.loadImageResource(EquipmentSlot.class, "lock.png");
		}
		else if (imageLabel.getIcon() != null)
		{
			bg = ImageUtil.loadImageResource(EquipmentSlot.class, "empty.png");
		}
		else
		{
			switch (indexInSlot)
			{
				case 0:
					bg = ImageUtil.loadImageResource(EquipmentSlot.class, "head.png");
					break;
				case 1:
					bg = ImageUtil.loadImageResource(EquipmentSlot.class, "cape.png");
					break;
				case 2:
					bg = ImageUtil.loadImageResource(EquipmentSlot.class, "amulet.png");
					break;
				case 3:
					bg = ImageUtil.loadImageResource(EquipmentSlot.class, "weapon.png");
					break;
				case 4:
					bg = ImageUtil.loadImageResource(EquipmentSlot.class, "body.png");
					break;
				case 5:
					bg = ImageUtil.loadImageResource(EquipmentSlot.class, "shield.png");
					break;
				case 7:
					bg = ImageUtil.loadImageResource(EquipmentSlot.class, "legs.png");
					break;
				case 9:
					bg = ImageUtil.loadImageResource(EquipmentSlot.class, "gloves.png");
					break;
				case 10:
					bg = ImageUtil.loadImageResource(EquipmentSlot.class, "boots.png");
					break;
				case 12:
					bg = ImageUtil.loadImageResource(EquipmentSlot.class, "ring.png");
					break;
				case 13:
					bg = ImageUtil.loadImageResource(EquipmentSlot.class, "ammo.png");
					break;
				default:
					bg = ImageUtil.loadImageResource(EquipmentSlot.class, "empty.png");
			}
		}

		if (indexInSlot != -1)
		{
			Image tmp = bg.getScaledInstance(this.getPreferredSize().width, this.getPreferredSize().height, Image.SCALE_SMOOTH);

			g.drawImage(tmp, Math.round((this.getWidth() - this.getPreferredSize().width) / 2), Math.round((this.getHeight() - this.getPreferredSize().height) / 2), null);
		}
	}

	public @NonNull Dimension getPreferredSize()
	{
		return new Dimension(42, 42);
	}

	public void setImageLabel(@Nullable String toolTip, @Nullable AsyncBufferedImage itemImage)
	{
		if (itemImage == null || toolTip == null || locked)
		{
			imageLabel.setToolTipText(locked && toolTip != null ? toolTip : "");
			imageLabel.setIcon(null);
			imageLabel.revalidate();
			return;
		}

		imageLabel.setToolTipText(toolTip);
		itemImage.addTo(imageLabel);

		validate();
		repaint();
	}
}
