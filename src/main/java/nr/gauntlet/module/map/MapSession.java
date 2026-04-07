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

import com.google.common.collect.Lists;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.util.ImageUtil;
import nr.gauntlet.GauntletToolkitConfig;

@Singleton
public class MapSession
{
	private final MapModule module;
	private final GauntletToolkitConfig config;

	private static final int TILE_DISTANCE = 16;
	private static final int BOSS_ROOM = 25;

	public static final List<Integer> DEMI_ROOM_LIST = List.of(3, 4, 5, 15, 21, 22, 28, 29, 35, 45, 46, 47);

	private enum MapIcons
	{
		PLAYER,
		BOSS,
		ACTIVE_TILE,
		DEMIBOSS_UNKNOWN,
		DEMIBOSS_MAGIC,
		DEMIBOSS_MELEE,
		DEMIBOSS_RANGED,
		FISHING_SPOT,
		GRYM_ROOT,
		FISHING_SPOT_GRYM_ROOT
	}

	private Map<Integer, MapIcons> demiBossLocationsMap = new TreeMap<>();

	@Getter
	private Map<Integer, List<GameObject>> highlightNodeMap = new TreeMap<>();

	private Map<Integer, List<WorldPoint>> demiBossNodeLocationsMap = new TreeMap<>();
	private Map<Integer, List<Integer>> roomResourcesMap = new TreeMap<>();

	@Getter
	private Map<Integer, BufferedImage> gauntletMap;
	
	private Map<Integer, WorldPoint> centerTileMap;
	private Map<Integer, List<WorldPoint>> roomTilesMap;
	
	private Integer startLocation;
	private Integer currentRoom;

	@Setter
	private boolean corrupted = false;

	@Getter
	private boolean newSession = true;

	@Inject
	MapSession(MapModule module, GauntletToolkitConfig config)
	{
		this.module = module;
		this.config = config;
	}

	public void stop()
	{
		newSession = true;
		corrupted = false;
		currentRoom = null;
		highlightNodeMap.clear();
		demiBossLocationsMap.clear();
		roomResourcesMap.clear();
		module.getPanel().clearPanel();
	}

	public void createInstanceMaps(WorldPoint playerLocation)
	{
		WorldPoint northWestCornerRoom = null;
		Map<Integer, WorldPoint> centerTileMap = new TreeMap<>();
		Map<Integer, List<WorldPoint>> roomTilesMap = new TreeMap<>();
		Map<Integer, BufferedImage> gauntletMap = new TreeMap<>();

		switch (startLocation)
		{
			case 18: // North start
				northWestCornerRoom = calculateNewPoint(playerLocation, -3, 2, -3, -2);
				break;
			case 26: // East start
				northWestCornerRoom = calculateNewPoint(playerLocation, -4, 3, -3, -2);
				break;
			case 32: // South start
				northWestCornerRoom = calculateNewPoint(playerLocation, -3, 4, -3, -2);
				break;
			case 24: // West start
				northWestCornerRoom = calculateNewPoint(playerLocation, -2, 3, -3, -2);
				break;
		}

		for (int gauntletMapY = 0; gauntletMapY <= 6; gauntletMapY++)
		{
			for (int gauntletMapX = 0; gauntletMapX <= 6; gauntletMapX++)
			{
				WorldPoint centerTile = calculateNewPoint(northWestCornerRoom, gauntletMapX, -gauntletMapY, 0, 0);
				WorldPoint northWestCornerTile = calculateNewPoint(centerTile, 0, 0, -6, 5);
				List<WorldPoint> roomTiles = new ArrayList<>();
				int room = (gauntletMapY * 7 + gauntletMapX + 1);

				String path = "inactive" + module.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(MapModule.class, path));

				if (!module.getConnectedRoomsMap().get(room).contains(startLocation))
				{
					module.getConnectedRoomsMap().get(room).add(startLocation);
				}

				for (int roomY = 0; roomY <= 11; roomY++)
				{
					for (int roomX = 0; roomX <= 11; roomX++)
					{
						roomTiles.add(calculateNewPoint(northWestCornerTile, 0, 0, roomX, -roomY));
					}
				}

				for (int roomEntranceY = 0; roomEntranceY <= 1; roomEntranceY++)
				{
					for (int roomEntranceX = 0; roomEntranceX <= 1; roomEntranceX++)
					{
						roomTiles.add(calculateNewPoint(centerTile, 0, 0, -roomEntranceX, 7 - roomEntranceY));
						roomTiles.add(calculateNewPoint(centerTile, 0, 0, 7 - roomEntranceX, -roomEntranceY));
						roomTiles.add(calculateNewPoint(centerTile, 0, 0, -roomEntranceX, -7 - roomEntranceY));
						roomTiles.add(calculateNewPoint(centerTile, 0, 0, -7 - roomEntranceX, -roomEntranceY));
					}
				}

				for (Integer connectedRoom : module.getConnectedRoomsMap().get(room))
				{
					if (DEMI_ROOM_LIST.contains(connectedRoom))
					{
						switch (connectedRoom - room)
						{
							case -7: // North
								if (demiBossNodeLocationsMap.containsKey(room))
								{
									demiBossNodeLocationsMap.get(room).addAll(Lists.newArrayList(
										calculateNewPoint(centerTile, 0, 0, -2, 7),
										calculateNewPoint(centerTile, 0, 0, -2, 6),
										calculateNewPoint(centerTile, 0, 0, 1, 7),
										calculateNewPoint(centerTile, 0, 0, 1, 6)
									));
								}
								else
								{
									demiBossNodeLocationsMap.put(room, Lists.newArrayList(
										calculateNewPoint(centerTile, 0, 0, -2, 7),
										calculateNewPoint(centerTile, 0, 0, -2, 6),
										calculateNewPoint(centerTile, 0, 0, 1, 7),
										calculateNewPoint(centerTile, 0, 0, 1, 6)
									));
								}
								break;

							case 7: // South
								if (demiBossNodeLocationsMap.containsKey(room))
								{
									demiBossNodeLocationsMap.get(room).addAll(Lists.newArrayList(
										calculateNewPoint(centerTile, 0, 0, -2, -7),
										calculateNewPoint(centerTile, 0, 0, -2, -8),
										calculateNewPoint(centerTile, 0, 0, 1, -7),
										calculateNewPoint(centerTile, 0, 0, 1, -8)
									));
								}
								else
								{
									demiBossNodeLocationsMap.put(room, Lists.newArrayList(
										calculateNewPoint(centerTile, 0, 0, -2, -7),
										calculateNewPoint(centerTile, 0, 0, -2, -8),
										calculateNewPoint(centerTile, 0, 0, 1, -7),
										calculateNewPoint(centerTile, 0, 0, 1, -8)
									));
								}
								break;

							case 1: // East
								if (demiBossNodeLocationsMap.containsKey(room))
								{
									demiBossNodeLocationsMap.get(room).addAll(Lists.newArrayList(
										calculateNewPoint(centerTile, 0, 0, 7, 1),
										calculateNewPoint(centerTile, 0, 0, 6, 1),
										calculateNewPoint(centerTile, 0, 0, 7, -2),
										calculateNewPoint(centerTile, 0, 0, 6, -2)
									));
								}
								else
								{
									demiBossNodeLocationsMap.put(room, Lists.newArrayList(
										calculateNewPoint(centerTile, 0, 0, 7, 1),
										calculateNewPoint(centerTile, 0, 0, 6, 1),
										calculateNewPoint(centerTile, 0, 0, 7, -2),
										calculateNewPoint(centerTile, 0, 0, 6, -2)
									));
								}
								break;

							case -1: // West
								if (demiBossNodeLocationsMap.containsKey(room))
								{
									demiBossNodeLocationsMap.get(room).addAll(Lists.newArrayList(
										calculateNewPoint(centerTile, 0, 0, -7, 1),
										calculateNewPoint(centerTile, 0, 0, -8, 1),
										calculateNewPoint(centerTile, 0, 0, -7, -2),
										calculateNewPoint(centerTile, 0, 0, -8, -2)
									));
								}
								else
								{
									demiBossNodeLocationsMap.put(room, Lists.newArrayList(
										calculateNewPoint(centerTile, 0, 0, -7, 1),
										calculateNewPoint(centerTile, 0, 0, -8, 1),
										calculateNewPoint(centerTile, 0, 0, -7, -2),
										calculateNewPoint(centerTile, 0, 0, -8, -2)
									));
								}
								break;
						}
					}
				}

				centerTileMap.put(room, centerTile);
				roomTilesMap.put(room, roomTiles);
			}
		}

		this.gauntletMap = gauntletMap;
		this.centerTileMap = centerTileMap;
		this.roomTilesMap = roomTilesMap;
	}

	private WorldPoint calculateNewPoint(WorldPoint startPoint, Integer roomsX, Integer roomsY, Integer tilesX, Integer tilesY)
	{
		return new WorldPoint(
			startPoint.getX() + (roomsX * TILE_DISTANCE) + tilesX,
			startPoint.getY() + (roomsY * TILE_DISTANCE) + tilesY,
			startPoint.getPlane()
		);
	}

	public Integer calculateActivatedRoom(WorldPoint player, WorldPoint target)
	{
		int difference = 0;

		if (Math.abs(player.getY() - target.getY()) > Math.abs(player.getX() - target.getX()))
		{
			if (player.getY() > target.getY())
			{
				difference = -7; // North
			}
			else
			{
				difference = 7; // South
			}
		}
		else
		{
			if (player.getX() > target.getX())
			{
				difference = 1; // East
			}
			else
			{
				difference = -1; // West
			}
		}

		if (currentRoom == null)
		{
			currentRoom = 25;
		}

		return (currentRoom + difference);
	}

	public void updateCurrentRoom(WorldPoint playerLocation)
	{
		if (roomTilesMap.get(currentRoom).contains(playerLocation))
		{
			return;
		}

		module.getConnectedRoomsMap().get(currentRoom).forEach(connectedRoom ->
		{
			if (roomTilesMap.get(connectedRoom).contains(playerLocation))
			{
				int previousRoom = currentRoom;
				currentRoom = connectedRoom;

				if (demiBossLocationsMap.containsKey(previousRoom))
				{
					updateGauntletMap(previousRoom, demiBossLocationsMap.get(previousRoom));
				}
				else if (roomResourcesMap.containsKey(previousRoom))
				{
					updateRoomResources(previousRoom);
				}
				else
				{
					updateGauntletMap(previousRoom, MapIcons.ACTIVE_TILE);
				}

				updateGauntletMap(currentRoom, MapIcons.PLAYER);
			}
		});
	}

	public void hunllefSpawned(WorldPoint player, WorldPoint hunllef)
	{
		startLocation = calculateActivatedRoom(player, hunllef);
		currentRoom = startLocation;
		createInstanceMaps(player);

		updateGauntletMap(currentRoom, MapIcons.PLAYER);
		updateGauntletMap(BOSS_ROOM, MapIcons.BOSS);

		if (this.config.mapShowDemiBosses())
		{
			for (int i = 0; i <= DEMI_ROOM_LIST.size() - 1; i++)
			{
				updateGauntletMap(DEMI_ROOM_LIST.get(i), MapIcons.DEMIBOSS_UNKNOWN);
			}
		}

		module.getPanel().firstLoad();
		newSession = false;
	}

	public void updateDemiBossLocations(WorldPoint player, NPC demiBoss)
	{
		int room = calculateActivatedRoom(player, centerTileMap.get(currentRoom));

		if (demiBoss.isDead())
		{
			for (Map.Entry<Integer, MapIcons> entry : demiBossLocationsMap.entrySet())
			{
				if (roomTilesMap.get(entry.getKey()).contains(demiBoss.getWorldLocation()))
				{
					room = entry.getKey();
				}
			}

			demiBossLocationsMap.remove(room);

			if (!roomTilesMap.get(room).contains(player))
			{
				updateGauntletMap(room, MapIcons.ACTIVE_TILE);
			}

			return;
		}

		switch (demiBoss.getId())
		{
			case NpcID.CRYSTALLINE_BEAR:
			case NpcID.CORRUPTED_BEAR:
				demiBossLocationsMap.put(room, MapIcons.DEMIBOSS_MELEE);
				updateGauntletMap(room, MapIcons.DEMIBOSS_MELEE);
				break;

			case NpcID.CRYSTALLINE_DRAGON:
			case NpcID.CORRUPTED_DRAGON:
				demiBossLocationsMap.put(room, MapIcons.DEMIBOSS_MAGIC);
				updateGauntletMap(room, MapIcons.DEMIBOSS_MAGIC);
				break;

			case NpcID.CRYSTALLINE_DARK_BEAST:
			case NpcID.CORRUPTED_DARK_BEAST:
				demiBossLocationsMap.put(room, MapIcons.DEMIBOSS_RANGED);
				updateGauntletMap(room, MapIcons.DEMIBOSS_RANGED);
				break;
		}
	}

	public void gameObjectSpawned(GameObject gameObject, Client client)
	{
		WorldPoint player = client.getLocalPlayer().getWorldLocation();
		int room = calculateActivatedRoom(player, centerTileMap.get(currentRoom));

		switch (gameObject.getId())
		{
			case ObjectID.FISHING_SPOT_36068:
			case ObjectID.CORRUPT_FISHING_SPOT:
			case ObjectID.GRYM_ROOT:
			case ObjectID.CORRUPT_GRYM_ROOT:
				if (roomTilesMap.get(room).contains(gameObject.getWorldLocation()))
				{
					if (roomResourcesMap.containsKey(room))
					{
						roomResourcesMap.get(room).add(gameObject.getId());
					}
					else
					{
						roomResourcesMap.put(room, Lists.newArrayList(gameObject.getId()));
					}
					updateRoomResources(room);
				}
				break;

			case ObjectID.NODE_35998:
			case ObjectID.NODE_35999:
			case ObjectID.NODE_36101:
			case ObjectID.NODE_36102:
				demiBossNodeLocationsMap.forEach((roomKey, worldPoints) ->
				{
					if (worldPoints.contains(gameObject.getWorldLocation()))
					{
						if (highlightNodeMap.containsKey(roomKey))
						{
							highlightNodeMap.get(roomKey).add(gameObject);
						}
						else
						{
							highlightNodeMap.put(roomKey, Lists.newArrayList(gameObject));
						}
					}
				});
				break;
		}
	}

	public void gameObjectDespawned(GameObject gameObject)
	{
		switch (gameObject.getId())
		{
			case ObjectID.FISHING_SPOT_36068:
			case ObjectID.CORRUPT_FISHING_SPOT:
			case ObjectID.GRYM_ROOT:
			case ObjectID.CORRUPT_GRYM_ROOT:
				if (roomTilesMap.get(currentRoom).contains(gameObject.getWorldLocation()))
				{
					for (Integer resource : roomResourcesMap.get(currentRoom))
					{
						if (gameObject.getId() == resource)
						{
							roomResourcesMap.get(currentRoom).remove(resource);
							return;
						}
					}
				}
				break;

			case ObjectID.NODE_35998:
			case ObjectID.NODE_35999:
			case ObjectID.NODE_36101:
			case ObjectID.NODE_36102:
				demiBossNodeLocationsMap.forEach((roomKey, worldPoints) ->
				{
					worldPoints.removeIf(o -> o.equals(gameObject.getWorldLocation()));
					if (highlightNodeMap.containsKey(roomKey))
					{
						highlightNodeMap.get(roomKey).removeIf(o -> o.getWorldLocation().equals(gameObject.getWorldLocation()));
					}
				});
				break;
		}
	}

	private void updateRoomResources(Integer room)
	{
		if (demiBossLocationsMap.containsKey(room))
		{
			return;
		}

		int fishingSpots = 0;
		int grymRoots = 0;

		for (int resource : roomResourcesMap.get(room))
		{
			switch (resource)
			{
				case ObjectID.FISHING_SPOT_36068:
				case ObjectID.CORRUPT_FISHING_SPOT:
					if (config.mapShowFishingSpots())
					{
						fishingSpots++;
					}
					break;

				case ObjectID.GRYM_ROOT:
				case ObjectID.CORRUPT_GRYM_ROOT:
					if (config.mapShowGrymLeaves())
					{
						grymRoots++;
					}
					break;
			}
		}

		if (fishingSpots > 0 && grymRoots > 0)
		{
			updateGauntletMap(room, MapIcons.FISHING_SPOT_GRYM_ROOT);
		}
		else if (fishingSpots > 0)
		{
			updateGauntletMap(room, MapIcons.FISHING_SPOT);
		}
		else if (grymRoots > 0)
		{
			updateGauntletMap(room, MapIcons.GRYM_ROOT);
		}
		else
		{
			updateGauntletMap(room, MapIcons.ACTIVE_TILE);
		}
	}

	public void gameStateChanged(GameStateChanged gameStateChanged, Client client)
	{
		switch (gameStateChanged.getGameState())
		{
			case LOADING:
				highlightNodeMap.clear();

				if (!client.isInInstancedRegion() && !newSession)
				{
					stop();
					return;
				}

				if (client.isInInstancedRegion() && !newSession)
				{
					int activatedRoom = calculateActivatedRoom(client.getLocalPlayer().getWorldLocation(), centerTileMap.get(currentRoom));
					updateGauntletMap(activatedRoom, MapIcons.ACTIVE_TILE);
				}
				break;

			case LOGIN_SCREEN:
				module.getPanel().clearPanel();
				break;
		}
	}

	private void updateGauntletMap(Integer room, MapIcons icon)
	{
		String path;
		String type = "regular/";

		if (corrupted)
		{
			type = "corrupted/";
		}

		switch (icon)
		{
			case PLAYER:
				path = type + "player" + module.getFileNameMap().get(room);
				if (room == startLocation)
				{
					path = type + "player_start.png";
				}
				gauntletMap.put(room, ImageUtil.loadImageResource(MapModule.class, path));
				break;

			case BOSS:
				path = type + "hunllef" + module.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(MapModule.class, path));
				break;

			case ACTIVE_TILE:
				path = "active" + module.getFileNameMap().get(room);
				if (room == startLocation)
				{
					path = type + "start_room.png";
				}
				gauntletMap.put(room, ImageUtil.loadImageResource(MapModule.class, path));
				break;

			case DEMIBOSS_UNKNOWN:
				path = type + "demi" + module.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(MapModule.class, path));
				break;

			case DEMIBOSS_MAGIC:
				path = "demiboss/magic" + module.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(MapModule.class, path));
				break;

			case DEMIBOSS_MELEE:
				path = "demiboss/melee" + module.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(MapModule.class, path));
				break;

			case DEMIBOSS_RANGED:
				path = "demiboss/ranged" + module.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(MapModule.class, path));
				break;

			case GRYM_ROOT:
				type = "resources/";
				path = type + "grym" + module.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(MapModule.class, path));
				break;

			case FISHING_SPOT:
				type = "resources/";
				path = type + "fish" + module.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(MapModule.class, path));
				break;

			case FISHING_SPOT_GRYM_ROOT:
				type = "resources/";
				path = type + "grym_fish" + module.getFileNameMap().get(room);
				gauntletMap.put(room, ImageUtil.loadImageResource(MapModule.class, path));
				break;
		}

		if (!newSession)
		{
			module.getPanel().updatePanel(room);
		}
	}

	public BufferedImage scaleImage(Integer size, BufferedImage image)
	{
		if (size == 34)
		{
			return image;
		}

		Image scaledImage = image.getScaledInstance(size, size, Image.SCALE_DEFAULT);
		BufferedImage bufferedImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		bufferedImage.getGraphics().drawImage(scaledImage, 0, 0, null);
		return bufferedImage;
	}
}
