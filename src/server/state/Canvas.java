package server.state;

import server.Socket.Drawer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The Canvas is the state of the server. It holds all the shapes
 * currently added by users, generates new user IDs for clients,
 * keeps track of it's version, holds socket connections, and
 * can make calls to the Snapshot Manager.
 */
public class Canvas implements CanvasInterface, Runnable {

    // AtomicLong to generate new user ID atomically
    private AtomicLong userIdGenerator;

    // AtomicLong to update version number atomically
    private AtomicLong versionNumber;

    // AtomicLong to give shapes an ID
    private AtomicLong shapeIDGenerator;

    // List of all shapes on server
    private ConcurrentHashMap<Long, GraphicalObject> shapeMap;

    // Set of banned IDs
    private HashSet<Long> bannedIDs;
    private final Object bannedIDLock;

    // List of markers for last shape placed by client
    private ConcurrentHashMap<Long, GraphicalObject> markerMap;
    private Timer markerTimer;

    // List of all in-use socket connections
    private ArrayList<Drawer> socketConnections;
    private final ConcurrentLinkedQueue<String> socketMessageQueue;
    private Executor exec;
    private volatile boolean isRunning;

    // Snapshot services
    private SnapshotSaver snapshotSaver;

    public Canvas() {
        userIdGenerator = new AtomicLong(0);
        versionNumber = new AtomicLong(0);
        shapeIDGenerator = new AtomicLong(0);

        shapeMap = new ConcurrentHashMap<>();

        bannedIDs = new HashSet<>();
        bannedIDLock = new Object();

        markerMap = new ConcurrentHashMap<>();

        socketConnections = new ArrayList<>();
        socketMessageQueue = new ConcurrentLinkedQueue<>();
        exec = Executors.newFixedThreadPool(10);

        markerTimer = new Timer();

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
    @Override
    public long registerNewUser() {
        return userIdGenerator.incrementAndGet();
    }

    /**
     * @return version number of the canvas
     */
    @Override
    public long getVersionNumber() {
        return versionNumber.get();
    }

    /**
     * Generates and adds a new Socket connection to the list of drawers.
     * @param socket socket connection
     * @return a Drawer for the socket
     */
    @Override
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
    @Override
    public void removeSocketConnection(Drawer drawer) {
        System.out.println("Disconnecting socket: " + drawer);
        socketConnections.remove(drawer);
    }

    /**
     * Adds a shape to the shape list
     * @param go The shape being added
     */
    @Override
    public void addShape(GraphicalObject go) {
        boolean canAddShape;
        synchronized (bannedIDLock) {
            canAddShape = !bannedIDs.contains(go.getClientID());
        }
        if (canAddShape) {
            GraphicalObject oldGO = markerMap.get(go.getClientID());
            if (oldGO != null) {
                oldGO.setMarked(false);
                tellAllDrawers("UNMARK " + oldGO.getShapeID() + ":" + oldGO.getClientID());
            }
            markerMap.put(go.getClientID(), go);

            long shapeID = shapeIDGenerator.incrementAndGet();
            go.setShapeID(shapeID);
            shapeMap.put(shapeID, go);
            go.setMarked(true);
            markerTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    go.run();
                    tellAllDrawers("UNMARK " + shapeID + ":" + go.getClientID());
                    versionNumber.incrementAndGet();
                }
            }, 3000);
            tellAllDrawers("ADDED " + shapeID + ":" + go.getClientID() + ":" + go.toString());
            tellAllDrawers("MARK " + shapeID + ":" + go.getClientID());
            versionNumber.incrementAndGet();
        }
    }

    @Override
    public void editShape(long shapeID, long newClientID, GraphicalObject.ShapeType type, String color, int width, int height) {
        boolean canEditShape;
        synchronized (bannedIDLock) {
            canEditShape = !bannedIDs.contains(newClientID);
        }
        if (canEditShape) {
            GraphicalObject go = shapeMap.get(shapeID);
            assert go != null;
            go.edit(newClientID, type, color, width, height);

            GraphicalObject oldGO = markerMap.get(go.getClientID());
            if (oldGO != null) {
                oldGO.setMarked(false);
                tellAllDrawers("UNMARK " + oldGO.getShapeID() + ":" + oldGO.getClientID());
            }
            markerMap.put(go.getClientID(), go);

            go.setMarked(true);
            markerTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    go.run();
                    tellAllDrawers("UNMARK " + shapeID + ":" + go.getClientID());
                    versionNumber.incrementAndGet();
                }
            }, 3000);
            tellAllDrawers("EDITED " + shapeID + ":" + go.getClientID() + ":" + go.toString());
            tellAllDrawers("MARK " + shapeID + ":" + go.getClientID());
            versionNumber.incrementAndGet();
        }
    }

    /**
     * Removes all shapes from the shape list
     */
    @Override
    public void removeAll(long ID) {
        boolean canRemoveAll;
        synchronized (bannedIDLock) {
            canRemoveAll = !bannedIDs.contains(ID);
        }
        if (canRemoveAll) {
            shapeMap.clear();
            tellAllDrawers("REMOVED_ALL");
            versionNumber.incrementAndGet();
        }
    }

    /**
     * Removes all shapes with ID from the shape list
     * @param ID ID of shapes to be removed
     */
    @Override
    public void removeAllWithID(long ID) {
        boolean canRemoveWithID;
        synchronized (bannedIDLock) {
            canRemoveWithID = !bannedIDs.contains(ID);
        }
        if (canRemoveWithID) {
            shapeMap.entrySet().removeIf(e -> e.getValue().getClientID() == ID);
            tellAllDrawers("REMOVED_FROM " + ID);
            versionNumber.incrementAndGet();
        }
    }

    /**
     * @return The list of shapes drawn by all clients
     */
    @Override
    public ConcurrentHashMap<Long, GraphicalObject> getShapeMap() {
        return shapeMap;
    }

    /**
     * Calls the SnapshotSaver saveSnapshot method
     * @param ID ID of user saving the snapshot
     */
    @Override
    public void saveSnapshot(long ID) {
        snapshotSaver.saveSnapshot(ID, shapeMap);
    }

    /**
     * Calls the SnapshotSaver retrieveSnapshot method
     * @param ID ID of user retrieving the snapshot
     * @return The list of shapes in the snapshot
     */
    @Override
    public ConcurrentHashMap<Long, GraphicalObject> getSnapshot(long ID) {
        return snapshotSaver.retrieveSnapshot(ID);
    }

    /**
     * Sends a message out to all socket connections to inform them of
     * changes made on the server.
     * @param message message to send to all socket connections
     */
    private void tellAllDrawers(String message) {
        synchronized (socketMessageQueue) {
            socketMessageQueue.add(message);
        }
        if (!isRunning) {
            isRunning = true;
            exec.execute(this);
        }
    }

    public void ban(long id) {
        synchronized (bannedIDLock) {
            bannedIDs.add(id);
        }
        System.out.println("Banning user " + id);
    }

    public void unban(long id) {
        synchronized (bannedIDLock) {
            bannedIDs.remove(id);
        }
        System.out.println("Unbanning user " + id);
    }

    public void dumpStateToFile() {
        System.out.println("Dumping state to file...");
        try {
            FileWriter fileWriter = new FileWriter("src/server/state/dumped_state.txt");
            PrintWriter writer = new PrintWriter(fileWriter);

            // Write all shapes to file
            writer.println("Shapes:");
            shapeMap.forEach((shapeID, shape) -> writer.println(shapeID + ":" + shape.getClientID() + ":" + shape));

            // Write all banned users to file
            writer.println("Banned Users:");
            synchronized (bannedIDLock) {
                bannedIDs.forEach((id) -> writer.println("ID: " + id));
            }

            writer.println("Snapshots:");
            snapshotSaver.getMap().forEach((clientID, snapshot) -> {
               writer.println("ID " + clientID);
               snapshot.forEach((shapeID, shape) -> writer.println(shapeID + ":" + shape.getClientID() + ":" + shape));
            });

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done dumping to file!");
    }

    public void eraseSnapshots() {
        snapshotSaver.eraseSnapshots();
    }

    @Override
    public void run() {
        int numToSend = socketMessageQueue.size();
        for (int i = 0; i < numToSend; i++) {
            String messageToSend = socketMessageQueue.poll();
            socketConnections.forEach(sc -> sc.tell(messageToSend));
        }
        synchronized (socketMessageQueue) {
            if (socketMessageQueue.isEmpty()) {
                isRunning = false;
            } else {
                exec.execute(this);
            }
        }
    }
}
