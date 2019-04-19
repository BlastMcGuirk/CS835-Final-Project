package server.Socket.operations;

import server.state.ShapeList;

public class RemoveOperation extends Operation {

    private long ID;

    public RemoveOperation() {
        // Removes all shapes
        this.ID = 0;
    }

    public RemoveOperation(long ID) {
        // Remove shapes with ID
        this.ID = ID;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void apply(ShapeList c) {
        if (ID == 0) {
            // Remove all
            // c.removeAll();
        } else {
            // Remove with ID
            // c.removeAll(ID);
        }
    }
}
