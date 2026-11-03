package com.titipet.gui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/** Icons drawn with Java2D: independent of emoji fonts and Windows font fallback. */
public final class PosIcons {
    private PosIcons() { }

    public static Icon of(String name, int size, Color color) {
        return new VectorIcon(name, size, color);
    }

    private static final class VectorIcon implements Icon {
        private final String name;
        private final int size;
        private final Color color;
        VectorIcon(String name, int size, Color color) {
            this.name = name; this.size = size; this.color = color;
        }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D d = (Graphics2D) g.create();
            d.translate(x, y);
            d.scale(size / 24.0, size / 24.0);
            d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            d.setColor(color);
            d.setStroke(new BasicStroke(1.85f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (name) {
                case "home" -> { path(d, 3, 10, 12, 3, 21, 10); line(d, 5, 9, 5, 21); line(d, 19, 9, 19, 21); line(d, 5, 21, 19, 21); line(d, 10, 21, 10, 14); line(d, 10, 14, 14, 14); line(d, 14, 14, 14, 21); }
                case "clock" -> { oval(d, 3, 3, 18, 18); line(d, 12, 7, 12, 12); line(d, 12, 12, 16, 14); }
                case "calendar" -> { round(d, 3, 5, 18, 16, 2); line(d, 3, 10, 21, 10); line(d, 8, 3, 8, 7); line(d, 16, 3, 16, 7); }
                case "paw" -> { fillOval(d, 7, 12, 11, 8); fillOval(d, 3, 8, 4, 5); fillOval(d, 8, 4, 4, 5); fillOval(d, 14, 4, 4, 5); fillOval(d, 19, 8, 3, 5); }
                case "bath", "spa" -> { path(d, 4, 13, 20, 13, 19, 18, 17, 20, 7, 20, 5, 18, 4, 13); line(d, 5, 13, 5, 8); line(d, 5, 8, 8, 5); line(d, 8, 5, 11, 8); line(d, 3, 13, 21, 13); }
                case "tools" -> { path(d, 7, 4, 11, 8, 8, 11, 4, 7); line(d, 10, 10, 20, 20); line(d, 20, 20, 22, 18); line(d, 22, 18, 12, 8); }
                case "cart" -> { path(d, 3, 5, 5, 5, 8, 17, 19, 17, 21, 9, 7, 9); oval(d, 8, 19, 2, 2); oval(d, 17, 19, 2, 2); }
                case "receipt" -> { path(d, 6, 3, 18, 3, 18, 21, 15, 19, 12, 21, 9, 19, 6, 21, 6, 3); line(d, 9, 8, 15, 8); line(d, 9, 12, 15, 12); line(d, 9, 16, 13, 16); }
                case "exchange" -> { line(d, 4, 8, 20, 8); path(d, 16, 4, 20, 8, 16, 12); line(d, 20, 16, 4, 16); path(d, 8, 12, 4, 16, 8, 20); }
                case "users" -> { oval(d, 8, 3, 8, 8); path(d, 4, 20, 4, 17, 7, 13, 17, 13, 20, 17, 20, 20, 4, 20); }
                case "heart" -> { path(d, 12, 20, 4, 12, 3, 9, 4, 5, 8, 4, 12, 8, 16, 4, 20, 5, 21, 9, 20, 12, 12, 20); }
                case "box" -> { path(d, 12, 3, 21, 8, 21, 17, 12, 22, 3, 17, 3, 8, 12, 3); line(d, 3, 8, 12, 13); line(d, 21, 8, 12, 13); line(d, 12, 13, 12, 22); }
                case "tag" -> { path(d, 3, 4, 12, 4, 21, 13, 13, 21, 3, 11, 3, 4); oval(d, 6, 7, 2, 2); }
                case "chart" -> { line(d, 4, 21, 4, 3); line(d, 4, 21, 22, 21); line(d, 8, 18, 8, 12); line(d, 13, 18, 13, 7); line(d, 18, 18, 18, 4); }
                case "bell" -> { path(d, 5, 18, 8, 15, 8, 10, 9, 6, 12, 5, 15, 6, 16, 10, 16, 15, 19, 18, 5, 18); path(d, 10, 21, 12, 22, 14, 21); }
                case "search" -> { oval(d, 3, 3, 13, 13); line(d, 15, 15, 22, 22); }
                case "help" -> { oval(d, 3, 3, 18, 18); path(d, 9, 9, 10, 7, 14, 7, 16, 9, 15, 12, 12, 14, 12, 16); fillOval(d, 11, 18, 2, 2); }
                case "close" -> { line(d, 5, 5, 19, 19); line(d, 19, 5, 5, 19); }
                case "logout" -> { path(d, 11, 4, 5, 4, 5, 20, 11, 20); line(d, 10, 12, 21, 12); path(d, 17, 8, 21, 12, 17, 16); }
                case "gift" -> { round(d, 4, 10, 16, 11, 1); line(d, 12, 10, 12, 21); round(d, 3, 7, 18, 4, 1); path(d, 12, 7, 7, 7, 6, 4, 8, 3, 12, 7, 16, 3, 18, 4, 17, 7, 12, 7); }
                case "plus" -> { line(d, 12, 5, 12, 19); line(d, 5, 12, 19, 12); }
                case "shirt" -> { path(d, 7, 3, 4, 6, 2, 12, 6, 14, 8, 10, 8, 21, 16, 21, 16, 10, 18, 14, 22, 12, 20, 6, 17, 3, 14, 5, 10, 5, 7, 3); }
                case "food" -> { round(d, 7, 3, 10, 18, 2); line(d, 7, 8, 17, 8); oval(d, 10, 11, 4, 4); }
                default -> { oval(d, 4, 4, 16, 16); fillOval(d, 10, 10, 4, 4); }
            }
            d.dispose();
        }
        private void line(Graphics2D g, int x, int y, int x2, int y2) { g.drawLine(x, y, x2, y2); }
        private void oval(Graphics2D g, int x, int y, int w, int h) { g.drawOval(x, y, w, h); }
        private void fillOval(Graphics2D g, int x, int y, int w, int h) { g.fillOval(x, y, w, h); }
        private void round(Graphics2D g, int x, int y, int w, int h, int r) { g.drawRoundRect(x, y, w, h, r, r); }
        private void path(Graphics2D g, int... xy) {
            Path2D.Double p = new Path2D.Double();
            p.moveTo(xy[0], xy[1]);
            for (int i = 2; i < xy.length; i += 2) p.lineTo(xy[i], xy[i + 1]);
            g.draw(p);
        }
    }
}
