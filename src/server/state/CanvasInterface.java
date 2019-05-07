package server.state;

import server.Socket.Drawer;

import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

public interface CanvasInterface extends Remote {

    long registerNewUser() throws RemoteException;
    long getVersionNumber() throws RemoteException;
    Drawer newSocketConnection(Socket socket) throws RemoteException;
    void removeSocketConnection(Drawer drawer) throws RemoteException;
    void addShape(GraphicalObject go) throws RemoteException;
    void editShape(long shapeID, long newClientID, GraphicalObject.ShapeType type, String color, int width, int height) throws RemoteException;
    void removeAll(long ID) throws RemoteException;
    void removeAllWithID(long ID) throws RemoteException;
    ConcurrentHashMap<Long, GraphicalObject> getShapeMap() throws RemoteException;
    void saveSnapshot(long ID) throws RemoteException;
    ConcurrentHashMap<Long, GraphicalObject> getSnapshot(long ID) throws RemoteException;

}
