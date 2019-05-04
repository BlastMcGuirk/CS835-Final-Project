package client.behaviors;

import client.GUI.Window;
import server.state.GraphicalObject;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SocketBehavior interacts with the server using a socket connection. It
 * uses the following protocol:
 *      The client can send the following messages to the server:
 *          ADD <GO>
 *          REMOVE_MINE
 *          REMOVE_ALL
 *          UNDO
 *          SAVE_SNAPSHOT
 *          LOAD_SNAPSHOT
 *          LOAD_CANVAS
 *          EXIT
 *
 *      The server can send the following messages to the client:
 *          WELCOME <ID>
 *          ADDED <ID>:<GO>
 *          REMOVED_FROM <ID>
 *          REMOVED_ALL
 *          UNDID <ID>
 *          GETTING_SNAPSHOT <# GOs>
 *          GETTING_CANVAS <# GOs>
 *          SH <ID>:<GO>
 *          MARK <SHAPE_ID> <CLIENT_ID>
 *          UNMARK <SHAPE_ID> <CLIENT_ID>
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
        out.println("ADD " + go.toString());
    }

    @Override
    public void removeYours() {
        out.println("REMOVE_MINE");
    }

    @Override
    public void removeAll() {
        out.println("REMOVE_ALL");
    }

    @Override
    public void undo() {
        out.println("UNDO");
    }

    @Override
    public void saveSnapshot() {
        out.println("SAVE_SNAPSHOT");
    }

    @Override
    public void loadSnapshot() {
        out.println("LOAD_SNAPSHOT");
    }

    @Override
    public void loadCurrentCanvas() {
        out.println("LOAD_CANVAS");
    }

    @Override
    public boolean isCanvasMode() {
        return displayCanvas;
    }

    @Override
    public long getId() {
        return userID;
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
        System.out.println("Disconnecting");
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
                        goList.put(shapeID, new GraphicalObject(clientID, shapeValues[1]));
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
                    }
                    // Last shape added by specified ID was removed
                    else if (response.startsWith("UNDID")) {
                        long idValue = Long.parseLong(response.substring(6));
                        for (int i = goList.size() - 1; i >= 0; i--) {
                            if (goList.get(i).getClientID() == idValue) {
                                goList.remove(i);
                                break;
                            }
                        }
                    } else if (response.startsWith("MARK")) {
                        String[] responseValues = response.substring(5).split(":");
                        long shapeID = Long.parseLong(responseValues[0]);
                        long clientID = Long.parseLong(responseValues[1]);
                        goList.get(shapeID).setMarked(true);
                    } else if (response.startsWith("UNMARK")) {
                        String[] responseValues = response.substring(7).split(":");
                        long shapeID = Long.parseLong(responseValues[0]);
                        long clientID = Long.parseLong(responseValues[1]);
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
