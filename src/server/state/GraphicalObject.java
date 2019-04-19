package server.state;

import java.awt.*;

public class GraphicalObject {

    // ID of client origin
    private final long ID;

    // Type of shape to be drawn
    public enum ShapeType{Circle, Triangle, Rectangle}
    private final ShapeType type;

    // Values of width and height
    private final int width, height;

    // Color of shape
    private final Color color;

    // Location on canvas
    private final Point p;

    public GraphicalObject(long ID, ShapeType type, int width, int height, Color color, Point p) {
        this.ID = ID;
        this.type = type;
        this.width = width;
        this.height = height;
        this.color = color;
        this.p = p;
    }

    public GraphicalObject() {
        this(0, ShapeType.Circle, 50, 50, Color.BLACK, new Point(0,0));
    }

    public GraphicalObject(long ID, String value) {
        this.ID = ID;

    }

    public long getID() {
        return ID;
    }

    public ShapeType getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Color getColor() {
        return color;
    }
}
