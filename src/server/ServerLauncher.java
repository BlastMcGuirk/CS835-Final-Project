package server;

import server.RMI.RMIService;
import server.Socket.SocketService;
import server.state.Canvas;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.RemoteException;

public class ServerLauncher {

    public static final int RMI_PORT = 1099;
    public static final int SOCKET_PORT = 1100;

    public static void main(String[] args) {
        // args[1]:
        //		1 = RMI
        //		2 = Socket
        //		3 = Both
        Canvas canvas = new Canvas();

        int code = Integer.parseInt(args[0]);
        if (code == 1 || code == 3) {
            // Start RMI service in new thread
            Thread RMIThread = new Thread(() -> {
                System.out.println("Starting RMI service...");
                try {
                    RMIService.start(RMI_PORT, canvas);
                    System.out.println("RMI service running.");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
            RMIThread.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> RMIService.stop(canvas)));
        }
        if (code == 2 || code == 3) {
            // Start Socket service in new thread
            Thread SocketThread = new Thread(() -> {
                System.out.println("Starting Socket service...");
                try (var listener = new ServerSocket(SOCKET_PORT)) {
                    System.out.println("Socket service running:");
                    System.out.println("    Host: " + listener.getInetAddress().getHostName());
                    System.out.println("    Port: " + SOCKET_PORT);
                    SocketService.start(listener, canvas);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            SocketThread.start();
        }
    }

}
