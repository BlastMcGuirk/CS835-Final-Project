package client.GUI;

import server.state.GraphicalObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.ArrayList;

/**
 * The Drawn Shapes panel displays the info of the shapes being
 * displayed on the screen. This could either be the current state
 * of the canvas, or the snapshot. It shows the shape, color, width,
 * height, and location of each GraphicalObject being displayed.
 */
class DrawnShapesPanel extends JScrollPane {

    private JList<String> goJList;

    DrawnShapesPanel() {
        goJList = new JList<>();
        setBorder(new TitledBorder("All Shapes"));
        setViewportView(goJList);
    }

    /**
     * Updates the displayed list with a list of GraphicalObjects
     * @param goList list of objects to show their information
     */
    void update(ArrayList<GraphicalObject> goList) {
        // update if changes are made on server
        DefaultListModel<String> dlm = new DefaultListModel<>();
        for (GraphicalObject go : goList) {
            dlm.addElement(go.displayString());
        }
        goJList.setModel(dlm);
    }

}
