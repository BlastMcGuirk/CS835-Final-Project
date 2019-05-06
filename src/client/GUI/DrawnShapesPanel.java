package client.GUI;

import client.behaviors.Behavior;
import server.state.GraphicalObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Drawn Shapes panel displays the info of the shapes being
 * displayed on the screen. This could either be the current state
 * of the canvas, or the snapshot. It shows the shape, color, width,
 * height, and location of each GraphicalObject being displayed.
 */
class DrawnShapesPanel extends JScrollPane {

    private JList<GraphicalObject> goJList;

    private JComboBox<GraphicalObject.ShapeType> typeValue;
    private JComboBox<String> colorValue;
    private JSpinner widthValue, heightValue;

    DrawnShapesPanel(Behavior behavior) {
        goJList = new JList<>();
        setBorder(new TitledBorder("All Shapes"));
        setViewportView(goJList);

        typeValue = new JComboBox<>(GraphicalObject.ShapeType.values());
        colorValue = new JComboBox<>(new String[] {"Black", "Red", "Green", "Blue"} );
        widthValue = new JSpinner(new SpinnerNumberModel(50, 10, 100, 1));
        heightValue = new JSpinner(new SpinnerNumberModel(50, 10, 100, 1));

        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    GraphicalObject go = goJList.getSelectedValue();

                    typeValue.setSelectedItem(go.getType());
                    colorValue.setSelectedItem(go.getColorName());
                    widthValue.setValue(go.getWidth());
                    heightValue.setValue(go.getHeight());

                    final JComponent[] inputs = new JComponent[]{
                            new JLabel("Type"),
                            typeValue,
                            new JLabel("Color"),
                            colorValue,
                            new JLabel("Width"),
                            widthValue,
                            new JLabel("Height"),
                            heightValue
                    };
                    int result = JOptionPane.showConfirmDialog(null, inputs, "Change Shape", JOptionPane.DEFAULT_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        behavior.editShape(go, (GraphicalObject.ShapeType) typeValue.getSelectedItem(), (String) colorValue.getSelectedItem(),
                                (Integer) widthValue.getValue(), (Integer) heightValue.getValue());
                    }
                }
            }
        };
        goJList.addMouseListener(mouseListener);
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
