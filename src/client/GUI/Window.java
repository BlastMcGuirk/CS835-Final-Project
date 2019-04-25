package client.GUI;

import client.behaviors.Behavior;
import server.state.GraphicalObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class Window extends JFrame {

    private Behavior behavior;

    private JButton removeYours, removeAll, undo;
    private GOInfoPanel infoPanel;
    private DrawnShapesPanel drawnShapes;
    private DrawingSurfacePanel drawingSurface;

    public Window(Behavior behavior){
        // Set window behavior
        this.behavior = behavior;

        // create panels
        removeYours = new JButton("Remove Yours");
        removeYours.addActionListener(e -> behavior.removeYours());

        removeAll = new JButton("Remove All");
        removeAll.addActionListener(e -> behavior.removeAll());

        undo = new JButton("Undo");
        undo.addActionListener(e -> behavior.undo());


        infoPanel = new GOInfoPanel();
        drawnShapes = new DrawnShapesPanel();
        drawingSurface = new DrawingSurfacePanel();

        drawingSurface.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                long id = behavior.getId();
                GraphicalObject newGO = infoPanel.makeGraphicalObject(id, e.getPoint());
                behavior.addShape(newGO);
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });

        // add components
        addPanels();

        // Listen for updates from server
        Thread t = new Thread(() -> behavior.listenForUpdates(this));
        t.start();
    }

    private void addPanels() {
        // Setup the layout
        Container cp = getContentPane();
        cp.setLayout(new GridBagLayout());
        ((GridBagLayout)cp.getLayout()).columnWidths = new int[] {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};
        ((GridBagLayout)cp.getLayout()).rowHeights = new int[] {50, 50, 50, 50, 50, 50, 50, 50, 50};

        GridBagConstraints gc = new GridBagConstraints();

        // Add the Drawing Surface
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 7; gc.gridheight = 7; gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.BOTH;
        cp.add(drawingSurface, gc);

        // Add Shape List
        gc.gridx = 7; gc.gridwidth = 4; gc.gridheight = 5;
        cp.add(drawnShapes, gc);

        // Add Shape Builder
        gc.gridy = 5; gc.gridheight = 3;
        cp.add(infoPanel, gc);

        // Add Buttons
        gc.gridx = 0; gc.gridy = 7; gc.gridwidth = 2; gc.gridheight = 1;
        cp.add(removeYours, gc);
        gc.gridx = 2;
        cp.add(removeAll, gc);
        gc.gridx = 5;
        cp.add(undo, gc);

        // Fix JFrame size
        setSize(570, 400);
        //pack();
        setLocationRelativeTo(null);
    }

    public void tellToRepaint() {
        ArrayList<GraphicalObject> list = behavior.getGraphicalObjects();
        drawnShapes.update(list);
        drawingSurface.setShapesToDraw(list);
        repaint();
    }
}
