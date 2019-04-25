package client.behaviors;

import client.GUI.Window;
import server.state.Canvas;
import server.state.GraphicalObject;

import java.util.ArrayList;

public class RMIBehavior implements Behavior {

    private Canvas canvas;
    private long userID;

    private long currentVersion;

    public RMIBehavior(Canvas c) {
        canvas = c;
        userID = c.registerNewUser();
        currentVersion = 0;
    }

    @Override
    public void addShape(GraphicalObject go) {
        canvas.addShape(go);
    }

    @Override
    public void removeYours() {
        canvas.removeAll(userID);
    }

    @Override
    public void removeAll() {
        canvas.removeAll();
    }

    @Override
    public void undo() {
        canvas.undo(userID);
    }

    @Override
    public long getId() {
        return userID;
    }

    @Override
    public ArrayList<GraphicalObject> getGraphicalObjects() {
        return canvas.getShapeList();
    }

    @Override
    public void listenForUpdates(Window w) {
        //noinspection InfiniteLoopStatement
        while (true) {
            if (currentVersion != canvas.getVersionNumber()) {
                currentVersion = canvas.getVersionNumber();
                w.tellToRepaint();
            }
        }
    }


}
