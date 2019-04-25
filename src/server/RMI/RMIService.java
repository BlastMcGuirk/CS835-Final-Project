package server.RMI;

import server.state.Canvas;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIService {

    private static final String BINDING_NAME = "Canvas";
    private static Registry registry;

    public static void start(int port, Canvas canvas) throws RemoteException {
        if (registry != null) {
            // Registry is running, throw exception
            throw new IllegalStateException("RMI registry already running");
        }
        registry = java.rmi.registry.LocateRegistry.getRegistry(port);
        registry.list();
        UnicastRemoteObject.exportObject(canvas, 0);
        registry.rebind(BINDING_NAME, canvas);
    }

    public static void stop(Canvas canvas) {
        if (registry == null) {
            // No registry running, throw exception
            throw new IllegalStateException("Server not running");
        }
        System.out.println("Shutting down RMI service...");
        try {
            registry.unbind(BINDING_NAME);
            registry = null;
            UnicastRemoteObject.unexportObject(canvas, false);
        } catch (Exception ignored) {}
    }

}
