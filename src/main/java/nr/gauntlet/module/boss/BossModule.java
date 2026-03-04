/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2023, rdutta <https://github.com/rdutta>
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

package nr.gauntlet.module.boss;

import nr.gauntlet.module.Module;
import nr.gauntlet.module.history.RunHistoryManager;
import nr.gauntlet.module.history.StatsTracker;
import nr.gauntlet.module.overlay.TimerOverlay;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.NullNpcID;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@Singleton
public final class BossModule implements Module
{
	private static final List<Integer> HUNLLEF_IDS = List.of(
		NpcID.CRYSTALLINE_HUNLLEF,
		NpcID.CRYSTALLINE_HUNLLEF_9022,
		NpcID.CRYSTALLINE_HUNLLEF_9023,
		NpcID.CRYSTALLINE_HUNLLEF_9024,
		NpcID.CORRUPTED_HUNLLEF,
		NpcID.CORRUPTED_HUNLLEF_9036,
		NpcID.CORRUPTED_HUNLLEF_9037,
		NpcID.CORRUPTED_HUNLLEF_9038
	);

	private static final List<Integer> TORNADO_IDS = List.of(NullNpcID.NULL_9025, NullNpcID.NULL_9039, 14142); // 14142 is echo tornado, remove after leagues

	@Getter(AccessLevel.PACKAGE)
	private final List<NPC> tornadoes = new ArrayList<>();

	@Inject
	private EventBus eventBus;
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private TimerOverlay timerOverlay;
	@Inject
	private BossOverlay bossOverlay;
	@Inject
	private StatsTracker statsTracker;
	@Inject
	private RunHistoryManager historyManager;

	@Nullable
	@Getter(AccessLevel.PACKAGE)
	private NPC hunllef;

	private boolean isCorrupted;
	private boolean inBossFight;
	private static final int MIN_TICKS_FOR_VALID_RUN = 100; // Filter out immediate teleports

	@Override
	public void start()
	{
		eventBus.register(this);

		for (final NPC npc : client.getTopLevelWorldView().npcs())
		{
			onNpcSpawned(new NpcSpawned(npc));
		}

		// Determine if corrupted based on region
		int[] regions = client.getTopLevelWorldView().getMapRegions();
		isCorrupted = regions != null && regions.length > 0 && regions[0] == 7512;

		// Start stats tracking
		statsTracker.startTracking(isCorrupted);
		inBossFight = true;

		overlayManager.add(timerOverlay);
		overlayManager.add(bossOverlay);
		timerOverlay.setHunllefStart();
	}

	@Override
	public void stop()
	{
		// If fight is still active, player teleported out or left early
		if (inBossFight && statsTracker.getCurrentRun() != null)
		{
			// Only save if fight lasted long enough (not an immediate disconnect)
			if (statsTracker.getCurrentRun().getTotalTicks() >= MIN_TICKS_FOR_VALID_RUN)
			{
				statsTracker.finishRun(false, "TELEPORT");
				historyManager.addRun(statsTracker.getCurrentRun());
			}
		}

		eventBus.unregister(this);
		overlayManager.remove(timerOverlay);
		overlayManager.remove(bossOverlay);
		timerOverlay.reset();
		tornadoes.clear();
		hunllef = null;
		inBossFight = false;
		statsTracker.reset();
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGIN_SCREEN:
			case HOPPING:
				stop();
				break;
		}
	}

	@Subscribe
	public void onActorDeath(final ActorDeath event)
	{
		if (event.getActor() == client.getLocalPlayer())
		{
			// Player died - finish run with failure
			inBossFight = false;
			if (statsTracker.getCurrentRun() != null &&
				statsTracker.getCurrentRun().getTotalTicks() >= MIN_TICKS_FOR_VALID_RUN)
			{
				statsTracker.finishRun(false, "DEATH");
				historyManager.addRun(statsTracker.getCurrentRun());
			}
			timerOverlay.onPlayerDeath();
		}
		else if (event.getActor() == hunllef)
		{
			// Boss died - finish run with success
			log.info("Hunllef died! Finishing successful run.");
			inBossFight = false;
			if (statsTracker.getCurrentRun() != null &&
				statsTracker.getCurrentRun().getTotalTicks() >= MIN_TICKS_FOR_VALID_RUN)
			{
				statsTracker.finishRun(true, "SUCCESS");
				log.info("Saving successful run with {} ticks", statsTracker.getCurrentRun().getTotalTicks());
				historyManager.addRun(statsTracker.getCurrentRun());
			}
			else
			{
				log.warn("Run not saved - currentRun: {}, ticks: {}", 
					statsTracker.getCurrentRun() != null, 
					statsTracker.getCurrentRun() != null ? statsTracker.getCurrentRun().getTotalTicks() : 0);
			}
		}
	}

	@Subscribe
	public void onGameTick(final GameTick event)
	{
		statsTracker.onTick();
	}

	@Subscribe
	public void onInteractingChanged(final InteractingChanged event)
	{
		if (event.getSource() == client.getLocalPlayer() && event.getTarget() == hunllef)
		{
			statsTracker.onPlayerAttack();
		}
		else if (event.getSource() == hunllef && event.getTarget() == client.getLocalPlayer())
		{
			statsTracker.onHunllefAttack();
		}
	}

	@Subscribe
	public void onHitsplatApplied(final HitsplatApplied event)
	{
		if (event.getActor() == client.getLocalPlayer())
		{
			// Player took damage
			int damage = event.getHitsplat().getAmount();
			statsTracker.onDamageTaken(damage);

			// Check if damage from tornado
			if (tornadoes.stream().anyMatch(t ->
				t.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) <= 1))
			{
				statsTracker.onTornadoHit();
			}
		}
		else if (event.getActor() == hunllef)
		{
			// Damage dealt to boss
			int damage = event.getHitsplat().getAmount();
			statsTracker.onDamageDealt(damage);
		}
	}

	@Subscribe
	public void onChatMessage(final ChatMessage event)
	{
		if (!inBossFight || event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = event.getMessage();

		// Detect teleport messages
		if (message.contains("You teleport") ||
			message.contains("You step through the portal") ||
			message.contains("You exit the Gauntlet"))
		{
			inBossFight = false;
			if (statsTracker.getCurrentRun() != null &&
				statsTracker.getCurrentRun().getTotalTicks() >= MIN_TICKS_FOR_VALID_RUN)
			{
				statsTracker.finishRun(false, "TELEPORT");
				historyManager.addRun(statsTracker.getCurrentRun());
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(final NpcSpawned event)
	{
		final NPC npc = event.getNpc();

		if (TORNADO_IDS.contains(npc.getId()))
		{
			tornadoes.add(npc);
		}
		else if (HUNLLEF_IDS.contains(npc.getId()))
		{
			hunllef = npc;
		}
	}

	@Subscribe
	public void onNpcDespawned(final NpcDespawned event)
	{
		final NPC npc = event.getNpc();

		if (TORNADO_IDS.contains(npc.getId()))
		{
			tornadoes.removeIf(t -> t == npc);
		}
		else if (HUNLLEF_IDS.contains(npc.getId()))
		{
			hunllef = null;
		}
	}
}
