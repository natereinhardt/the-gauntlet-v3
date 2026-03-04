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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stores detailed statistics for a single Gauntlet run
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunStats
{
	// Timing information
	private long date;
	private int totalTicks;
	private int lostTicks;
	private int usedTicks;

	// Player performance
	private int playerAttacks;
	private int wrongOffensivePrayer;
	private int wrongAttackStyle;

	// Hunllef data
	private int hunllefAttacks;
	private int wrongDefensivePrayer;
	private int hunllefStomps;

	// Damage tracking
	private int tornadoHits;
	private int floorTileHits;
	private int damageTaken;
	private double dpsTaken;
	private double dpsGiven;

	// Run metadata
	private boolean isCorrupted;
	private boolean completed;
	private String outcome; // "SUCCESS", "DEATH", "TELEPORT"

	/**
	 * Format time in MM:SS
	 */
	public String getFormattedTime()
	{
		int seconds = (int) (totalTicks * 0.6);
		return String.format("%d:%02d", seconds / 60, seconds % 60);
	}

	/**
	 * Calculate efficiency percentage (used ticks / total ticks)
	 */
	public double getEfficiency()
	{
		return totalTicks > 0 ? (double) usedTicks / totalTicks * 100.0 : 0.0;
	}

	/**
	 * Get formatted outcome for display
	 */
	public String getOutcomeDisplay()
	{
		if (outcome == null)
		{
			return completed ? "SUCCESS" : "DEATH";
		}
		return outcome;
	}
}
