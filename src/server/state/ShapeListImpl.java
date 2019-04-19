package server.state;

import java.util.ArrayList;

public class ShapeListImpl implements ShapeList {

    private ArrayList<GraphicalObject> list;

    @Override
    public synchronized void add(GraphicalObject go) {
        list.add(go);
    }

    @Override
    public synchronized void remove() {
        list.clear();
    }

    @Override
    public synchronized void remove(long id) {
        for (GraphicalObject go : list) {
            if (go.getID() == id) {
                // remove from list
                list.remove(go);
            }
        }
    }

    @Override
    public synchronized void undo(long id) {

    }
}
