package client.behaviors;

import client.GUI.Window;
import server.state.GraphicalObject;

import java.util.ArrayList;

public interface Behavior {

    void addShape(GraphicalObject go);
    void removeYours();
    void removeAll();
    void undo();
    long getId();
    ArrayList<GraphicalObject> getGraphicalObjects();
    void listenForUpdates(Window w);

}
