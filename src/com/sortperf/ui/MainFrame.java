package com.sortperf.ui;

import com.sortperf.io.CsvLoader;
import com.sortperf.model.Dataset;
import com.sortperf.model.NumericColumn;
import com.sortperf.model.SortResult;
import com.sortperf.service.PerformanceEvaluator;
import com.sortperf.sort.HeapSort;
import com.sortperf.sort.InsertionSort;
import com.sortperf.sort.MergeSort;
import com.sortperf.sort.QuickSort;
import com.sortperf.sort.ShellSort;
import com.sortperf.sort.SortingAlgorithm;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Main window: upload a CSV, pick a numeric column, choose algorithms, run them and compare
 * the execution times. Heavy work (file loading, sorting) runs in background SwingWorkers so
 * the interface never freezes.
 */
public class MainFrame extends JFrame {

    private static final int PREVIEW_ROWS = 200;
    private static final int LARGE_DATASET = 100_000;
    private static final Color BEST_COLOR = new Color(198, 239, 206);
    private static final Color FAIL_COLOR = new Color(255, 199, 206);

    private final PerformanceEvaluator evaluator = new PerformanceEvaluator();
    private final List<SortingAlgorithm> algorithms = Arrays.asList(
            new InsertionSort(), new ShellSort(), new MergeSort(), new QuickSort(), new HeapSort());
    private final Map<SortingAlgorithm, JCheckBox> algorithmChecks = new LinkedHashMap<>();

    private final JButton uploadButton = new JButton("Upload CSV...");
    private final JLabel fileLabel = new JLabel("No file loaded");
    private final JLabel datasetInfoLabel = new JLabel(" ");
    private final JComboBox<ColumnItem> columnCombo = new JComboBox<>();
    private final JButton runButton = new JButton("Run Sorting");
    private final JProgressBar progressBar = new JProgressBar();
    private final JTable previewTable = new JTable();
    private final DefaultTableModel resultModel = new DefaultTableModel(
            new Object[]{"Rank", "Algorithm", "Time (ms)", "Avg. complexity", "Sorted correctly"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable resultTable = new JTable(resultModel);
    private final JLabel bestLabel = new JLabel("Best performing algorithm: -");
    private final JTextArea sampleArea = new JTextArea(3, 30);
    private final BarChartPanel chartPanel = new BarChartPanel();
    private final JLabel statusLabel = new JLabel("Ready. Upload a CSV file to begin.");

    private Dataset dataset;
    private boolean rankingFinal = false;

    public MainFrame() {
        super("Sorting Algorithm Performance Evaluator - IC4302 Programming III");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        add(buildControlPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 6, 10));
        add(statusLabel, BorderLayout.SOUTH);

        setSize(1150, 780);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        updateControls(false);
    }

    // ------------------------------------------------------------------ UI construction

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filePanel.setBorder(BorderFactory.createTitledBorder("1. Dataset"));
        uploadButton.addActionListener(e -> onUploadClicked());
        filePanel.add(uploadButton);
        filePanel.add(fileLabel);
        filePanel.add(datasetInfoLabel);

        JPanel columnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        columnPanel.setBorder(BorderFactory.createTitledBorder("2. Column to sort (numeric columns only)"));
        columnCombo.setPreferredSize(new Dimension(320, 26));
        columnPanel.add(new JLabel("Column:"));
        columnPanel.add(columnCombo);

        JPanel algorithmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        algorithmPanel.setBorder(BorderFactory.createTitledBorder("3. Sorting algorithms"));
        for (SortingAlgorithm algorithm : algorithms) {
            JCheckBox box = new JCheckBox(algorithm.getName(), true);
            algorithmChecks.put(algorithm, box);
            algorithmPanel.add(box);
        }

        JPanel runPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        runPanel.setBorder(BorderFactory.createTitledBorder("4. Execute"));
        runButton.setFont(runButton.getFont().deriveFont(Font.BOLD));
        runButton.addActionListener(e -> onRunClicked());
        progressBar.setPreferredSize(new Dimension(260, 20));
        runPanel.add(runButton);
        runPanel.add(progressBar);

        panel.add(filePanel);
        panel.add(columnPanel);
        panel.add(algorithmPanel);
        panel.add(runPanel);
        return panel;
    }

    private JSplitPane buildCenterPanel() {
        previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane previewScroll = new JScrollPane(previewTable);
        previewScroll.setBorder(BorderFactory.createTitledBorder(
                "Dataset preview (first " + PREVIEW_ROWS + " rows)"));

        resultTable.setRowHeight(24);
        resultTable.setDefaultRenderer(Object.class, new ResultRenderer());

        bestLabel.setFont(bestLabel.getFont().deriveFont(Font.BOLD, 14f));
        bestLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        sampleArea.setEditable(false);
        sampleArea.setLineWrap(true);
        sampleArea.setWrapStyleWord(true);

        JPanel resultsSouth = new JPanel(new BorderLayout());
        resultsSouth.add(bestLabel, BorderLayout.NORTH);
        resultsSouth.add(new JScrollPane(sampleArea), BorderLayout.CENTER);

        JPanel resultsPanel = new JPanel(new BorderLayout(4, 4));
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Performance results"));
        resultsPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        resultsPanel.add(resultsSouth, BorderLayout.SOUTH);

        chartPanel.setBorder(BorderFactory.createTitledBorder("Execution time comparison"));

        JSplitPane bottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsPanel, chartPanel);
        bottom.setResizeWeight(0.55);

        JSplitPane center = new JSplitPane(JSplitPane.VERTICAL_SPLIT, previewScroll, bottom);
        center.setResizeWeight(0.35);
        center.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        return center;
    }

    // ------------------------------------------------------------------ file upload

    private void onUploadClicked() {
        File dataDir = new File("data");
        JFileChooser chooser = new JFileChooser(
                dataDir.isDirectory() ? dataDir : new File(System.getProperty("user.home")));
        chooser.setDialogTitle("Select a CSV dataset");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadFile(chooser.getSelectedFile());
        }
    }

    private void loadFile(File file) {
        updateControls(true);
        statusLabel.setText("Loading " + file.getName() + " ...");

        new SwingWorker<Dataset, Void>() {
            @Override
            protected Dataset doInBackground() throws Exception {
                return CsvLoader.load(file);
            }

            @Override
            protected void done() {
                try {
                    onDatasetLoaded(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    String message = describe(e.getCause());
                    statusLabel.setText("Failed to load file.");
                    JOptionPane.showMessageDialog(MainFrame.this, message,
                            "Cannot load file", JOptionPane.ERROR_MESSAGE);
                } finally {
                    updateControls(false);
                }
            }
        }.execute();
    }

    private void onDatasetLoaded(Dataset loaded) {
        dataset = loaded;
        fileLabel.setText(loaded.getName());
        datasetInfoLabel.setText(String.format("(%,d rows, %d columns, %d numeric)",
                loaded.getRowCount(), loaded.getColumnCount(), loaded.getNumericColumnCount()));

        columnCombo.removeAllItems();
        int firstNumeric = -1;
        for (int i = 0; i < loaded.getColumnCount(); i++) {
            boolean numeric = loaded.isNumeric(i);
            columnCombo.addItem(new ColumnItem(i, loaded.getHeaders().get(i), numeric));
            if (numeric && firstNumeric < 0) {
                firstNumeric = i;
            }
        }
        if (firstNumeric >= 0) {
            columnCombo.setSelectedIndex(firstNumeric);
        }

        fillPreview(loaded);
        clearResults();

        if (firstNumeric < 0) {
            statusLabel.setText("Loaded, but the file has no numeric column to sort.");
            JOptionPane.showMessageDialog(this,
                    "This file has no numeric column, so nothing can be sorted.\n"
                            + "Please upload a CSV that contains at least one numeric column.",
                    "No numeric columns", JOptionPane.WARNING_MESSAGE);
        } else {
            statusLabel.setText("File loaded. Select a numeric column and click 'Run Sorting'.");
        }
    }

    private void fillPreview(Dataset loaded) {
        DefaultTableModel model = new DefaultTableModel(loaded.getHeaders().toArray(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        int limit = Math.min(PREVIEW_ROWS, loaded.getRowCount());
        for (int i = 0; i < limit; i++) {
            model.addRow(loaded.getRows().get(i));
        }
        previewTable.setModel(model);
    }

    // ------------------------------------------------------------------ sorting

    private void onRunClicked() {
        ColumnItem item = (ColumnItem) columnCombo.getSelectedItem();
        if (dataset == null || item == null) {
            warn("Please upload a CSV file first.");
            return;
        }
        if (!item.numeric) {
            warn("Column '" + item.name + "' is not numeric.\nPlease select a numeric column.");
            return;
        }

        NumericColumn column = dataset.extractNumericColumn(item.index);
        if (column.size() == 0) {
            warn("Column '" + item.name + "' has no valid numeric values (it is empty).");
            return;
        }

        List<SortingAlgorithm> selected = new ArrayList<>();
        for (SortingAlgorithm algorithm : algorithms) {
            if (algorithmChecks.get(algorithm).isSelected()) {
                selected.add(algorithm);
            }
        }
        if (selected.isEmpty()) {
            warn("Please tick at least one sorting algorithm.");
            return;
        }

        boolean slowSelected = selected.stream().anyMatch(a -> a instanceof InsertionSort);
        if (slowSelected && column.size() > LARGE_DATASET) {
            int choice = JOptionPane.showConfirmDialog(this,
                    String.format("The column has %,d values. Insertion Sort is O(n^2) and may take a very long time.%n"
                            + "Continue anyway?", column.size()),
                    "Large dataset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String skipped = "";
        if (column.getEmptyCount() + column.getInvalidCount() > 0) {
            skipped = String.format(" (skipped %d blank and %d non-numeric cells)",
                    column.getEmptyCount(), column.getInvalidCount());
        }
        runSorting(column.getValues(), selected, item.name, skipped);
    }

    private void runSorting(double[] data, List<SortingAlgorithm> selected, String columnName, String note) {
        clearResults();
        updateControls(true);
        statusLabel.setText(String.format("Sorting %,d values of '%s' ...%s", data.length, columnName, note));

        new SwingWorker<List<SortResult>, SortResult>() {
            @Override
            protected List<SortResult> doInBackground() {
                List<SortResult> all = new ArrayList<>();
                for (SortingAlgorithm algorithm : selected) {
                    SortResult result = evaluator.run(algorithm, data);
                    all.add(result);
                    publish(result);
                }
                return all;
            }

            @Override
            protected void process(List<SortResult> chunks) {
                for (SortResult result : chunks) {
                    resultModel.addRow(toRow("-", result));
                }
            }

            @Override
            protected void done() {
                try {
                    showFinalResults(get(), columnName, data.length, note);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    statusLabel.setText("Sorting failed.");
                    JOptionPane.showMessageDialog(MainFrame.this, describe(e.getCause()),
                            "Sorting error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    updateControls(false);
                }
            }
        }.execute();
    }

    private void showFinalResults(List<SortResult> results, String columnName, int count, String note) {
        List<SortResult> ranked = evaluator.rank(results);

        resultModel.setRowCount(0);
        for (int i = 0; i < ranked.size(); i++) {
            resultModel.addRow(toRow(String.valueOf(i + 1), ranked.get(i)));
        }
        rankingFinal = true;
        resultTable.repaint();
        chartPanel.setResults(ranked);

        Optional<SortResult> best = evaluator.findBest(results);
        if (best.isPresent()) {
            SortResult b = best.get();
            bestLabel.setText(String.format("Best performing algorithm: %s  (%.3f ms)",
                    b.getAlgorithmName(), b.getTimeMillis()));
            sampleArea.setText("First " + b.getSortedHead().length + " sorted values of '" + columnName
                    + "' (ascending): " + formatValues(b.getSortedHead()));
        } else {
            bestLabel.setText("Best performing algorithm: none (no algorithm produced a correct result)");
        }
        statusLabel.setText(String.format("Done. Sorted %,d values of '%s'.%s", count, columnName, note));
    }

    // ------------------------------------------------------------------ helpers

    private Object[] toRow(String rank, SortResult r) {
        return new Object[]{rank, r.getAlgorithmName(), String.format("%.3f", r.getTimeMillis()),
                r.getComplexity(), r.isVerified() ? "Yes" : "NO"};
    }

    private void clearResults() {
        resultModel.setRowCount(0);
        rankingFinal = false;
        bestLabel.setText("Best performing algorithm: -");
        sampleArea.setText("");
        chartPanel.setResults(Collections.<SortResult>emptyList());
    }

    private void updateControls(boolean busy) {
        uploadButton.setEnabled(!busy);
        columnCombo.setEnabled(!busy && dataset != null);
        runButton.setEnabled(!busy && dataset != null && dataset.hasNumericColumn());
        algorithmChecks.values().forEach(box -> box.setEnabled(!busy));
        progressBar.setIndeterminate(busy);
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Cannot sort", JOptionPane.WARNING_MESSAGE);
    }

    private static String describe(Throwable t) {
        if (t == null) {
            return "Unknown error.";
        }
        if (t instanceof OutOfMemoryError) {
            return "Not enough memory to process this file. Try a smaller dataset.";
        }
        return t.getMessage() != null ? t.getMessage() : t.toString();
    }

    private static String formatValues(double[] values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            double v = values[i];
            if (v == Math.rint(v) && Math.abs(v) < 1e15) {
                sb.append((long) v);
            } else {
                sb.append(v);
            }
        }
        return sb.append(']').toString();
    }

    // ------------------------------------------------------------------ inner classes

    /** Combo box entry that tells the user which columns are numeric. */
    private static final class ColumnItem {
        final int index;
        final String name;
        final boolean numeric;

        ColumnItem(int index, String name, boolean numeric) {
            this.index = index;
            this.name = name;
            this.numeric = numeric;
        }

        @Override
        public String toString() {
            return name + (numeric ? "  (numeric)" : "  (text - cannot be sorted)");
        }
    }

    /** Highlights the winner row in green and failed verifications in red. */
    private final class ResultRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, selected, focus, row, col);
            if (!selected) {
                c.setForeground(Color.BLACK);
                boolean failed = "NO".equals(resultModel.getValueAt(row, 4));
                if (failed) {
                    c.setBackground(FAIL_COLOR);
                } else if (rankingFinal && row == 0) {
                    c.setBackground(BEST_COLOR);
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            return c;
        }
    }
}
