package client.behaviors;

import client.GUI.Window;
import server.state.GraphicalObject;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class SocketBehavior implements Behavior{

    /**
     * Socket variables.
     * The socket connection uses a protocol defined below:
     *      The client can send the following messages to the server:
     *          ADD <GO>
     *          REMOVE_MINE
     *          REMOVE_ALL
     *          UNDO
     *          EXIT
     *
     *      The server can send the following messages to the client:
     *          WELCOME <ID>
     *          ADDED <ID> <GO>
     *          REMOVED_FROM <ID>
     *          REMOVED_ALL
     *          UNDID <ID>
     */
    private Socket socket;
    private Scanner in;
    private PrintWriter out;

    private ArrayList<GraphicalObject> goList;

    private long userID;

    public SocketBehavior(Socket socket, Scanner in, PrintWriter out) {
        this.socket = socket;
        this.in = in;
        this.out = out;

        goList = new ArrayList<>();

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
    public long getId() {
        return userID;
    }

    @Override
    public ArrayList<GraphicalObject> getGraphicalObjects() {
        return goList;
    }

    @Override
    public void listenForUpdates(Window w) {
        try {
            while (in.hasNextLine()) {
                String response = in.nextLine();
                if (response.startsWith("ADDED")) {
                    String[] responseValues = response.split(" ");
                    long idValue = Long.parseLong(responseValues[1]);
                    String graphicalObject = responseValues[2] + " " // ShapeType
                            + responseValues[3] + " " // Color
                            + responseValues[4] + " " // Width
                            + responseValues[5] + " " // Height
                            + responseValues[6] + " " // x position
                            + responseValues[7];    // y position
                    goList.add(new GraphicalObject(idValue, graphicalObject));
                } else if (response.startsWith("REMOVED_FROM")) {
                    long idValue = Long.parseLong(response.substring(13));
                    for (GraphicalObject go : goList) {
                        if (go.getID() == idValue) {
                            goList.remove(go);
                        }
                    }
                } else if (response.startsWith("REMOVED_ALL")) {
                    goList.clear();
                } else if (response.startsWith("UNDID")) {
                    long idValue = Long.parseLong(response.substring(6));
                    for (int i = goList.size() - 1; i >= 0; i--) {
                        if (goList.get(i).getID() == idValue) {
                            goList.remove(i);
                            break;
                        }
                    }
                }
                w.tellToRepaint();
            }
            out.println("EXIT");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}
