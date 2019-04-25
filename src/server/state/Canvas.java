package server.state;

import server.Socket.Drawer;

import java.net.Socket;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Canvas implements Remote {

    private volatile AtomicLong userIdGenerator;
    private volatile AtomicLong versionNumber;

    private ArrayList<GraphicalObject> shapeList;
    private ArrayList<Drawer> drawers;

    public Canvas() {
        userIdGenerator = new AtomicLong(0);
        versionNumber = new AtomicLong(0);

        shapeList = new ArrayList<>();
        drawers = new ArrayList<>();
    }

    public long registerNewUser() {
        return userIdGenerator.incrementAndGet();
    }

    public long getVersionNumber() {
        return versionNumber.get();
    }

    public Drawer newSocketConnection(Socket socket) {
        long idValue = registerNewUser();
        Drawer newDrawer = new Drawer(socket, idValue, this);
        drawers.add(newDrawer);
        System.out.println("New socket connection: " + newDrawer);
        return newDrawer;
    }

    public void removeSocketConnection(Drawer drawer) {
        System.out.println("Disconnecting socket: " + drawer);
        drawers.remove(drawer);
    }

    public synchronized void addShape(GraphicalObject go) {
        // add shape to shapeList
        shapeList.add(go);
        tellAllDrawers("ADDED " + go.getID() + " " + go.toString());
        versionNumber.incrementAndGet();
    }

    public synchronized void removeAll() {
        // remove all shapes from shapeList
        shapeList.clear();
        tellAllDrawers("REMOVED_ALL");
        versionNumber.incrementAndGet();
    }

    public synchronized void removeAll(long ID) {
        // remove all shapes with ID from shapeList
        shapeList.removeIf(go -> go.getID() == ID);
        tellAllDrawers("REMOVED_FROM " + ID);
        versionNumber.incrementAndGet();
    }

    public void undo(long ID) {
        // remove last shape from shapeList with ID
        for (int i = shapeList.size() - 1; i >= 0; i--) {
            if (shapeList.get(i).getID() == ID) {
                shapeList.remove(i);
                break;
            }
        }
        tellAllDrawers("UNDID " + ID);
        versionNumber.incrementAndGet();
    }

    public ArrayList<GraphicalObject> getShapeList() {
        return shapeList;
    }

    private void tellAllDrawers(String message) {
        drawers.forEach(d -> d.tell(message));
    }
}
