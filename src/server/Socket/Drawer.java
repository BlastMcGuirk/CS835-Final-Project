package server.Socket;

import server.state.Canvas;
import server.state.GraphicalObject;

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
            try {
                canvas.removeSocketConnection(this);
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    public void tell(String message) {
        output.println(message);
    }

    private void processCommands() {
        while(input.hasNextLine()) {
            var command = input.nextLine();
            if (command.startsWith("LEAVE")) {
                return;
            } else if (command.startsWith("ADD")){
                GraphicalObject go = new GraphicalObject(ID, command.substring(4));
                canvas.addShape(go);
            } else if (command.startsWith("REMOVE_MINE")) {
                canvas.removeAll(ID);
            } else if (command.startsWith("REMOVE_ALL")) {
                canvas.removeAll();
            } else if (command.startsWith("UNDO")) {
                canvas.undo(ID);
            }
        }
    }

    private void setup() throws IOException {
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);

        output.println("WELCOME " + ID);
        for (GraphicalObject go : canvas.getShapeList()) {
            output.println("ADDED " + go.getID() + " " + go);
        }
    }

    @Override
    public String toString() {
        return "Drawer ID[" + ID + "]";
    }
}
