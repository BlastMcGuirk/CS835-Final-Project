package server.Socket.operations;

import server.state.GraphicalObject;
import server.state.ShapeList;

public class AddOperation extends Operation {

    // Shape
    // Color
    // Width
    // Height
    // X-pos
    // Y-pos
    // Example Operation: SHAPE COLOR WIDTH HEIGHT X Y

    private long ID;
    private String value;
    private final String[] validShapes = {"RECTANGLE", "CIRCLE", "TRIANGLE"};
    private final String[] validColors = {"BLACK", "RED", "BLUE", "GREEN"};
    private final int MIN_SIZE = 0;
    private final int MAX_SIZE = 100;
    private final int CANVAS_SIZE = 400;

    public AddOperation(long ID, String value) {
        this.ID = ID;
        this.value = value;
    }

    @Override
    public boolean isValid() {
        String[] values = value.split(" ");

        // Must be 6 values
        if (values.length != 6) return false;

        // Check shape
        if (!isShapeValid(values[0])) return false;

        // Check color
        if (!isColorValid(values[1])) return false;

        // Check MIN_SIZE < width <= MAX_SIZE
        int width = Integer.parseInt(values[2]);
        if (width <= MIN_SIZE || width > MAX_SIZE) return false;

        // Check MIN_SIZE < height <= MAX_SIZE
        int height = Integer.parseInt(values[3]);
        if (height <= MIN_SIZE || height > MAX_SIZE) return false;

        // Check x position within bounds of canvas (0, CANVAS_SIZE)
        int x = Integer.parseInt(values[4]);
        if (x < 0 || x > CANVAS_SIZE) return false;

        // Check y position within bounds of canvas (0, CANVAS_SIZE)
        int y = Integer.parseInt(values[5]);
        if (y < 0 || y > CANVAS_SIZE) return false;

        return true;
    }

    private boolean isColorValid(String color) {
        for (String op : validColors) {
            if (color.equals(op)) {
                return true;
            }
        }
        return false;
    }

    private boolean isShapeValid(String shape) {
        for (String op : validShapes) {
            if (shape.equals(op)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void apply(ShapeList sl) {
        // Add shape to ShapeList
        GraphicalObject go = new GraphicalObject(ID, value);
        sl.add(go);
    }
}
