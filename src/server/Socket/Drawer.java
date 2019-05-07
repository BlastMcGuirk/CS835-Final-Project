package server.Socket;

import server.state.Canvas;
import server.state.GraphicalObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Drawer class represents 1 Socket connection. It holds
 * information on what ID it is connected to, socket information,
 * as well as has a copy of the canvas to make changes to it.
 */
public class Drawer implements Runnable {

    // Drawer information
    private long ID;
    private Canvas canvas;

    // Socket stuff
    private Socket socket;
    private Scanner input;
    private PrintWriter output;

    // Output lock
    private final Object lock;

    public Drawer(Socket socket, long ID, Canvas canvas) {
        this.socket = socket;
        this.ID = ID;
        this.canvas = canvas;
        this.lock = new Object();
    }

    /**
     * Starts socket listening
     */
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

    /**
     * Tell sends a message to the client via socket.
     * @param message Message to send to client
     */
    public void tell(String message) {
        // Synchronize on a lock so messages come/go in order
        synchronized (lock) {
            output.println(message);
        }
    }

    /**
     * Listens for input from socket, does something with command.
     */
    private void processCommands() {
        while(input.hasNextLine()) {
            // Synchronize on a lock so messages come/go in order
            synchronized (lock) {
                var command = input.nextLine();
                if (command.startsWith("EXIT")) {
                    // Stops listening for commands so thread can end
                    return;
                } else if (command.startsWith("ADD")) {
                    // Adds shape to canvas
                    GraphicalObject go = new GraphicalObject(ID, command.substring(4));
                    canvas.addShape(go);
                } else if (command.startsWith("EDIT")) {
                    // Edit shape
                    String[] commandArgs = command.substring(5).split(":");
                    String[] shapeArgs = commandArgs[1].split(" ");
                    long shapeID = Long.parseLong(commandArgs[0]);
                    GraphicalObject.ShapeType type = null;
                    switch (shapeArgs[0]) {
                        case "Circle":
                            type = GraphicalObject.ShapeType.Circle;
                            break;
                        case "Triangle":
                            type = GraphicalObject.ShapeType.Triangle;
                            break;
                        case "Rectangle":
                            type = GraphicalObject.ShapeType.Rectangle;
                            break;
                    }
                    int width = Integer.parseInt(shapeArgs[2]);
                    int height = Integer.parseInt(shapeArgs[3]);
                    canvas.editShape(shapeID, ID, type, shapeArgs[1], width, height);
                } else if (command.startsWith("REMOVE_MINE")) {
                    // Removes shapes with client ID
                    canvas.removeAllWithID(ID);
                } else if (command.startsWith("REMOVE_ALL")) {
                    // Removes all shapes from canvas
                    canvas.removeAll(ID);
                } else if (command.startsWith("SAVE_SNAPSHOT")) {
                    // Saves a snapshot of the current state of the canvas
                    System.out.println("Saving snapshot for ID: " + ID);
                    canvas.saveSnapshot(ID);
                } else if (command.startsWith("LOAD_SNAPSHOT")) {
                    // Loads the client's snapshot
                    ConcurrentHashMap<Long, GraphicalObject> snapshotList = canvas.getSnapshot(ID);
                    output.println("GETTING_SNAPSHOT " + snapshotList.size());
                    snapshotList.forEach((shapeID, go) -> output.println("SH " + shapeID + ":" + go.getClientID() + ":" + go));
                } else if (command.startsWith("LOAD_CANVAS")) {
                    // Loads the current state of the canvas
                    ConcurrentHashMap<Long, GraphicalObject> canvasList = canvas.getShapeMap();
                    output.println("GETTING_CANVAS " + canvasList.size());
                    canvasList.forEach((shapeID, go) -> output.println("SH " + shapeID + ":" + go.getClientID() + ":" + go));
                }
            }
        }
    }

    /**
     * Sets up input and output from socket, as well as tells the user it's
     * ID and current state of the canvas.
     * @throws IOException on input/output error
     */
    private void setup() throws IOException {
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);

        output.println("WELCOME " + ID);
        canvas.getShapeMap().forEach((shapeID, go) ->
                output.println("ADDED " + shapeID + ":" + go.getClientID() + ":" + go));
    }

    @Override
    public String toString() {
        return "Drawer ID[" + ID + "]";
    }
}
