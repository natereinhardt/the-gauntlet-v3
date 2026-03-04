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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * Plugin panel to display Gauntlet run history with charts and statistics
 */
@Slf4j
public class HistoryPanel extends PluginPanel
{
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd HH:mm");

	private final RunHistoryManager historyManager;
	private JTable historyTable;
	private DefaultTableModel tableModel;
	private MultiMetricChart chart;
	private JLabel statsLabel;

	@Inject
	public HistoryPanel(RunHistoryManager historyManager)
	{
		super(false);
		this.historyManager = historyManager;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		init();
		refresh();
	}

	private void init()
	{
		// Header
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel titleLabel = new JLabel("Gauntlet Run History");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(titleLabel.getFont().deriveFont(16f));
		titleLabel.setAlignmentX(CENTER_ALIGNMENT);
		headerPanel.add(titleLabel);

		headerPanel.add(Box.createVerticalStrut(10));

		// Stats summary
		statsLabel = new JLabel();
		statsLabel.setForeground(Color.LIGHT_GRAY);
		statsLabel.setAlignmentX(CENTER_ALIGNMENT);
		headerPanel.add(statsLabel);

		add(headerPanel, BorderLayout.NORTH);

		// Main content panel
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Chart section - 4x2 grid showing 8 key metrics
		JPanel chartPanel = new JPanel(new BorderLayout());
		chartPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		chartPanel.setBorder(BorderFactory.createCompoundBorder(
			new EmptyBorder(10, 10, 10, 10),
			BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR)
		));

		JLabel chartTitle = new JLabel("Performance Metrics (with 5-run moving averages)");
		chartTitle.setForeground(Color.WHITE);
		chartTitle.setHorizontalAlignment(SwingConstants.CENTER);
		chartPanel.add(chartTitle, BorderLayout.NORTH);

		chart = new MultiMetricChart();
		JScrollPane chartScroll = new JScrollPane(chart);
		chartScroll.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		chartScroll.setBorder(null);
		chartPanel.add(chartScroll, BorderLayout.CENTER);

		contentPanel.add(chartPanel);
		contentPanel.add(Box.createVerticalStrut(10));

		// Table section
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		tablePanel.setBorder(new EmptyBorder(0, 10, 10, 10));

		String[] columnNames = {"Date", "Ticks", "Used %", "Type", "Attacks", "Dmg", "Outcome"};
		tableModel = new DefaultTableModel(columnNames, 0)
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};

		historyTable = new JTable(tableModel);
		historyTable.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		historyTable.setForeground(Color.WHITE);
		historyTable.setSelectionBackground(ColorScheme.MEDIUM_GRAY_COLOR);
		historyTable.setRowHeight(25);
		historyTable.setShowGrid(false);
		historyTable.setIntercellSpacing(new Dimension(0, 0));

		// Center align all columns
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		for (int i = 0; i < historyTable.getColumnCount(); i++)
		{
			historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}

		JScrollPane scrollPane = new JScrollPane(historyTable);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.getViewport().setBackground(ColorScheme.DARKER_GRAY_COLOR);
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		contentPanel.add(tablePanel);

		// Button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 2, 5, 5));
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		buttonPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> refresh());
		buttonPanel.add(refreshButton);

		JButton exportHTMLButton = new JButton("Export HTML");
		exportHTMLButton.setToolTipText("Export to HTML (opens in browser, can save as PDF)");
		exportHTMLButton.addActionListener(e -> exportHTML());
		buttonPanel.add(exportHTMLButton);

		JButton exportCSVButton = new JButton("Export CSV");
		exportCSVButton.setToolTipText("Export to CSV for data analysis");
		exportCSVButton.addActionListener(e -> exportCSV());
		buttonPanel.add(exportCSVButton);

		JButton clearButton = new JButton("Clear History");
		clearButton.addActionListener(e ->
		{
			int result = JOptionPane.showConfirmDialog(
				this,
				"Are you sure you want to delete all run history?",
				"Clear History",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
			);
			if (result == JOptionPane.YES_OPTION)
			{
				historyManager.clearHistory();
				refresh();
			}
		});
		buttonPanel.add(clearButton);

		contentPanel.add(buttonPanel);

		add(contentPanel, BorderLayout.CENTER);
	}

	private void exportHTML()
	{
		File file = historyManager.exportToHTML();
		if (file != null)
		{
			try
			{
				Desktop.getDesktop().browse(file.toURI());
				JOptionPane.showMessageDialog(this,
					"Exported to: " + file.getAbsolutePath() + "\n\nOpening in browser...",
					"Export Successful",
					JOptionPane.INFORMATION_MESSAGE);
			}
			catch (Exception e)
			{
				log.error("Failed to open HTML file", e);
				JOptionPane.showMessageDialog(this,
					"Exported to: " + file.getAbsolutePath() + "\n\nCould not open automatically.",
					"Export Successful",
					JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this,
				"Failed to export HTML file.",
				"Export Failed",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void exportCSV()
	{
		File file = historyManager.exportToCSV();
		if (file != null)
		{
			try
			{
				Desktop.getDesktop().open(file.getParentFile());
				JOptionPane.showMessageDialog(this,
					"Exported to: " + file.getAbsolutePath(),
					"Export Successful",
					JOptionPane.INFORMATION_MESSAGE);
			}
			catch (Exception e)
			{
				log.error("Failed to open directory", e);
				JOptionPane.showMessageDialog(this,
					"Exported to: " + file.getAbsolutePath(),
					"Export Successful",
					JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this,
				"Failed to export CSV file.",
				"Export Failed",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	public void refresh()
	{
		List<RunStats> history = historyManager.getHistory();

		// Update stats label
		int totalRuns = history.size();
		long successRuns = history.stream().filter(r -> "SUCCESS".equals(r.getOutcomeDisplay())).count();
		long deathRuns = history.stream().filter(r -> "DEATH".equals(r.getOutcomeDisplay())).count();
		long teleportRuns = history.stream().filter(r -> "TELEPORT".equals(r.getOutcomeDisplay())).count();
		statsLabel.setText(String.format("Total: %d | Success: %d | Deaths: %d | Teleports: %d",
			totalRuns, successRuns, deathRuns, teleportRuns));

		// Update table
		tableModel.setRowCount(0);
		for (RunStats run : history)
		{
			String outcome = run.getOutcomeDisplay();
			String outcomeSymbol = outcome.equals("SUCCESS") ? "✓" :
															outcome.equals("TELEPORT") ? "◄" : "✗";

			Object[] row = {
				DATE_FORMAT.format(new Date(run.getDate())),
				run.getTotalTicks(),
				String.format("%.1f%%", run.getEfficiency()),
				run.isCorrupted() ? "CG" : "Normal",
				run.getPlayerAttacks(),
				run.getDamageTaken(),
				outcomeSymbol + " " + outcome
			};
			tableModel.addRow(row);
		}

		// Update chart with all runs
		chart.setData(history);
	}
}
