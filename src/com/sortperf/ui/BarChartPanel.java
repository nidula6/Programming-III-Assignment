package com.sortperf.ui;

import com.sortperf.model.SortResult;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/** Simple horizontal bar chart comparing execution times. The fastest bar is green. */
public class BarChartPanel extends JPanel {

    private static final Color BEST = new Color(46, 160, 67);
    private static final Color OTHER = new Color(66, 133, 244);

    private List<SortResult> results = new ArrayList<>();

    public BarChartPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(380, 220));
    }

    public void setResults(List<SortResult> newResults) {
        this.results = new ArrayList<>(newResults);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        FontMetrics fm = g.getFontMetrics();

        if (results.isEmpty()) {
            g.setColor(Color.GRAY);
            g.drawString("Run the sorting to see the comparison chart.", 12, 24);
            g.dispose();
            return;
        }

        long max = 1;
        long min = Long.MAX_VALUE;
        for (SortResult r : results) {
            max = Math.max(max, r.getTimeNanos());
            min = Math.min(min, r.getTimeNanos());
        }

        int labelWidth = 0;
        for (SortResult r : results) {
            labelWidth = Math.max(labelWidth, fm.stringWidth(r.getAlgorithmName()));
        }
        labelWidth += 16;

        int valueWidth = fm.stringWidth("00000.000 ms") + 8;
        int top = 12;
        int barArea = Math.max(40, getWidth() - labelWidth - valueWidth - 12);
        int rowHeight = Math.min(40, Math.max(24, (getHeight() - top - 8) / results.size()));

        for (int i = 0; i < results.size(); i++) {
            SortResult r = results.get(i);
            int y = top + i * rowHeight;
            int barWidth = Math.max(2, (int) ((double) r.getTimeNanos() / max * barArea));

            g.setColor(Color.BLACK);
            g.drawString(r.getAlgorithmName(), 8, y + rowHeight / 2 + fm.getAscent() / 2 - 2);

            g.setColor(r.getTimeNanos() == min ? BEST : OTHER);
            g.fillRoundRect(labelWidth, y + 4, barWidth, rowHeight - 8, 6, 6);

            g.setColor(Color.DARK_GRAY);
            g.drawString(String.format("%.3f ms", r.getTimeMillis()), labelWidth + barWidth + 6,
                    y + rowHeight / 2 + fm.getAscent() / 2 - 2);
        }
        g.dispose();
    }
}
