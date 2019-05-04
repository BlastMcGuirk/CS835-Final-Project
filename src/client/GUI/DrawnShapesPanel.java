package client.GUI;

import server.state.GraphicalObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Drawn Shapes panel displays the info of the shapes being
 * displayed on the screen. This could either be the current state
 * of the canvas, or the snapshot. It shows the shape, color, width,
 * height, and location of each GraphicalObject being displayed.
 */
class DrawnShapesPanel extends JScrollPane {

    private JList<GraphicalObject> goJList;

    DrawnShapesPanel() {
        goJList = new JList<>();
        setBorder(new TitledBorder("All Shapes"));
        setViewportView(goJList);
    }

    /**
     * Updates the displayed list with a list of GraphicalObjects
     * @param goList list of objects to show their information
     */
    void update(ConcurrentHashMap<Long, GraphicalObject> goList) {
        // update if changes are made on server
        DefaultListModel<GraphicalObject> dlm = new DefaultListModel<>();
        goList.forEach((shapeID, go) -> dlm.addElement(go));
        goJList.setModel(dlm);
    }

}
