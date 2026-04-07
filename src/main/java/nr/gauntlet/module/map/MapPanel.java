/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2023, Tyler (StickySerum) (Original Gauntlet Map plugin)
 * Copyright (c) 2026, Integration and modifications for The Gauntlet
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

package nr.gauntlet.module.map;

import lombok.Getter;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

@Getter
public class MapPanel extends PluginPanel
{
	private final MapModule module;
	private final MapSession session;

	@Inject
	MapPanel(MapModule module, MapSession session)
	{
		this.module = module;
		this.session = session;
	}

	public void clearPanel()
	{
		removeAll();
		revalidate();
		repaint();
	}

	public void firstLoad()
	{
		SwingUtilities.invokeLater(() ->
		{
			removeAll();
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;

			for (int y = 0; y <= 6; y++)
			{
				for (int x = 0; x <= 6; x++)
				{
					c.gridx = x;
					c.gridy = y;
					int room = (y * 7 + x + 1);

					add(new JLabel(new ImageIcon(session.getGauntletMap().get(room))), c);
				}
			}

			revalidate();
			repaint();
		});
	}

	public void updatePanel(Integer room)
	{
		SwingUtilities.invokeLater(() ->
		{
			GridBagLayout layout = (GridBagLayout) getLayout();
			Component toRemove = getComponent(room - 1);
			GridBagConstraints c = layout.getConstraints(toRemove);
			remove(toRemove);

			add(new JLabel(new ImageIcon(session.getGauntletMap().get(room))), c, room - 1);

			revalidate();
			repaint();
		});
	}
}
