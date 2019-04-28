package server.state;

import server.Socket.Drawer;

import java.net.Socket;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The Canvas is the state of the server. It holds all the shapes
 * currently added by users, generates new user IDs for clients,
 * keeps track of it's version, holds socket connections, and
 * can make calls to the Snapshot Manager.
 */
public class Canvas implements Remote {

    // AtomicLong to generate new user ID atomically
    private volatile AtomicLong userIdGenerator;

    // AtomicLong to update version number atomically
    private volatile AtomicLong versionNumber;

    // List of all shapes on server
    private ArrayList<GraphicalObject> shapeList;

    // List of all in-use socket connections
    private ArrayList<Drawer> socketConnections;

    // Snapshot services
    private SnapshotSaver snapshotSaver;

    public Canvas() {
        userIdGenerator = new AtomicLong(0);
        versionNumber = new AtomicLong(0);

        shapeList = new ArrayList<>();
        socketConnections = new ArrayList<>();

        // Load snapshots
        snapshotSaver = new SnapshotSaver();
        // Remove all (if any) socket connections on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Removing all connections");
            socketConnections.forEach(this::removeSocketConnection);
        }));
    }

    /**
     * Generates a new unique user ID for client
     * @return new user ID
     */
    public long registerNewUser() {
        return userIdGenerator.incrementAndGet();
    }

    /**
     * @return version number of the canvas
     */
    public long getVersionNumber() {
        return versionNumber.get();
    }

    /**
     * Generates and adds a new Socket connection to the list of drawers.
     * @param socket socket connection
     * @return a Drawer for the socket
     */
    public Drawer newSocketConnection(Socket socket) {
        long idValue = registerNewUser();
        Drawer newDrawer = new Drawer(socket, idValue, this);
        socketConnections.add(newDrawer);
        System.out.println("New socket connection: " + newDrawer);
        return newDrawer;
    }

    /**
     * Removes a Drawer from the list of socket connections
     * @param drawer drawer to remove from list
     */
    public void removeSocketConnection(Drawer drawer) {
        System.out.println("Disconnecting socket: " + drawer);
        socketConnections.remove(drawer);
    }

    /**
     * Adds a shape to the shape list
     * @param go The shape being added
     */
    public synchronized void addShape(GraphicalObject go) {
        shapeList.add(go);
        tellAllDrawers("ADDED " + go.getID() + ":" + go.toString());
        versionNumber.incrementAndGet();
    }

    /**
     * Removes all shapes from the shape list
     */
    public synchronized void removeAll() {
        shapeList.clear();
        tellAllDrawers("REMOVED_ALL");
        versionNumber.incrementAndGet();
    }

    /**
     * Removes all shapes with ID from the shape list
     * @param ID ID of shapes to be removed
     */
    public synchronized void removeAll(long ID) {
        shapeList.removeIf(go -> go.getID() == ID);
        tellAllDrawers("REMOVED_FROM " + ID);
        versionNumber.incrementAndGet();
    }

    /**
     * Removes the last shape with ID from the shape list (if one exists)
     * @param ID ID of the shape to be removed
     */
    public synchronized void undo(long ID) {
        for (int i = shapeList.size() - 1; i >= 0; i--) {
            if (shapeList.get(i).getID() == ID) {
                shapeList.remove(i);
                break;
            }
        }
        tellAllDrawers("UNDID " + ID);
        versionNumber.incrementAndGet();
    }

    /**
     * @return The list of shapes drawn by all clients
     */
    public synchronized ArrayList<GraphicalObject> getShapeList() {
        return shapeList;
    }

    /**
     * Calls the SnapshotSaver saveSnapshot method
     * @param ID ID of user saving the snapshot
     */
    public synchronized void saveSnapshot(long ID) {
        snapshotSaver.saveSnapshot(ID, shapeList);
    }

    /**
     * Calls the SnapshotSaver retrieveSnapshot method
     * @param ID ID of user retrieving the snapshot
     * @return The list of shapes in the snapshot
     */
    public ArrayList<GraphicalObject> getSnapshot(long ID) {
        return snapshotSaver.retrieveSnapshot(ID);
    }

    /**
     * Sends a message out to all socket connections to inform them of
     * changes made on the server.
     * @param message message to send to all socket connections
     */
    private void tellAllDrawers(String message) {
        socketConnections.forEach(d -> d.tell(message));
    }
}
