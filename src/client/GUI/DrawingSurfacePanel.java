package client.GUI;

import server.state.GraphicalObject;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Drawing Surface is where the shapes are displayed, as well
 * as the space that the user clicks on to add a shape in that spot.
 */
public class DrawingSurfacePanel extends JPanel {

    // List of shapes to draw
    private ConcurrentHashMap<Long, GraphicalObject> shapesToDraw;

    DrawingSurfacePanel() {
        setBorder(LineBorder.createBlackLineBorder());
        shapesToDraw = new ConcurrentHashMap<>();
    }

    /**
     * Updates the list of shapes to draw, since no additional params
     * can be added in java.swing paint(Graphics g) method below.
     * @param list List of shapes to draw
     */
    void setShapesToDraw(ConcurrentHashMap<Long, GraphicalObject> list) {
        shapesToDraw = list;
    }

    @Override
    public void paint(Graphics g) {
        // Clear canvas
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Go through list of shapes to draw
        if (!shapesToDraw.isEmpty()){
            shapesToDraw.forEach((shapeID, go) -> {
                g.setColor(go.getColor());
                switch (go.getType()) {
                    case Circle:
                        int x = go.getPoint().x - (go.getWidth() / 2);
                        int y = go.getPoint().y - (go.getHeight() / 2);
                        g.fillOval(x, y, go.getWidth(), go.getHeight());
                        break;
                    case Triangle:
                        fillTriangle(g, go);
                        break;
                    case Rectangle:
                        x = go.getPoint().x - (go.getWidth() / 2);
                        y = go.getPoint().y - (go.getHeight() / 2);
                        g.fillRect(x, y, go.getWidth(), go.getHeight());
                    default:
                        x = go.getPoint().x - (go.getWidth() / 2);
                        y = go.getPoint().y - (go.getHeight() / 2);
                        g.fillOval(x, y, go.getWidth(), go.getHeight());
                }
                if (go.isMarked()) {
                    g.setColor(Color.BLACK);
                    g.drawString("[" + go.getClientID() + "]", go.getPoint().x + (go.getWidth() / 2), go.getPoint().y);
                }
            });
        }
    }

    /**
     * Helper method for paint, draws a triangle.
     * @param g paint brush
     * @param go triangle to be drawn
     */
    private void fillTriangle(Graphics g, GraphicalObject go) {
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        Point p = go.getPoint();

        // Top point
        xPoints[0] = p.x;
        yPoints[0] = p.y - (go.getHeight() / 2);

        // Bottom left point
        xPoints[1] = p.x - (go.getWidth() / 2);
        yPoints[1] = p.y + (go.getHeight() / 2);

        // Bottom right point
        xPoints[2] = p.x + (go.getWidth() / 2);
        yPoints[2] = p.y + (go.getHeight() / 2);

        g.fillPolygon(xPoints, yPoints, 3);
    }

}
