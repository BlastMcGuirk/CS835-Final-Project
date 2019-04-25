package server.Socket;

import server.state.Canvas;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class SocketService {

    // Give operations based on commands received
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
