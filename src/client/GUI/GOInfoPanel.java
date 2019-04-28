package client.GUI;

import server.state.GraphicalObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * The GOInfoPanel or GraphicalObjectInformationPanel acts as a
 * builder for GraphicalObjects. The user can specify shape type,
 * color, width, and height, and given a point, will generate
 * a GraphicalObject with those values. This panel holds 2
 * JComboBoxes for selecting shape type and color, as well as
 * 2 JSpinners for selecting width/height. Width/Height have an
 * upper limit of 100, and a lower limit of 10.
 */
class GOInfoPanel extends JPanel {

    private JComboBox<GraphicalObject.ShapeType> typeValue;
    private JComboBox<String> colorValue;
    private JSpinner widthValue, heightValue;

    GOInfoPanel() {
        setBorder(new TitledBorder("Choose Shape"));

        typeValue = new JComboBox<>(GraphicalObject.ShapeType.values());
        colorValue = new JComboBox<>(new String[] {"Black", "Red", "Green", "Blue"} );
        widthValue = new JSpinner(new SpinnerNumberModel(50, 10, 100, 1));
        heightValue = new JSpinner(new SpinnerNumberModel(50, 10, 100, 1));

        // Align JObjects in panel
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.anchor = GridBagConstraints.LINE_END;
        gc.gridy = 0; add(new JLabel("Shape: "), gc);
        gc.gridy = 1; add(new JLabel("Color: "), gc);
        gc.gridy = 2; add(new JLabel("Width: "), gc);
        gc.gridy = 3; add(new JLabel("Height: "), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.LINE_START;
        gc.gridy = 0; add(typeValue, gc);
        gc.gridy = 1; add(colorValue, gc);
        gc.gridy = 2; add(widthValue, gc);
        gc.gridy = 3; add(heightValue, gc);
    }

    // Makes a GraphicalObject given a point.
    GraphicalObject makeGraphicalObject(long userId, Point p) {
        return new GraphicalObject(userId, getShapeType(), getShapeColor(), getShapeWidth(), getShapeHeight(), p);
    }

    // The following methods are used to retrieve values from the JObjects.
    private GraphicalObject.ShapeType getShapeType() { return (GraphicalObject.ShapeType) typeValue.getSelectedItem(); }
    private Color getShapeColor() { switch (colorValue.getSelectedIndex()) {
        case 0:
            return Color.BLACK;
        case 1:
            return Color.RED;
        case 2:
            return Color.GREEN;
        default:
            return Color.BLUE;
    } }
    private int getShapeWidth() { return (int)widthValue.getModel().getValue(); }
    private int getShapeHeight() { return (int)heightValue.getModel().getValue(); }

}
