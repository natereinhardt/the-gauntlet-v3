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
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import nr.gauntlet.GauntletToolkitConfig;
import nr.gauntlet.module.Module;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.util.*;

@Slf4j
@Singleton
@Getter
public class MapModule implements Module
{
	private static final Integer CORRUPTED_GAUNTLET_REGION_ID = 7768;
	private static final Integer GAUNTLET_REGION_ID = 7512;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MapPanel panel;

	@Inject
	private GauntletToolkitConfig config;

	@Inject
	private MapSession session;

	@Inject
	private MapOverlay mapOverlay;

	@Inject
	private DemiBossOverlay demiBossOverlay;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	private Map<Integer, String> fileNameMap;
	private Map<Integer, List<Integer>> connectedRoomsMap;
	private NavigationButton navButton;
	private boolean isPanelDisplayed;

	@Override
	public void start()
	{
		eventBus.register(this);
		
		createStartingMaps();
		
		BufferedImage icon = ImageUtil.loadImageResource(MapModule.class, "icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Gauntlet Map")
			.icon(icon)
			.priority(99)
			.panel(panel)
			.build();

		updatePanelDisplay();

		overlayManager.add(mapOverlay);
		overlayManager.add(demiBossOverlay);
		
		log.info("Gauntlet Map module started");
	}

	@Override
	public void stop()
	{
		eventBus.unregister(this);
		clientToolbar.removeNavigation(navButton);
		overlayManager.remove(demiBossOverlay);
		overlayManager.remove(mapOverlay);
		log.info("Gauntlet Map module stopped");
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (!config.mapEnabled() || !client.isInInstancedRegion() || isNotInGauntlet())
		{
			return;
		}

		WorldPoint player = client.getLocalPlayer().getWorldLocation();

		switch (npcSpawned.getNpc().getId())
		{
			case NpcID.CORRUPTED_HUNLLEF:
				session.setCorrupted(true);
			case NpcID.CRYSTALLINE_HUNLLEF:
				session.hunllefSpawned(player, npcSpawned.getActor().getWorldLocation());
				break;

			case NpcID.CORRUPTED_BEAR:
			case NpcID.CRYSTALLINE_BEAR:
			case NpcID.CORRUPTED_DARK_BEAST:
			case NpcID.CRYSTALLINE_DARK_BEAST:
			case NpcID.CORRUPTED_DRAGON:
			case NpcID.CRYSTALLINE_DRAGON:
				session.updateDemiBossLocations(player, npcSpawned.getNpc());
				break;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (!config.mapEnabled() || !client.isInInstancedRegion() || isNotInGauntlet())
		{
			return;
		}

		WorldPoint player = client.getLocalPlayer().getWorldLocation();

		switch (npcDespawned.getNpc().getId())
		{
			case NpcID.CORRUPTED_BEAR:
			case NpcID.CRYSTALLINE_BEAR:
			case NpcID.CORRUPTED_DARK_BEAST:
			case NpcID.CRYSTALLINE_DARK_BEAST:
			case NpcID.CORRUPTED_DRAGON:
			case NpcID.CRYSTALLINE_DRAGON:
				session.updateDemiBossLocations(player, npcDespawned.getNpc());
				break;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (!config.mapEnabled())
		{
			return;
		}
		
		session.gameStateChanged(gameStateChanged, client);
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (!config.mapEnabled())
		{
			return;
		}

		updatePanelDisplay();

		if (!client.isInInstancedRegion() || isNotInGauntlet())
		{
			return;
		}

		if (session.isNewSession())
		{
			return;
		}

		session.updateCurrentRoom(client.getLocalPlayer().getWorldLocation());
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		if (!config.mapEnabled() || !client.isInInstancedRegion() || isNotInGauntlet())
		{
			return;
		}

		session.gameObjectSpawned(gameObjectSpawned.getGameObject(), client);
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		if (!config.mapEnabled() || !client.isInInstancedRegion() || isNotInGauntlet())
		{
			return;
		}

		session.gameObjectDespawned(gameObjectDespawned.getGameObject());
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (!config.mapEnabled())
		{
			return;
		}

		if (event.getVarbitId() == VarbitID.GAUNTLET_BOSS_STARTED && event.getValue() == 1)
		{
			session.stop();
		}
	}

	private void createStartingMaps()
	{
		Map<Integer, String> fileNameMap = new TreeMap<>();
		Map<Integer, List<Integer>> connectedRoomsMap = new TreeMap<>();

		for (int room = 1; room <= 49; room++)
		{
			List<Integer> connectedRoomsList = new ArrayList<>();

			switch (room)
			{
				case 1:
					connectedRoomsList.addAll(Arrays.asList(2, 8));
					fileNameMap.put(room, "_top_left.png");
					break;
				case 7:
					connectedRoomsList.addAll(Arrays.asList(6, 14));
					fileNameMap.put(room, "_top_right.png");
					break;
				case 43:
					connectedRoomsList.addAll(Arrays.asList(36, 44));
					fileNameMap.put(room, "_bottom_left.png");
					break;
				case 49:
					connectedRoomsList.addAll(Arrays.asList(42, 48));
					fileNameMap.put(room, "_bottom_right.png");
					break;
				case 8:
				case 15:
				case 22:
				case 29:
				case 36:
					connectedRoomsList.addAll(Arrays.asList(room - 7, room + 1, room + 7));
					fileNameMap.put(room, "_left.png");
					break;
				case 14:
				case 21:
				case 28:
				case 35:
				case 42:
					connectedRoomsList.addAll(Arrays.asList(room - 7, room - 1, room + 7));
					fileNameMap.put(room, "_right.png");
					break;
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
					connectedRoomsList.addAll(Arrays.asList(room - 1, room + 1, room + 7));
					fileNameMap.put(room, "_top.png");
					break;
				case 44:
				case 45:
				case 46:
				case 47:
				case 48:
					connectedRoomsList.addAll(Arrays.asList(room - 7, room - 1, room + 1));
					fileNameMap.put(room, "_bottom.png");
					break;
				default:
					connectedRoomsList.addAll(Arrays.asList(room - 7, room - 1, room + 1, room + 7));
					fileNameMap.put(room, ".png");
					break;
			}
			connectedRoomsMap.put(room, connectedRoomsList);
		}
		this.fileNameMap = fileNameMap;
		this.connectedRoomsMap = connectedRoomsMap;
	}

	private boolean isNotInGauntlet()
	{
		int[] regions = client.getMapRegions();
		if (regions == null)
		{
			return true;
		}
		
		for (int region : regions)
		{
			if (region == CORRUPTED_GAUNTLET_REGION_ID || region == GAUNTLET_REGION_ID)
			{
				return false;
			}
		}
		return true;
	}

	private boolean shouldDisplayPanel()
	{
		switch (config.mapPanelVisibility())
		{
			case NEVER:
				return false;
			case IN_GAUNTLET:
				return !isNotInGauntlet();
			case ALWAYS:
			default:
				return true;
		}
	}

	private void updatePanelDisplay()
	{
		boolean shouldDisplayPanel = shouldDisplayPanel();
		if (shouldDisplayPanel == isPanelDisplayed)
		{
			return;
		}

		if (shouldDisplayPanel)
		{
			isPanelDisplayed = true;
			clientToolbar.addNavigation(navButton);
		}
		else
		{
			isPanelDisplayed = false;
			clientToolbar.removeNavigation(navButton);
		}
	}
}
