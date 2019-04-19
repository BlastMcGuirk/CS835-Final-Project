package server.RMI;

import server.state.Canvas;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIService {

    private static Registry registry;

    public static void start(int port, Canvas canvas) throws RemoteException {
        if (registry != null) {
            // Registry is running, throw exception
        }
        registry = java.rmi.registry.LocateRegistry.getRegistry(port);
        registry.list();
        UnicastRemoteObject.exportObject(canvas, 0);
        registry.rebind("Canvas", canvas);
    }

}
