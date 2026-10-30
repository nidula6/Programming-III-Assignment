package com.sortperf;

import com.sortperf.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Application entry point. Run this class from IntelliJ. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // fall back to the default Swing look and feel
            }
            new MainFrame().setVisible(true);
        });
    }
}
