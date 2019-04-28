package client.behaviors;

import client.GUI.Window;
import server.state.Canvas;
import server.state.GraphicalObject;

import java.util.ArrayList;

/**
 * RMIBehavior uses RMI to interact with the server. In this case, it has a
 * stub object of the Canvas, and uses RMI to make changes to the server.
 */
public class RMIBehavior implements Behavior {

    // RMI Canvas object
    private Canvas canvas;

    // ID of the client
    private long userID;

    // Whether or not the user is looking at the canvas (vs a snapshot)
    private boolean displayCanvas;

    // Current version of canvas, used for repainting on update
    private long currentVersion;

    public RMIBehavior(Canvas c) {
        canvas = c;
        userID = c.registerNewUser();
        displayCanvas = true;
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
    public void saveSnapshot() {
        canvas.saveSnapshot(userID);
    }

    @Override
    public void loadSnapshot() {
        displayCanvas = false;
    }

    @Override
    public void loadCurrentCanvas() {
        displayCanvas = true;
    }

    @Override
    public boolean isCanvasMode() {
        return displayCanvas;
    }

    @Override
    public long getId() {
        return userID;
    }

    @Override
    public ArrayList<GraphicalObject> getGraphicalObjects() {
        return displayCanvas ? canvas.getShapeList() : canvas.getSnapshot(userID);
    }

    /**
     * Nothing to do with RMI object
     */
    @Override
    public void disconnect() {
        // Do Nothing
    }

    /**
     * Listens for updates by checking the current version of the canvas. When
     * the client is not up to date, it refreshes it's version number and repaints
     * the list.
     * @param w     The Window that it needs to let know when it is updated
     */
    @Override
    public void listenForUpdates(Window w) {
        //noinspection InfiniteLoopStatement
        while (true) {
            if (displayCanvas && currentVersion != canvas.getVersionNumber()) {
                currentVersion = canvas.getVersionNumber();
                w.tellToRepaint();
            }
        }
    }


}
