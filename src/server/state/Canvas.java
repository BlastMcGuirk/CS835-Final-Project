package server.state;

import server.Socket.operations.Operation;

import java.rmi.Remote;

public class Canvas implements Remote {

    private ShapeList shapeList;

    public void addShape(GraphicalObject go) {
        // add shape to shapeList
        shapeList.add(go);
    }

    public void removeAll() {
        // remove all shapes from shapeList
        shapeList.remove();
    }

    public void removeAll(long ID) {
        // remove all shapes with ID from shapeList
        shapeList.remove(ID);
    }

    public void undo(long ID) {
        // remove last shape from shapeList with ID
        shapeList.undo(ID);
    }

    public ShapeList getShapeList() {
        return shapeList;
    }

    // requestOperation
    public void requestOperation(Operation operation) {
        operation.apply(shapeList);
    }
}
