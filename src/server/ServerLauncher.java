package server;

import server.RMI.RMIService;
import server.Socket.SocketService;
import server.simulation.GhostClient;
import server.state.Canvas;
import server.state.CanvasInterface;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerLauncher {

    public static final int RMI_PORT = 1099;
    public static final int SOCKET_PORT = 1100;

    public static void main(String[] args) {
        // args[0]:
        //		1 = RMI
        //		2 = Socket
        //		3 = Both
        // args[1]:
        //      # of ghost clients to start
        // args[2]:
        //      % activity from ghost clients

        CanvasInterface canvas = new Canvas();
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(200);

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
                    SocketService.start(threadPool, listener, canvas);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            SocketThread.start();
        }

        // Launch Ghost Clients
        int numGhostClients = Integer.parseInt(args[1]);
        int activityPercentage = Integer.parseInt(args[2]);
        for (int i = 0; i < numGhostClients; i++) {
            GhostClient gc = new GhostClient(canvas, activityPercentage);
            threadPool.submit(gc);
        }


        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your commands");
        Canvas c = (Canvas) canvas;
        while (scanner.hasNextLine()) {
            String cmd = scanner.nextLine();
            if (cmd.startsWith("ban")) {
                long id = Long.parseLong(cmd.substring(4));
                c.ban(id);
            } else if (cmd.startsWith("unban")) {
                long id = Long.parseLong(cmd.substring(6));
                c.unban(id);
            } else if (cmd.startsWith("dump")) {
                c.dumpStateToFile();
            } else if (cmd.startsWith("erase snapshots")) {
                c.eraseSnapshots();
            }
        }
    }

}
