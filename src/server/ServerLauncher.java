package server;

import server.RMI.RMIService;
import server.state.Canvas;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.util.concurrent.Executors;

public class ServerLauncher {

    public static void main(String[] args) {
        // args[1]:
        //		1 = RMI
        //		2 = Socket
        //		3 = Both
        Canvas canvas = new Canvas();

        int code = Integer.parseInt(args[1]);
        if (code == 1) {
            // Start RMI service in new thread
            Thread t = new Thread(() -> {
                System.out.println("Starting RMI service...");
                int port = 1099;
                try {
                    RMIService.start(port, canvas);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }
        if (code == 2) {
            // Start Socket service in new thread
            Thread t = new Thread(() -> {
                int port = 1100;
                try (var listener = new ServerSocket(port)) {
                    System.out.println("Canvas server is running...");
                    var pool = Executors.newFixedThreadPool(200);
                    while (true) {
                        // new connection
                        listener.accept();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
