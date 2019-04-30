package client.GUI;

import client.behaviors.Behavior;
import server.state.GraphicalObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * JFrame for the GUI. The Window holds all the parts of the client.
 * It's behavior is specified by it's Behavior object.
 */
public class Window extends JFrame {
    // Behavior of the client
    private Behavior behavior;

    // GUI
    private DrawingSurfacePanel drawingSurface;
    private DrawnShapesPanel drawnShapes;
    private GOInfoPanel infoPanel;
    private JButton removeYours, removeAll, undo;
    private JButton saveSnapshot, getSnapshot, getCanvas;

    public Window(Behavior behavior){
        // Set window behavior
        this.behavior = behavior;

        // Set title
        setTitle("Shared Drawing Surface: ClientID[" + behavior.getId() + "]");

        // Initialize GUI buttons
        removeYours = new JButton("Remove Yours");
        removeYours.addActionListener(e -> {
            if (behavior.isCanvasMode())
                behavior.removeYours();
        });

        removeAll = new JButton("Remove All");
        removeAll.addActionListener(e -> {
            if (behavior.isCanvasMode())
                behavior.removeAll();
        });

        undo = new JButton("Undo");
        undo.addActionListener(e -> {
            if (behavior.isCanvasMode())
                behavior.undo();
        });

        saveSnapshot = new JButton("Save Snapshot");
        saveSnapshot.addActionListener(e -> {
            if (behavior.isCanvasMode())
                behavior.saveSnapshot();
        });

        getSnapshot = new JButton("Show Snapshot");
        getSnapshot.addActionListener(e -> {
            if (behavior.isCanvasMode())
                behavior.loadSnapshot();
        });

        getCanvas = new JButton("Show Canvas");
        getCanvas.addActionListener(e -> {
            if (!behavior.isCanvasMode())
                behavior.loadCurrentCanvas();
        });

        // Initialize GUI panels
        infoPanel = new GOInfoPanel();

        drawnShapes = new DrawnShapesPanel();

        drawingSurface = new DrawingSurfacePanel();
        drawingSurface.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (behavior.isCanvasMode()) {
                    long id = behavior.getId();
                    GraphicalObject newGO = infoPanel.makeGraphicalObject(id, e.getPoint());
                    behavior.addShape(newGO);
                }
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });

        // add components
        addPanels();

        // Disconnect on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(behavior::disconnect));

        // Listen for updates from server
        Thread t = new Thread(() -> behavior.listenForUpdates(this));
        t.start();
    }

    /**
     * Adds GUI panels to JFrame
     */
    private void addPanels() {
        // Setup the layout
        Container cp = getContentPane();
        cp.setLayout(new GridBagLayout());

        int[] width = new int[23];
        Arrays.fill(width, 25);

        int[] height = new int[17];
        Arrays.fill(height, 25);

        ((GridBagLayout)cp.getLayout()).columnWidths = width;
        ((GridBagLayout)cp.getLayout()).rowHeights = height;

        GridBagConstraints gc = new GridBagConstraints();

        // Add the Drawing Surface
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 15; gc.gridheight = 15; gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.BOTH;
        cp.add(drawingSurface, gc);

        // Add Drawn Shapes
        gc.gridx = 15; gc.gridwidth = 8; gc.gridheight = 10;
        cp.add(drawnShapes, gc);

        // Add Info Panel
        gc.gridy = 10; gc.gridheight = 7;
        cp.add(infoPanel, gc);

        // Add Buttons
        gc.gridx = 0; gc.gridy = 15; gc.gridwidth = 5; gc.gridheight = 1;
        cp.add(removeYours, gc);
        gc.gridx = 5;
        cp.add(removeAll, gc);
        gc.gridx = 10;
        cp.add(undo, gc);
        gc.gridx = 0; gc.gridy = 16;
        cp.add(saveSnapshot, gc);
        gc.gridx = 5;
        cp.add(getSnapshot, gc);
        gc.gridx = 10;
        cp.add(getCanvas, gc);

        // Fix JFrame size
        setSize(590, 466);

        setLocationRelativeTo(null);
    }

    /**
     * Repaints the drawing surface and updates Drawn Shapes List
     */
    public void tellToRepaint() {
        ArrayList<GraphicalObject> list = behavior.getGraphicalObjects();
        drawnShapes.update(list);
        drawingSurface.setShapesToDraw(list);
        repaint();
    }
}
