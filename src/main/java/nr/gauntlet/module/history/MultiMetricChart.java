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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.client.ui.ColorScheme;

/**
 * Multi-panel chart showing 8 key metrics with 5-run moving averages
 */
public class MultiMetricChart extends JPanel
{
	private static final int MOVING_AVG_WINDOW = 5;
	private static final Color MA_LINE_COLOR = new Color(255, 100, 100); // Red moving average
	private static final Color GRID_COLOR = ColorScheme.LIGHT_GRAY_COLOR.darker();
	private static final Color TEXT_COLOR = Color.LIGHT_GRAY;

	@Data
	@AllArgsConstructor
	private static class MetricConfig
	{
		String title;
		String metric;
		Color color;
		boolean invertY; // Lower is better
	}

	private static final MetricConfig[] METRICS = {
		new MetricConfig("DPS Given", "dpsGiven", new Color(76, 175, 80), false),
		new MetricConfig("DPS Taken (lower = better)", "dpsTaken", new Color(244, 67, 54), true),
		new MetricConfig("Tick Efficiency", "efficiency", new Color(33, 150, 243), false),
		new MetricConfig("Wrong Off Prayers (lower = better)", "wrongOffensivePrayer", new Color(255, 152, 0), true),
		new MetricConfig("Wrong Def Prayers (lower = better)", "wrongDefensivePrayer", new Color(156, 39, 176), true),
		new MetricConfig("Wrong Attack Style (lower = better)", "wrongAttackStyle", new Color(121, 85, 72), true),
		new MetricConfig("Tornado Hits (lower = better)", "tornadoHits", new Color(0, 188, 212), true),
		new MetricConfig("Fight Duration (lower = faster)", "totalTicks", new Color(233, 30, 99), true)
	};

	private List<RunStats> runs = new ArrayList<>();

	public MultiMetricChart()
	{
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setLayout(new GridLayout(4, 2, 10, 10));
		setPreferredSize(new Dimension(0, 800));

		// Create 8 sub-panels
		for (int i = 0; i < 8; i++)
		{
			final int index = i;
			JPanel chartPanel = new JPanel()
			{
				@Override
				protected void paintComponent(Graphics g)
				{
					super.paintComponent(g);
					paintMetricChart(g, index);
				}
			};
			chartPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			add(chartPanel);
		}
	}

	public void setData(List<RunStats> runs)
	{
		this.runs = runs != null ? new ArrayList<>(runs) : new ArrayList<>();
		repaint();
	}

	private void paintMetricChart(Graphics g, int metricIndex)
	{
		if (runs.isEmpty() || metricIndex >= METRICS.length)
		{
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		MetricConfig config = METRICS[metricIndex];
		List<Double> values = extractMetricValues(config.metric);

		if (values.isEmpty())
		{
			return;
		}

		// Each panel paints within its own bounds
		JPanel panel = (JPanel) getComponent(metricIndex);
		int width = panel.getWidth();
		int height = panel.getHeight();

		// Chart area
		int padding = 40;
		int chartWidth = width - 2 * padding;
		int chartHeight = height - 2 * padding;
		int x0 = padding;
		int y0 = padding;

		// Find min/max
		double minValue = values.stream().min(Double::compare).orElse(0.0);
		double maxValue = values.stream().max(Double::compare).orElse(100.0);
		double range = maxValue - minValue;
		if (range == 0) range = 1;
		minValue -= range * 0.1;
		maxValue += range * 0.1;

		// Draw title
		g2.setColor(TEXT_COLOR);
		FontMetrics fm = g2.getFontMetrics();
		int titleWidth = fm.stringWidth(config.title);
		g2.drawString(config.title, (width - titleWidth) / 2, 15);

		// Draw grid
		g2.setColor(GRID_COLOR);
		for (int i = 0; i <= 5; i++)
		{
			int y = y0 + (chartHeight * i / 5);
			g2.drawLine(x0, y, x0 + chartWidth, y);
		}

		// Draw axes
		g2.setColor(TEXT_COLOR);
		g2.drawLine(x0, y0, x0, y0 + chartHeight);
		g2.drawLine(x0, y0 + chartHeight, x0 + chartWidth, y0 + chartHeight);

		// Draw data points and lines
		int n = values.size();
		int[] xPoints = new int[n];
		int[] yPoints = new int[n];

		for (int i = 0; i < n; i++)
		{
			double value = values.get(i);
			double normalized = (value - minValue) / (maxValue - minValue);

			if (config.invertY)
			{
				normalized = 1.0 - normalized; // Invert for "lower is better"
			}

			xPoints[i] = x0 + (int) ((double) i / (n - 1) * chartWidth);
			yPoints[i] = y0 + chartHeight - (int) (normalized * chartHeight);
		}

		// Draw faint line connecting points
		g2.setColor(new Color(config.color.getRed(), config.color.getGreen(), config.color.getBlue(), 80));
		g2.setStroke(new BasicStroke(1f));
		for (int i = 0; i < n - 1; i++)
		{
			g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
		}

		// Draw scatter points
		g2.setColor(new Color(config.color.getRed(), config.color.getGreen(), config.color.getBlue(), 150));
		for (int i = 0; i < n; i++)
		{
			g2.fillOval(xPoints[i] - 3, yPoints[i] - 3, 6, 6);
		}

		// Draw 5-run moving average
		if (n >= MOVING_AVG_WINDOW)
		{
			List<Double> movingAvg = calculateMovingAverage(values, MOVING_AVG_WINDOW);
			g2.setColor(MA_LINE_COLOR);
			g2.setStroke(new BasicStroke(2f));

			for (int i = 0; i < movingAvg.size() - 1; i++)
			{
				double v1 = movingAvg.get(i);
				double v2 = movingAvg.get(i + 1);
				double n1 = (v1 - minValue) / (maxValue - minValue);
				double n2 = (v2 - minValue) / (maxValue - minValue);

				if (config.invertY)
				{
					n1 = 1.0 - n1;
					n2 = 1.0 - n2;
				}

				int x1 = x0 + (int) ((double) i / (n - 1) * chartWidth);
				int y1 = y0 + chartHeight - (int) (n1 * chartHeight);
				int x2 = x0 + (int) ((double) (i + 1) / (n - 1) * chartWidth);
				int y2 = y0 + chartHeight - (int) (n2 * chartHeight);

				g2.drawLine(x1, y1, x2, y2);
			}

			// Draw legend
			g2.setFont(g2.getFont().deriveFont(9f));
			g2.drawString("5-run avg", x0 + chartWidth - 60, y0 + 15);
		}

		// Draw axis labels
		g2.setFont(g2.getFont().deriveFont(9f));
		g2.setColor(TEXT_COLOR);

		// Y-axis labels
		for (int i = 0; i <= 5; i++)
		{
			int y = y0 + (chartHeight * i / 5);
			double value = maxValue - ((maxValue - minValue) * i / 5);
			String label = String.format("%.1f", value);
			g2.drawString(label, 5, y + 4);
		}

		// X-axis label
		String xLabel = "Run #";
		g2.drawString(xLabel, width / 2 - 15, height - 5);
	}

	private List<Double> extractMetricValues(String metric)
	{
		List<Double> values = new ArrayList<>();

		// Reverse to get chronological order (oldest to newest)
		for (int i = runs.size() - 1; i >= 0; i--)
		{
			RunStats run = runs.get(i);
			Double value = getMetricValue(run, metric);
			if (value != null)
			{
				values.add(value);
			}
		}

		return values;
	}

	private Double getMetricValue(RunStats run, String metric)
	{
		switch (metric)
		{
			case "totalTicks": return (double) run.getTotalTicks();
			case "lostTicks": return (double) run.getLostTicks();
			case "usedTicks": return (double) run.getUsedTicks();
			case "efficiency": return run.getEfficiency();
			case "playerAttacks": return (double) run.getPlayerAttacks();
			case "wrongOffensivePrayer": return (double) run.getWrongOffensivePrayer();
			case "wrongAttackStyle": return (double) run.getWrongAttackStyle();
			case "hunllefAttacks": return (double) run.getHunllefAttacks();
			case "wrongDefensivePrayer": return (double) run.getWrongDefensivePrayer();
			case "hunllefStomps": return (double) run.getHunllefStomps();
			case "tornadoHits": return (double) run.getTornadoHits();
			case "floorTileHits": return (double) run.getFloorTileHits();
			case "damageTaken": return (double) run.getDamageTaken();
			case "dpsTaken": return run.getDpsTaken();
			case "dpsGiven": return run.getDpsGiven();
			default: return null;
		}
	}

	private List<Double> calculateMovingAverage(List<Double> data, int window)
	{
		List<Double> result = new ArrayList<>();

		for (int i = 0; i < data.size(); i++)
		{
			int start = Math.max(0, i - window + 1);
			double sum = 0;
			for (int j = start; j <= i; j++)
			{
				sum += data.get(j);
			}
			result.add(sum / (i - start + 1));
		}

		return result;
	}
}
