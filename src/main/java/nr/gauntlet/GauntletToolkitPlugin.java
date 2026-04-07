/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2023, rdutta <https://github.com/rdutta>
 * Copyright (c) 2019, kThisIsCvpv <https://github.com/kThisIsCvpv>
 * Copyright (c) 2019, ganom <https://github.com/Ganom>
 * Copyright (c) 2019, kyle <https://github.com/Kyleeld>
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

package nr.gauntlet;

import nr.gauntlet.module.boss.BossModule;
import nr.gauntlet.module.history.HistoryPanel;
import nr.gauntlet.module.history.RunHistoryManager;
import nr.gauntlet.module.map.MapModule;
import nr.gauntlet.module.maze.MazeModule;
import nr.gauntlet.module.overlay.PerformanceStatsOverlay;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "The Gauntlet",
	description = "Comprehensive all-in-one plugin for The Gauntlet with map, stats, and overlays",
	tags = {"the", "gauntlet", "toolkit"}
)
public final class GauntletToolkitPlugin extends Plugin
{
	private static final int VARBIT_MAZE = 9178;
	private static final int VARBIT_BOSS = 9177;
	private static final BufferedImage ICON;

	static
	{
		BufferedImage icon = null;
		try
		{
			icon = ImageUtil.loadImageResource(GauntletToolkitPlugin.class, "icon.png");
		}
		catch (Exception e)
		{
			// Icon not found, will use null
		}
		ICON = icon;
	}

	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private MazeModule mazeModule;
	@Inject
	private BossModule bossModule;
	@Inject
	private MapModule mapModule;
	@Inject
	private RunHistoryManager historyManager;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private GauntletToolkitConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private PerformanceStatsOverlay performanceStatsOverlay;

	private HistoryPanel historyPanel;
	private NavigationButton navButton;

	@Provides
	GauntletToolkitConfig provideConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(GauntletToolkitConfig.class);
	}

	@Override
	protected void startUp()
	{
		// Add performance overlay
		overlayManager.add(performanceStatsOverlay);

		// Initialize history panel
		if (config.showHistoryPanel())
		{
			initializeHistoryPanel();
		}

		// Start map module if enabled
		if (config.mapEnabled())
		{
			mapModule.start();
		}

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		clientThread.invoke(() -> {
			if (client.getVarbitValue(VARBIT_BOSS) == 1)
			{
				bossModule.start();
			}
			else if (client.getVarbitValue(VARBIT_MAZE) == 1)
			{
				mazeModule.start();
			}
		});
	}

	@Override
	protected void shutDown()
	{
		mazeModule.stop();
		bossModule.stop();
		
		if (mapModule != null)
		{
			mapModule.stop();
		}
		
		overlayManager.remove(performanceStatsOverlay);
		removeHistoryPanel();
	}

	private void initializeHistoryPanel()
	{
		if (historyPanel != null)
		{
			return;
		}

		historyPanel = new HistoryPanel(historyManager);

		navButton = NavigationButton.builder()
			.tooltip("Gauntlet History")
			.icon(ICON)
			.priority(8)
			.panel(historyPanel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	private void removeHistoryPanel()
	{
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}
		historyPanel = null;
	}

	@Subscribe
	public void onConfigChanged(final ConfigChanged event)
	{
		if (!event.getGroup().equals(GauntletToolkitConfig.CONFIG_GROUP))
		{
			return;
		}

		if (event.getKey().equals("showHistoryPanel"))
		{
			if (config.showHistoryPanel())
			{
				initializeHistoryPanel();
			}
			else
			{
				removeHistoryPanel();
			}
		}
		else if (event.getKey().equals("mapEnabled"))
		{
			if (config.mapEnabled())
			{
				mapModule.start();
			}
			else
			{
				mapModule.stop();
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event)
	{
		final int varbit = event.getVarbitId();

		if (varbit == VARBIT_MAZE)
		{
			if (event.getValue() == 1)
			{
				mazeModule.start();
			}
			else
			{
				mazeModule.stop();
			}
		}
		else if (varbit == VARBIT_BOSS)
		{
			if (event.getValue() == 1)
			{
				mazeModule.stop();
				bossModule.start();
			}
			else
			{
				bossModule.stop();
			}
		}
	}
}
