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

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import nr.gauntlet.GauntletToolkitConfig;

import javax.inject.Inject;
import java.awt.*;

public class DemiBossOverlay extends OverlayPanel
{
	private final GauntletToolkitConfig config;
	private final MapSession session;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	private DemiBossOverlay(MapSession session, GauntletToolkitConfig config, ModelOutlineRenderer modelOutlineRenderer)
	{
		this.session = session;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.mapShowDemiBosses())
		{
			return null;
		}

		highlightDemiBoss(graphics);

		return super.render(graphics);
	}

	private void highlightDemiBoss(Graphics2D graphics)
	{
		if (session.getHighlightNodeMap() == null)
		{
			return;
		}

		session.getHighlightNodeMap().forEach((room, nodeObjectList) ->
		{
			nodeObjectList.forEach(nodeGameObject ->
			{
				modelOutlineRenderer.drawOutline(nodeGameObject, config.mapDemiBossOutlineSize(), config.mapDemiBossOutlineColor(), 1);
			});
		});
	}
}
