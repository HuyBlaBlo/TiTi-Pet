package com.titipet.util;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

/**
 * JPanel có nền chuyển màu tuyến tính và các góc được bo tròn.
 */
public class RoundedPannelLinear extends JPanel {
    public enum Direction {
        HORIZONTAL,
        VERTICAL,
        DIAGONAL
    }

    private Color startColor;
    private Color endColor;
    private int cornerRadius;
    private Direction direction;

    public RoundedPannelLinear() {
        this(Color.WHITE, new Color(235, 235, 235), 20, Direction.DIAGONAL);
    }

    public RoundedPannelLinear(Color startColor, Color endColor, int cornerRadius) {
        this(startColor, endColor, cornerRadius, Direction.DIAGONAL);
    }

    public RoundedPannelLinear(
            Color startColor,
            Color endColor,
            int cornerRadius,
            Direction direction
    ) {
        setOpaque(false);
        setStartColor(startColor);
        setEndColor(endColor);
        setCornerRadius(cornerRadius);
        setDirection(direction);
    }

    public Color getStartColor() {
        return startColor;
    }

    public void setStartColor(Color startColor) {
        this.startColor = Objects.requireNonNull(
                startColor,
                "Màu bắt đầu không được để trống."
        );
        repaint();
    }

    public Color getEndColor() {
        return endColor;
    }

    public void setEndColor(Color endColor) {
        this.endColor = Objects.requireNonNull(
                endColor,
                "Màu kết thúc không được để trống."
        );
        repaint();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        if (cornerRadius < 0) {
            throw new IllegalArgumentException("Độ bo góc không được nhỏ hơn 0.");
        }
        this.cornerRadius = cornerRadius;
        repaint();
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = Objects.requireNonNull(
                direction,
                "Hướng chuyển màu không được để trống."
        );
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        graphics2D.setPaint(createGradientPaint());
        graphics2D.fillRoundRect(
                0,
                0,
                getWidth(),
                getHeight(),
                cornerRadius,
                cornerRadius
        );
        graphics2D.dispose();

        super.paintComponent(graphics);
    }

    private Paint createGradientPaint() {
        return switch (direction) {
            case HORIZONTAL -> new GradientPaint(
                    0, 0, startColor,
                    getWidth(), 0, endColor
            );
            case VERTICAL -> new GradientPaint(
                    0, 0, startColor,
                    0, getHeight(), endColor
            );
            case DIAGONAL -> new GradientPaint(
                    0, 0, startColor,
                    getWidth(), getHeight(), endColor
            );
        };
    }

    @Override
    public boolean contains(int x, int y) {
        return new RoundRectangle2D.Float(
                0,
                0,
                getWidth(),
                getHeight(),
                cornerRadius,
                cornerRadius
        ).contains(x, y);
    }
}
