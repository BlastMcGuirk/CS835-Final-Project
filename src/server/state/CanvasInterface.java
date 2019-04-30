package server.state;

import server.Socket.Drawer;

import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface CanvasInterface extends Remote {

    long registerNewUser() throws RemoteException;
    long getVersionNumber() throws RemoteException;
    Drawer newSocketConnection(Socket socket) throws RemoteException;
    void removeSocketConnection(Drawer drawer) throws RemoteException;
    void addShape(GraphicalObject go) throws RemoteException;
    void removeAll() throws RemoteException;
    void removeAll(long ID) throws RemoteException;
    void undo(long ID) throws RemoteException;
    ArrayList<GraphicalObject> getShapeList() throws RemoteException;
    void saveSnapshot(long ID) throws RemoteException;
    ArrayList<GraphicalObject> getSnapshot(long ID) throws RemoteException;

}