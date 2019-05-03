package client.behaviors;

import client.GUI.Window;
import server.state.GraphicalObject;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

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
 */
public class SocketBehavior implements Behavior{

    // Socket connection and input/output
    private Socket socket;
    private Scanner in;
    private PrintWriter out;

    // List of shapes being drawn (can be canvas or snapshot)
    private ArrayList<GraphicalObject> goList;
    private Timer markerTimer;

    // ID of the client
    private long userID;

    // Whether or not the user is looking at the canvas (vs a snapshot)
    private boolean displayCanvas;

    public SocketBehavior(Socket socket, Scanner in, PrintWriter out) {
        this.socket = socket;
        this.in = in;
        this.out = out;

        goList = new ArrayList<>();
        markerTimer = new Timer();

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
    public ArrayList<GraphicalObject> getGraphicalObjects() {
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
                        long idValue = Long.parseLong(shapeValues[0]);
                        goList.add(new GraphicalObject(idValue, shapeValues[1]));
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
                        long idValue = Long.parseLong(responseValues[0]);
                        GraphicalObject go = new GraphicalObject(idValue, responseValues[1]);
                        go.setMarked(true);
                        for (int i = goList.size() - 1; i >= 0; i--) {
                            GraphicalObject removeMarkerGO = goList.get(i);
                            if (removeMarkerGO.getID() == go.getID()) {
                                removeMarkerGO.setMarked(false);
                            }
                        }
                        markerTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                go.setMarked(false);
                                w.tellToRepaint();
                            }
                        }, 3000);
                        goList.add(go);
                    }
                    // All shapes with specified ID were removed from canvas
                    else if (response.startsWith("REMOVED_FROM")) {
                        long idValue = Long.parseLong(response.substring(13));
                        goList.removeIf(go -> go.getID() == idValue);
                    }
                    // All shapes were removed from canvas
                    else if (response.startsWith("REMOVED_ALL")) {
                        goList.clear();
                    }
                    // Last shape added by specified ID was removed
                    else if (response.startsWith("UNDID")) {
                        long idValue = Long.parseLong(response.substring(6));
                        for (int i = goList.size() - 1; i >= 0; i--) {
                            if (goList.get(i).getID() == idValue) {
                                goList.remove(i);
                                break;
                            }
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
