package GameOfLife;

import java.awt.Color;

public class CellPalette {
    private static final Color PANEL_BACKGROUND = new Color(10, 14, 28);
    private static final Color BOARD_BACKGROUND = new Color(18, 25, 44);
    private static final Color GRID_COLOR = new Color(60, 73, 108);
    private static final Color TEXT_COLOR = new Color(228, 234, 255);
    private static final Color CONTROL_BACKGROUND = new Color(22, 30, 52);

    public Color getPanelBackground() {
        return PANEL_BACKGROUND;
    }

    public Color getBoardBackground() {
        return BOARD_BACKGROUND;
    }

    public Color getGridColor() {
        return GRID_COLOR;
    }

    public Color getTextColor() {
        return TEXT_COLOR;
    }

    public Color getControlBackground() {
        return CONTROL_BACKGROUND;
    }

    public Color getAliveColor(int row, int col, int age) {
        float hue = ((row * 13 + col * 7 + age * 5) % 360) / 360.0f;
        float saturation = 0.72f;
        float brightness = Math.min(1.0f, 0.72f + Math.min(age, 8) * 0.03f);
        return Color.getHSBColor(hue, saturation, brightness);
    }

    public Color getDeadCellOverlay(int row, int col) {
        int alpha = ((row + col) % 2 == 0) ? 18 : 10;
        return new Color(255, 255, 255, alpha);
    }
}
