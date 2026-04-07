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

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import nr.gauntlet.GauntletToolkitConfig;

public class MapOverlay extends OverlayPanel
{
	private final MapSession session;
	private final GauntletToolkitConfig config;

	@Inject
	private MapOverlay(MapSession session, GauntletToolkitConfig config, Client client, ModelOutlineRenderer modelOutlineRenderer)
	{
		this.session = session;
		this.config = config;

		setPosition(OverlayPosition.TOP_CENTER);
		setLayer(OverlayLayer.ABOVE_SCENE);

		panelComponent.setWrap(true);
		panelComponent.setOrientation(ComponentOrientation.HORIZONTAL);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (session.isNewSession() || !config.mapShowOverlay())
		{
			return null;
		}

		int size = config.mapOverlayTileSize() * 7;
		panelComponent.setPreferredSize(new Dimension(size, size));

		for (int i = 1; i <= 49; i++)
		{
			panelComponent.getChildren().add(new ImageComponent(session.scaleImage(config.mapOverlayTileSize(), session.getGauntletMap().get(i))));
		}

		float opacity = (float) config.mapOverlayOpacity() / 100;
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		return super.render(graphics);
	}
}
