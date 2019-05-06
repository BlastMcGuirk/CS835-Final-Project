package client.behaviors;

import client.GUI.Window;
import server.state.GraphicalObject;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SocketBehavior interacts with the server using a socket connection. It
 * uses the following protocol:
 *      The client can send the following messages to the server:
 *          ADD <GO>
 *          EDIT <S#>:<GO>
 *          REMOVE_MINE
 *          REMOVE_ALL
 *          SAVE_SNAPSHOT
 *          LOAD_SNAPSHOT
 *          LOAD_CANVAS
 *          EXIT
 *
 *      The server can send the following messages to the client:
 *          WELCOME <ID>
 *          ADDED <S#>:<ID>:<GO>
 *          EDITED <S#>:<ID>:<GO>
 *          REMOVED_FROM <ID>
 *          REMOVED_ALL
 *          GETTING_SNAPSHOT <# GOs>
 *          GETTING_CANVAS <# GOs>
 *          SH <S#>:<ID>:<GO>
 *          MARK <SHAPE_ID>:<CLIENT_ID>
 *          UNMARK <SHAPE_ID>:<CLIENT_ID>
 */
public class SocketBehavior implements Behavior{

    // Socket connection and input/output
    private Socket socket;
    private Scanner in;
    private PrintWriter out;

    // List of shapes being drawn (can be canvas or snapshot)
    private ConcurrentHashMap<Long, GraphicalObject> goList;

    // ID of the client
    private long userID;

    // Whether or not the user is looking at the canvas (vs a snapshot)
    private boolean displayCanvas;

    public SocketBehavior(Socket socket, Scanner in, PrintWriter out) {
        this.socket = socket;
        this.in = in;
        this.out = out;

        goList = new ConcurrentHashMap<>();

        displayCanvas = true;

        // Get userID from WELCOME message
        String firstResponse = in.nextLine();
        userID = Long.parseLong(firstResponse.substring(8));
    }

    @Override
    public void addShape(GraphicalObject go) {
        String message = "ADD " + go.toString();
        outputMessage(message);
        out.println(message);
    }

    @Override
    public void editShape(GraphicalObject go, GraphicalObject.ShapeType type, String color, int width, int height) {
        String message = "EDIT " + go.getShapeID() + ":" + type + " " + color + " " + width + " " + height;
        outputMessage(message);
        out.println(message);
    }

    @Override
    public void removeYours() {
        String message = "REMOVE_MINE";
        outputMessage(message);
        out.println(message);
    }

    @Override
    public void removeAll() {
        String message = "REMOVE_ALL";
        outputMessage(message);
        out.println(message);
    }

    @Override
    public void saveSnapshot() {
        String message = "SAVE_SNAPSHOT";
        outputMessage(message);
        out.println(message);
    }

    @Override
    public void loadSnapshot() {
        String message = "LOAD_SNAPSHOT";
        outputMessage(message);
        out.println(message);
    }

    @Override
    public void loadCurrentCanvas() {
        String message = "LOAD_CANVAS";
        outputMessage(message);
        out.println(message);
    }

    @Override
    public boolean isCanvasMode() {
        return displayCanvas;
    }

    @Override
    public long getId() {
        return userID;
    }

    private void outputMessage(String m) {
        System.out.println("  ---> " + m);
    }

    /**
     * Only one list to work with. It is either the canvas or the snapshot at
     * any given time.
     * @return The current list of shapes to be drawn
     */
    @Override
    public ConcurrentHashMap<Long, GraphicalObject> getGraphicalObjects() {
        return goList;
    }

    /**
     * On disconnect, send the message EXIT to server so it knows to close
     * it's socket.
     */
    @Override
    public void disconnect() {
        // Run as a shutdown hook
        System.out.println(" ---> EXIT");
        out.println("EXIT");
    }

    /**
     * Listens for messages from the server via socket connection.
     * @param w     The Window that it needs to let know when it is updated
     */
    @Override
    public void listenForUpdates(Window w) {
        try {
            while (in.hasNextLine()) {
                // Get next line
                String response = in.nextLine();
                System.out.println("<--- " + response);

                // If it starts with GETTING, it's switching between canvas and snapshot
                if (response.startsWith("GETTING")) {
                    displayCanvas = response.startsWith("GETTING_CANVAS");
                    // clear the list in prep for new list
                    goList.clear();
                    int numToGet = displayCanvas ?
                            Integer.parseInt(response.substring(15)) :
                            Integer.parseInt(response.substring(17));
                    // These shapes are sent sequentially, and are a list of either the snapshot or the canvas
                    for (int i = 0; i < numToGet && in.hasNextLine(); i++) {
                        String newShape = in.nextLine();
                        String[] shapeValues = newShape.substring(3).split(":");
                        long shapeID = Long.parseLong(shapeValues[0]);
                        long clientID = Long.parseLong(shapeValues[1]);
                        GraphicalObject newGO = new GraphicalObject(clientID, shapeValues[2]);
                        newGO.setShapeID(shapeID);
                        goList.put(shapeID, newGO);
                    }
                    // Repaint the canvas
                    w.tellToRepaint();
                }

                // The following commands are only read if the user is displaying the canvas.
                // If the user is looking at a snapshot, nothing should be updated until
                // switching back to the canvas.
                if (displayCanvas) {
                    // New shape was added to canvas
                    if (response.startsWith("ADDED")) {
                        String[] responseValues = response.substring(6).split(":");
                        long shapeID = Long.parseLong(responseValues[0]);
                        long clientID = Long.parseLong(responseValues[1]);
                        GraphicalObject go = new GraphicalObject(clientID, responseValues[2]);
                        go.setShapeID(shapeID);
                        goList.put(shapeID, go);
                    }
                    // Shape was edited
                    else if (response.startsWith("EDITED")) {
                        String[] responseValues = response.substring(7).split(":");
                        long shapeID = Long.parseLong(responseValues[0]);
                        long clientID = Long.parseLong(responseValues[1]);
                        GraphicalObject go = new GraphicalObject(clientID, responseValues[2]);
                        go.setShapeID(shapeID);
                        goList.put(shapeID, go);
                    }
                    // All shapes with specified ID were removed from canvas
                    else if (response.startsWith("REMOVED_FROM")) {
                        long idValue = Long.parseLong(response.substring(13));
                        goList.entrySet().removeIf(e -> e.getValue().getClientID() == idValue);
                    }
                    // All shapes were removed from canvas
                    else if (response.startsWith("REMOVED_ALL")) {
                        goList.clear();
                    } else if (response.startsWith("MARK")) {
                        String[] responseValues = response.substring(5).split(":");
                        long shapeID = Long.parseLong(responseValues[0]);
                        goList.get(shapeID).setMarked(true);
                    } else if (response.startsWith("UNMARK")) {
                        String[] responseValues = response.substring(7).split(":");
                        long shapeID = Long.parseLong(responseValues[0]);
                        GraphicalObject markedGo = goList.get(shapeID);
                        if (markedGo != null) {
                            markedGo.setMarked(false);
                        }
                    }
                    // Repaint the canvas
                    w.tellToRepaint();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}
