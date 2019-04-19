package server.Socket.operations;

import server.state.ShapeList;

public class UndoOperation extends Operation {

    private long ID;

    public UndoOperation(long ID) {
        this.ID = ID;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void apply(ShapeList sl) {
        // Remove last object with ID
        sl.undo(ID);
    }
}
