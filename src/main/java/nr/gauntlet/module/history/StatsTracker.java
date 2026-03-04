/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2024, LlemonDuck
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

package nr.gauntlet.module.history;

import javax.inject.Singleton;
import lombok.Getter;

/**
 * Tracks detailed performance statistics during a Gauntlet run
 */
@Singleton
public class StatsTracker
{
	@Getter
	private RunStats currentRun;

	private int tickCount;
	private int lastActionTick;
	private int runStartTick;
	private int totalDamageDealt;
	private boolean isCorrupted;

	public void startTracking(boolean corrupted)
	{
		currentRun = new RunStats();
		currentRun.setCorrupted(corrupted);
		currentRun.setDate(System.currentTimeMillis());
		tickCount = 0;
		lastActionTick = 0;
		runStartTick = 0;
		totalDamageDealt = 0;
		isCorrupted = corrupted;
	}

	public void onTick()
	{
		if (currentRun == null)
		{
			return;
		}

		tickCount++;
		currentRun.setTotalTicks(tickCount);
	}

	public void onPlayerAttack()
	{
		if (currentRun == null)
		{
			return;
		}

		currentRun.setPlayerAttacks(currentRun.getPlayerAttacks() + 1);
		lastActionTick = tickCount;
		currentRun.setUsedTicks(currentRun.getUsedTicks() + 1);
	}

	public void onHunllefAttack()
	{
		if (currentRun == null)
		{
			return;
		}

		currentRun.setHunllefAttacks(currentRun.getHunllefAttacks() + 1);
	}

	public void onWrongOffensivePrayer()
	{
		if (currentRun == null)
		{
			return;
		}

		currentRun.setWrongOffensivePrayer(currentRun.getWrongOffensivePrayer() + 1);
	}

	public void onWrongDefensivePrayer()
	{
		if (currentRun == null)
		{
			return;
		}

		currentRun.setWrongDefensivePrayer(currentRun.getWrongDefensivePrayer() + 1);
	}

	public void onWrongAttackStyle()
	{
		if (currentRun == null)
		{
			return;
		}

		currentRun.setWrongAttackStyle(currentRun.getWrongAttackStyle() + 1);
	}

	public void onHunllefStomp()
	{
		if (currentRun == null)
		{
			return;
		}

		currentRun.setHunllefStomps(currentRun.getHunllefStomps() + 1);
	}

	public void onTornadoHit()
	{
		if (currentRun == null)
		{
			return;
		}

		currentRun.setTornadoHits(currentRun.getTornadoHits() + 1);
	}

	public void onFloorTileHit()
	{
		if (currentRun == null)
		{
			return;
		}

		currentRun.setFloorTileHits(currentRun.getFloorTileHits() + 1);
	}

	public void onDamageTaken(int damage)
	{
		if (currentRun == null)
		{
			return;
		}

		currentRun.setDamageTaken(currentRun.getDamageTaken() + damage);
	}

	public void onDamageDealt(int damage)
	{
		if (currentRun == null)
		{
			return;
		}

		totalDamageDealt += damage;
	}

	public void finishRun(boolean completed, String outcome)
	{
		if (currentRun == null)
		{
			return;
		}

		currentRun.setCompleted(completed);
		currentRun.setOutcome(outcome);
		currentRun.setLostTicks(tickCount - currentRun.getUsedTicks());

		// Calculate DPS
		if (tickCount > 0)
		{
			double timeInSeconds = tickCount * 0.6;
			currentRun.setDpsGiven(totalDamageDealt / timeInSeconds);
			currentRun.setDpsTaken(currentRun.getDamageTaken() / timeInSeconds);
		}
	}

	public void finishRun(boolean completed)
	{
		finishRun(completed, completed ? "SUCCESS" : "DEATH");
	}

	public void reset()
	{
		currentRun = null;
		tickCount = 0;
		lastActionTick = 0;
		runStartTick = 0;
		totalDamageDealt = 0;
	}
}
