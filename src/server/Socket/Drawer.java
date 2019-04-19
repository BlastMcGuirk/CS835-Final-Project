package server.Socket;

import server.state.Canvas;
import server.Socket.operations.AddOperation;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Drawer implements Runnable {

    // Drawer information
    private long ID;
    private Canvas canvas;

    // Socket stuff
    private Socket socket;
    private Scanner input;
    private PrintWriter output;

    public Drawer(Socket socket, long ID, Canvas canvas) {
        this.socket = socket;
        this.ID = ID;
        this.canvas = canvas;
    }

    @Override
    public void run() {
        try {
            setup();
            processCommands();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void processCommands() {
        while(input.hasNextLine()) {
            var command = input.nextLine();
            if (command.startsWith("LEAVE")) {
                return;
            } else if (command.startsWith("ADD")){
                AddOperation addOp = new AddOperation(ID, command.substring(4));
                if (addOp.isValid()) {
                    canvas.requestOperation(addOp);
                }
            }
        }
    }

    private void setup() throws IOException {
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);
    }
}
