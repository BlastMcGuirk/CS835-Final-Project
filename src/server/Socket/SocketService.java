package server.Socket;

import server.state.CanvasInterface;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

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
    public static void start(ThreadPoolExecutor threadPool, ServerSocket listener, CanvasInterface c) throws IOException {
        //noinspection InfiniteLoopStatement
        while (true) {
            // new connection
            Socket socket = listener.accept();
            Drawer drawer = c.newSocketConnection(socket);
            threadPool.submit(drawer);
        }
    }

}
