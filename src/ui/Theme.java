package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Theme {
    // ===== COLORS =====
    public static final Color BG = new Color(246, 247, 251);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(17, 24, 39);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color BORDER = new Color(229, 231, 235);

    public static final Color PRIMARY = new Color(99, 102, 241);

    // ===== FONTS =====
    public static final Font H1 = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    // ===== HELPERS =====
    public static JPanel card(JComponent inner) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 16, 16, 16)
        ));
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        return b;
    }

    public static JButton softButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 14, 10, 14)
        ));
        b.setBackground(Color.WHITE);
        b.setForeground(TEXT);
        return b;
    }
}
