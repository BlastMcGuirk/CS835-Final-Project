package client.behaviors;

import client.GUI.Window;
import server.state.CanvasInterface;
import server.state.GraphicalObject;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RMIBehavior uses RMI to interact with the server. In this case, it has a
 * stub object of the Canvas, and uses RMI to make changes to the server.
 */
public class RMIBehavior implements Behavior {

    // RMI Canvas object
    private CanvasInterface canvas;

    // ID of the client
    private long userID;

    // Whether or not the user is looking at the canvas (vs a snapshot)
    private boolean displayCanvas;

    // If the window should refresh the image (e.g., on snapshot load)
    private boolean refreshImage;

    // Current version of canvas, used for repainting on update
    private long currentVersion;

    public RMIBehavior(CanvasInterface c) {
        canvas = c;
        try {
            userID = c.registerNewUser();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        displayCanvas = true;
        refreshImage = false;
        currentVersion = 0;
    }

    @Override
    public void addShape(GraphicalObject go) {
        try {
            canvas.addShape(go);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeYours() {
        try {
            canvas.removeAll(userID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAll() {
        try {
            canvas.removeAll();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void undo() {
        try {
            canvas.undo(userID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveSnapshot() {
        try {
            canvas.saveSnapshot(userID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadSnapshot() {
        displayCanvas = false;
        refreshImage = true;
    }

    @Override
    public void loadCurrentCanvas() {
        displayCanvas = true;
        refreshImage = true;
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
    public ConcurrentHashMap<Long, GraphicalObject> getGraphicalObjects() {
        try {
            return displayCanvas ? canvas.getShapeList() : canvas.getSnapshot(userID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.err.println("Cannot get shapes");
        return new ConcurrentHashMap<>();
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
            try {
                if (refreshImage) {
                    refreshImage = false;
                    w.tellToRepaint();
                } else if (displayCanvas && currentVersion != canvas.getVersionNumber()) {
                    currentVersion = canvas.getVersionNumber();
                    w.tellToRepaint();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


}
