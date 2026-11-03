package com.titipet.gui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Bộ màu, chữ, nút bo góc của bản mẫu POS.png; không dùng absolute positioning. */
public final class PosUI {
    public static final Color BG = new Color(245, 248, 253);
    public static final Color WHITE = Color.WHITE;
    public static final Color NAVY = new Color(27, 58, 100);
    public static final Color MUTED = new Color(138, 158, 184);
    public static final Color BLUE = new Color(38, 112, 214);
    public static final Color SOFT_BLUE = new Color(232, 241, 255);
    public static final Color BORDER = new Color(225, 233, 245);
    public static final Color PALE = new Color(249, 251, 255);
    public static final Color RED = new Color(213, 73, 91);
    public static final Font REG = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font MED = new Font("Segoe UI", Font.BOLD, 13);

    private PosUI() { }

    public static JLabel text(String value, int size, boolean bold, Color color) {
        JLabel label = new JLabel(value);
        label.setForeground(color);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static JPanel vertical(int gap) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    public static JPanel horizontal(int gap) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
        p.setOpaque(false);
        return p;
    }

    public static JComponent space(int height) {
        return (JComponent) Box.createRigidArea(new Dimension(1, height));
    }

    public static JPanel line() {
        JPanel p = new JPanel();
        p.setBackground(new Color(238, 243, 250));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        p.setPreferredSize(new Dimension(10, 1));
        return p;
    }

    public static void pad(JComponent c, int t, int l, int b, int r) {
        c.setBorder(BorderFactory.createEmptyBorder(t, l, b, r));
    }

    public static RoundPanel roundPanel(Color color, int radius) {
        return new RoundPanel(color, radius);
    }

    public static StyledButton button(String title, Color fg, Color bg, Color border) {
        return new StyledButton(title, fg, bg, border);
    }

    public static JTextField field(String placeholder) {
        JTextField t = new HintTextField(placeholder);
        t.setFont(REG);
        t.setForeground(NAVY);
        t.setBackground(WHITE);
        t.setCaretColor(BLUE);
        t.putClientProperty("JTextField.placeholderText", placeholder);
        t.setBorder(new CompoundRoundBorder(BORDER, 9, 12, 10));
        t.setPreferredSize(new Dimension(100, 44));
        return t;
    }

    public static <T> JComboBox<T> combo(T[] elements) {
        JComboBox<T> c = new JComboBox<>(elements);
        c.setFont(REG);
        c.setForeground(NAVY);
        c.setBackground(WHITE);
        c.setBorder(new CompoundRoundBorder(BORDER, 8, 7, 7));
        c.setPreferredSize(new Dimension(100, 44));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        c.setFocusable(false);
        return c;
    }


    private static final class HintTextField extends JTextField {
        private final String hint;
        private HintTextField(String hint) { this.hint = hint; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !hasFocus() && hint != null) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(new Color(153, 163, 178));
                g2.setFont(getFont());
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, insets.left + 1, (getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
        }
    }

    /** Load assets relative to project folder, with fallback when started from a nested directory. */
    public static ImageIcon sampleIcon(String fileName, int width, int height) {
        return assetIcon("assets/pos/" + fileName, width, height);
    }

    /** Custom brand assets take priority; users may replace assets/branding/logo.png. */
    public static ImageIcon logoIcon(int width, int height) {
        ImageIcon custom = assetIcon("assets/branding/logo.png", width, height);
        return custom != null ? custom : sampleIcon("logo.png", width, height);
    }

    public static ImageIcon assetIcon(String relativePath, int width, int height) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("").toAbsolutePath();
            for (int attempt = 0; attempt < 5 && path != null; attempt++, path = path.getParent()) {
                java.nio.file.Path imgPath = path.resolve(relativePath);
                if (!java.nio.file.Files.isRegularFile(imgPath)) continue;
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(imgPath.toFile());
                if (img == null) continue;
                return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
            java.net.URL resource = PosUI.class.getClassLoader().getResource(relativePath.replace('\\', '/'));
            if (resource != null) {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(resource);
                if (img != null) return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            System.err.println("Không tải được ảnh: " + relativePath + " (" + e.getMessage() + ")");
        }
        return null;
    }

    public static final class RoundPanel extends JPanel {
        private final Color bg;
        private final int r;
        public RoundPanel(Color bg, int radius) {
            super(); this.bg = bg; this.r = radius;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, r, r);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, r, r);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static final class StyledButton extends JButton {
        private final Color foreground, background, outline;
        private boolean hover;
        public StyledButton(String text, Color foreground, Color background, Color outline) {
            super(text);
            this.foreground = foreground; this.background = background; this.outline = outline;
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(foreground);
            setOpaque(false); setFocusPainted(false); setBorderPainted(false);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = background;
            if (hover && isEnabled()) {
                color = new Color(Math.max(0, color.getRed() - 7),
                        Math.max(0, color.getGreen() - 6), Math.max(0, color.getBlue() - 3));
            }
            if (!isEnabled()) color = new Color(237, 240, 245);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 13, 13);
            if (outline != null) {
                g2.setColor(outline);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 13, 13);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Paint rounded stroke without using null layout or setBounds. */
    public static final class CompoundRoundBorder extends AbstractBorder {
        private final Color color;
        private final int radius, horizontal, vertical;
        public CompoundRoundBorder(Color color, int radius, int horizontal, int vertical) {
            this.color = color; this.radius = radius;
            this.horizontal = horizontal; this.vertical = vertical;
        }
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(vertical, horizontal, vertical, horizontal);
        }
        @Override public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(vertical, horizontal, vertical, horizontal); return insets;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width-1, height-1, radius*2, radius*2);
            g2.dispose();
        }
    }
}
