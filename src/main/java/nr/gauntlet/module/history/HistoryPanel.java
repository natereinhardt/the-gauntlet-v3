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
	private final RunHistoryManager historyManager;
	private JTable historyTable;
	private DefaultTableModel tableModel;
	private JLabel statsLabel;
	private JLabel personalBestLabel;

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

		headerPanel.add(Box.createVerticalStrut(5));

		// Personal best
		personalBestLabel = new JLabel();
		personalBestLabel.setForeground(new Color(76, 175, 80)); // Green
		personalBestLabel.setAlignmentX(CENTER_ALIGNMENT);
		headerPanel.add(personalBestLabel);

		add(headerPanel, BorderLayout.NORTH);

		// Main content panel
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Table section
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		tablePanel.setBorder(new EmptyBorder(0, 10, 0, 10));

		JLabel tableTitle = new JLabel("Recent Runs");
		tableTitle.setForeground(Color.WHITE);
		tableTitle.setFont(tableTitle.getFont().deriveFont(12f));
		tableTitle.setBorder(new EmptyBorder(0, 0, 5, 0));
		tablePanel.add(tableTitle, BorderLayout.NORTH);

		String[] columnNames = {"Date", "Time", "Type", "Result"};
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
		historyTable.setRowHeight(30);
		historyTable.setShowGrid(true);
		historyTable.setGridColor(ColorScheme.LIGHT_GRAY_COLOR.darker());
		historyTable.setIntercellSpacing(new Dimension(3, 2));
		historyTable.setFont(historyTable.getFont().deriveFont(11f));

		// Center align all columns
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		for (int i = 0; i < historyTable.getColumnCount(); i++)
		{
			historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}

		// Adjust column widths to fit sidebar better
		historyTable.getColumnModel().getColumn(0).setPreferredWidth(40); // Date
		historyTable.getColumnModel().getColumn(0).setMaxWidth(50);
		historyTable.getColumnModel().getColumn(1).setPreferredWidth(65); // Time
		historyTable.getColumnModel().getColumn(2).setPreferredWidth(40); // Type
		historyTable.getColumnModel().getColumn(2).setMaxWidth(50);
		historyTable.getColumnModel().getColumn(3).setPreferredWidth(45); // Result

		JScrollPane scrollPane = new JScrollPane(historyTable);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.getViewport().setBackground(ColorScheme.DARKER_GRAY_COLOR);
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		contentPanel.add(tablePanel);
		contentPanel.add(Box.createVerticalStrut(10));

		// Button panel - vertical layout for better space usage
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		buttonPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

		// Primary action - View Full Report
		JButton viewReportButton = new JButton("📊 View Full Report (HTML)");
		viewReportButton.setToolTipText("Opens interactive charts in your browser");
		viewReportButton.setFont(viewReportButton.getFont().deriveFont(12f));
		viewReportButton.setBackground(new Color(33, 150, 243));
		viewReportButton.setForeground(Color.WHITE);
		viewReportButton.setAlignmentX(CENTER_ALIGNMENT);
		viewReportButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
		viewReportButton.addActionListener(e -> exportHTML());
		buttonPanel.add(viewReportButton);

		buttonPanel.add(Box.createVerticalStrut(8));

		// Secondary actions in a row
		JPanel secondaryPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		secondaryPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		secondaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

		JButton exportCSVButton = new JButton("Export CSV");
		exportCSVButton.setToolTipText("Export raw data for analysis");
		exportCSVButton.addActionListener(e -> exportCSV());
		secondaryPanel.add(exportCSVButton);

		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> refresh());
		secondaryPanel.add(refreshButton);

		buttonPanel.add(secondaryPanel);
		buttonPanel.add(Box.createVerticalStrut(8));

		// Clear button (danger action)
		JButton clearButton = new JButton("Clear All History");
		clearButton.setToolTipText("Permanently delete all run data");
		clearButton.setForeground(new Color(244, 67, 54)); // Red text
		clearButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		clearButton.addActionListener(e ->
		{
			int result = JOptionPane.showConfirmDialog(
				this,
				"Are you sure you want to delete all run history?\nThis cannot be undone!",
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
		statsLabel.setText(String.format("<html><center>Total: %d | Successes: %d | Deaths: %d | Teleports: %d</center></html>",
			totalRuns, successRuns, deathRuns, teleportRuns));

		// Find personal best (successful corrupted runs)
		RunStats personalBest = history.stream()
			.filter(r -> "SUCCESS".equals(r.getOutcomeDisplay()) && r.isCorrupted())
			.min((a, b) -> Integer.compare(a.getTotalTicks(), b.getTotalTicks()))
			.orElse(null);

		if (personalBest != null)
		{
			double minutes = personalBest.getTotalTicks() * 0.6 / 60.0;
			personalBestLabel.setText(String.format("🏆 Personal Best (CG): %d ticks (%.1f min) | %.1f%% efficiency",
				personalBest.getTotalTicks(), minutes, personalBest.getEfficiency()));
		}
		else
		{
			personalBestLabel.setText("🏆 No successful corrupted runs yet");
		}

		// Update table - show most recent runs first
		tableModel.setRowCount(0);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
		
		for (RunStats run : history)
		{
			String outcome = run.getOutcomeDisplay();
			String outcomeSymbol = outcome.equals("SUCCESS") ? "✓" :
															outcome.equals("TELEPORT") ? "◄" : "✗";
			
			Date runDate = new Date(run.getDate());
			String timeDisplay = String.format("%d (%.0f%%)", run.getTotalTicks(), run.getEfficiency());

			Object[] row = {
				dateFormat.format(runDate),
				timeDisplay,
				run.isCorrupted() ? "CG" : "Norm",
				outcomeSymbol
			};
			tableModel.addRow(row);
		}
	}
}
