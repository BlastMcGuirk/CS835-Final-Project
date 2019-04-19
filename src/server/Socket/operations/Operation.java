package server.Socket.operations;

import server.state.ShapeList;

public abstract class Operation {

    public abstract boolean isValid();
    public abstract void apply(ShapeList c);

}
