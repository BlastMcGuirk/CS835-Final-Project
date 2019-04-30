package server.RMI;

import server.state.Canvas;
import server.state.CanvasInterface;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Starts and stops the RMI service.
 */
public class RMIService {

    private static final String BINDING_NAME = "Canvas";
    private static Registry registry;

    /**
     * Starts the RMI service
     * @param port port number to run RMI
     * @param canvas Canvas object being exported
     */
    public static void start(int port, CanvasInterface canvas) throws RemoteException {
        if (registry != null) {
            // Registry is running, throw exception
            throw new IllegalStateException("RMI registry already running");
        }
        registry = java.rmi.registry.LocateRegistry.createRegistry(port);
        registry.list();
        UnicastRemoteObject.exportObject(canvas, 0);
        registry.rebind(BINDING_NAME, canvas);
    }

    /**
     * Stops the RMI service
     * @param canvas Canvas object to un-export
     */
    public static void stop(CanvasInterface canvas) {
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
