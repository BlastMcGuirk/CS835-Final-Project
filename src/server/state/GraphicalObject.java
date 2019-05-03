package server.state;

import java.awt.*;
import java.io.Serializable;
import java.util.TimerTask;

/**
 * Holds the information on a shape added to the Canvas.
 * It is Serializable because the client sends these shapes
 * to the server using RMI, and vice versa.
 */
public class GraphicalObject extends TimerTask implements Serializable {

    // ID of client origin
    private final long ID;

    // Type of shape to be drawn
    public enum ShapeType implements Serializable {Circle { public String toString() { return "Circle"; } },
                        Triangle { public String toString() { return "Triangle"; } },
                        Rectangle { public String toString() { return "Rectangle"; } } }
    private final ShapeType type;

    // Values of width and height
    private final int width, height;

    // Color of shape
    private final Color color;

    // Location on canvas
    private final Point p;

    // Whether or not the GO is marked with client ID
    private boolean marked;

    /**
     * Constructor specifying each attribute
     * @param ID ID of client that created the shape
     * @param type What shape it is
     * @param color The color of the shape
     * @param width The width of the shape
     * @param height The height of the shape
     * @param p Where the shape is located
     */
    public GraphicalObject(long ID, ShapeType type, Color color, int width, int height, Point p) {
        this.ID = ID;
        this.type = type;
        this.color = color;
        this.width = width;
        this.height = height;
        this.p = p;
        this.marked = false;
    }

    /**
     * Constructor using a String (For socket connection and snapshots)
     * @param ID ID of client that created the shape
     * @param value attributes of shape in order of:
     *              type, color, width, height, x, y
     */
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

    /**
     * Clones the object
     * @return clone of the GraphicalObject
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public GraphicalObject clone() {
        return new GraphicalObject(getID(), getType(), getColor(), getWidth(), getHeight(), getPoint());
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

    /**
     * @return The name of the color, used for readability
     */
    private String getColorName() {
        if (color.equals(new Color(0, 0, 0))) return "Black";
        if (color.equals(new Color(255, 0, 0))) return "Red";
        if (color.equals(new Color(0, 255, 0))) return "Green";
        if (color.equals(new Color(0, 0, 255))) return "Blue";
        return "BLACK";
    }

    public Point getPoint() {
        return p;
    }

    public void setMarked(boolean mark) {
        this.marked = mark;
    }

    public boolean isMarked() {
        return marked;
    }

    @Override
    public void run() {
        marked = false;
    }

    /**
     * @return The string displayed in DrawnShapesPanel
     */
    public String displayString() {
        return getColorName() + " " + type.toString() + ": [" + width + "x" + height + "] (" + p.x + ", " + p.y + ")";
    }

    /**
     * @return The standard format for GraphicalObject String constructor
     */
    @Override
    public String toString() {
        return type.toString() + " " + getColorName() + " " + width + " " + height + " " + p.x + " " + p.y;
    }
}
