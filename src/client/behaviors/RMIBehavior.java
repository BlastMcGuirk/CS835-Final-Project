package client.behaviors;

import server.state.Canvas;
import server.state.GraphicalObject;

public class RMIBehavior implements Behavior {

    private Canvas canvas;

    public RMIBehavior(Canvas c) {
        canvas = c;
    }

    @Override
    public void addShape() {
        canvas.addShape(new GraphicalObject());
    }

    @Override
    public void removeMine() {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void undo() {

    }
}
