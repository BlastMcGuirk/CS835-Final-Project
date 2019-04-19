package server.state;

import java.awt.*;
import java.io.Serializable;

public class GraphicalObject implements Serializable {

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
        String[] parts = value.split(" ");

        // ShapeType
        switch (parts[0]) {
            case "Circle":
                type = ShapeType.Circle;
                break;
            case "Triangle":
                type = ShapeType.Triangle;
                break;
            case "Rectangle":
                type = ShapeType.Rectangle;
                break;
            default:
                type = ShapeType.Circle;
        }

        // Color
        switch (parts[1]) {
            case "Black":
                color = Color.BLACK;
                break;
            case "Red":
                color = Color.RED;
                break;
            case "Green":
                color = Color.GREEN;
                break;
            case "Blue":
                color = Color.BLUE;
                break;
            default:
                color = Color.BLACK;
        }

        width = Integer.parseInt(parts[2]);
        height = Integer.parseInt(parts[3]);
        p = new Point(Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
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
