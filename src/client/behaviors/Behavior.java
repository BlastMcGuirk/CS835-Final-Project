package client.behaviors;

import client.GUI.Window;
import server.state.GraphicalObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The Behavior interface is used to specify what the client should do when
 * the user clicks on a GUI element. For example, in a SocketBehavior, the
 * client should send a message via socket to the server.
 */
public interface Behavior {

    /**
     * Adds a shape to the server
     * @param go    The shape being added
     */
    void addShape(GraphicalObject go);

    void editShape(GraphicalObject go, GraphicalObject.ShapeType type, String color, int width, int height);

    /**
     * Removes all of client's shapes from server
     */
    void removeYours();

    /**
     * Removes all shapes from server
     */
    void removeAll();

    /**
     * Saves a snapshot of the current server, saves it on server
     */
    void saveSnapshot();

    /**
     * Loads client's snapshot from server
     */
    void loadSnapshot();

    /**
     * Load's current state of server
     */
    void loadCurrentCanvas();

    /**
     * @return Whether or not the client is viewing the current state of the
     * server (vs a snapshot)
     */
    boolean isCanvasMode();

    /**
     * @return The client's ID
     */
    long getId();

    /**
     * This can be one of two lists. Either the current state of the server,
     * or a snapshot
     * @return Current working GraphicalObject list
     */
    ConcurrentHashMap<Long, GraphicalObject> getGraphicalObjects();

    /**
     * Used as a shutdown hook, it disconnects from the server. Not all
     * Behavior implementations should do anything, but it is here for
     * the ones that do (SocketBehavior)
     */
    void disconnect();

    /**
     * Listens for updates from the server. Updates are when the state of the
     * server changes. This could mean adding/removing a shape, or listening
     * for socket messages.
     * @param w     The Window that it needs to let know when it is updated
     */
    void listenForUpdates(Window w);

}
