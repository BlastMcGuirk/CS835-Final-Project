package server.state;

import java.rmi.Remote;

public interface ShapeList extends Remote {

    void add(GraphicalObject go);
    void remove();
    void remove(long id);
    void undo(long id);

}
