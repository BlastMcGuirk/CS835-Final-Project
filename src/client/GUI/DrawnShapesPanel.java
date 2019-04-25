package client.GUI;

import server.state.GraphicalObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.ArrayList;

class DrawnShapesPanel extends JScrollPane {

    private JList<GraphicalObject> goJList;

    DrawnShapesPanel() {
        goJList = new JList<>();
        setBorder(new TitledBorder("All Shapes"));
        setViewportView(goJList);
    }

    void update(ArrayList<GraphicalObject> goList) {
        // Change if shapes are changed on server
        DefaultListModel<GraphicalObject> dlm = new DefaultListModel<>();
        for (GraphicalObject go : goList) {
            dlm.addElement(go);
        }
        goJList.setModel(dlm);
    }

}
