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

    // clientID of the shape
    private long shapeID;

    // clientID of client origin
    private long clientID;

    // Type of shape to be drawn
    public enum ShapeType implements Serializable {Circle { public String toString() { return "Circle"; } },
                        Triangle { public String toString() { return "Triangle"; } },
                        Rectangle { public String toString() { return "Rectangle"; } } }
    private ShapeType type;

    // Values of width and height
    private int width, height;

    // Color of shape
    private Color color;

    // Location on canvas
    private final Point p;

    // Whether or not the GO is marked with client clientID
    private boolean marked;

    /**
     * Constructor specifying each attribute
     * @param ID clientID of client that created the shape
     * @param type What shape it is
     * @param color The color of the shape
     * @param width The width of the shape
     * @param height The height of the shape
     * @param p Where the shape is located
     */
    public GraphicalObject(long ID, ShapeType type, Color color, int width, int height, Point p) {
        this.clientID = ID;
        this.type = type;
        this.color = color;
        this.width = width;
        this.height = height;
        this.p = p;
        this.marked = false;
    }

    /**
     * Constructor using a String (For socket connection and snapshots)
     * @param ID clientID of client that created the shape
     * @param value attributes of shape in order of:
     *              type, color, width, height, x, y
     */
    public GraphicalObject(long ID, String value) {
        this.clientID = ID;
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
        color = getNameColor(parts[1]);

        // Width/Height
        width = Integer.parseInt(parts[2]);
        height = Integer.parseInt(parts[3]);

        // Point
        p = new Point(Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
    }

    synchronized void edit(long clientID, ShapeType type, String color, int width, int height) {
        this.clientID = clientID;
        this.type = type;
        this.color = getNameColor(color);
        this.width = width;
        this.height = height;
    }

    /**
     * Clones the object
     * @return clone of the GraphicalObject
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public synchronized GraphicalObject clone() {
        return new GraphicalObject(getClientID(), getType(), getColor(), getWidth(), getHeight(), getPoint());
    }

    public synchronized void setShapeID(long id) {
        this.shapeID = id;
    }
    public synchronized long getShapeID() {
        return shapeID;
    }

    public synchronized long getClientID() {
        return clientID;
    }

    public synchronized ShapeType getType() {
        return type;
    }

    public synchronized int getWidth() {
        return width;
    }

    public synchronized int getHeight() {
        return height;
    }

    public synchronized Color getColor() {
        return color;
    }

    /**
     * @return The name of the color, used for readability
     */
    public synchronized String getColorName() {
        if (color.equals(new Color(0, 0, 0))) return "Black";
        if (color.equals(new Color(255, 0, 0))) return "Red";
        if (color.equals(new Color(0, 255, 0))) return "Green";
        if (color.equals(new Color(0, 0, 255))) return "Blue";
        return "BLACK";
    }

    private Color getNameColor(String s) {
        switch (s) {
            case "Black":
                return Color.BLACK;
            case "Red":
                return Color.RED;
            case "Green":
                return Color.GREEN;
            case "Blue":
                return Color.BLUE;
            default:
                return Color.BLACK;
        }
    }

    public Point getPoint() {
        return p;
    }

    public synchronized void setMarked(boolean mark) {
        this.marked = mark;
    }

    public synchronized boolean isMarked() {
        return marked;
    }

    @Override
    public void run() {
        setMarked(false);
    }

    /**
     * @return The standard format for GraphicalObject String constructor
     */
    @Override
    public String toString() {
        return type.toString() + " " + getColorName() + " " + width + " " + height + " " + p.x + " " + p.y;
    }
}
