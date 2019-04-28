package server.Socket;

import server.state.Canvas;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Starts the socket service.
 */
public class SocketService {

    /**
     * Waits for a new connection, makes a Drawer and starts running it
     * @param listener The server waiting for connections
     * @param c The shared Canvas object
     * @throws IOException on socket error
     */
    public static void start(ServerSocket listener, Canvas c) throws IOException {
        var pool = Executors.newFixedThreadPool(200);
        //noinspection InfiniteLoopStatement
        while (true) {
            // new connection
            Socket socket = listener.accept();
            Drawer drawer = c.newSocketConnection(socket);
            pool.submit(drawer);
        }
    }

}
