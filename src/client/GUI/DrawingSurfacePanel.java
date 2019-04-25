package client.GUI;

import server.state.GraphicalObject;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;

public class DrawingSurfacePanel extends JPanel {

    private ArrayList<GraphicalObject> shapesToDraw;

    DrawingSurfacePanel() {
        setBorder(LineBorder.createBlackLineBorder());
        shapesToDraw = new ArrayList<>();
    }

    void setShapesToDraw(ArrayList<GraphicalObject> list) {
        shapesToDraw = list;
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (!shapesToDraw.isEmpty()){
            for (GraphicalObject go : shapesToDraw) {
                g.setColor(go.getColor());
                switch (go.getType()) {
                    case Circle:
                        g.fillOval(go.getPoint().x, go.getPoint().y, go.getWidth(), go.getHeight());
                        break;
                    case Triangle:
                        fillTriangle(g, go);
                        break;
                    case Rectangle:
                        g.fillRect(go.getPoint().x, go.getPoint().y, go.getWidth(), go.getHeight());
                    default:
                        g.fillOval(go.getPoint().x, go.getPoint().y, go.getWidth(), go.getHeight());
                }
            }
        }
    }

    private void fillTriangle(Graphics g, GraphicalObject go) {
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        Point p = go.getPoint();

        // Top point
        xPoints[0] = go.getWidth() / 2 + p.x;
        yPoints[0] = p.y;

        // Bottom left point
        xPoints[1] = p.x;
        yPoints[1] = go.getHeight() + p.y;

        // Bottom right point
        xPoints[2] = go.getWidth() + p.x;
        yPoints[2] = go.getHeight() + p.y;

        g.fillPolygon(xPoints, yPoints, 3);
    }

}
