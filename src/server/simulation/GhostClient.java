package server.simulation;

import server.state.CanvasInterface;
import server.state.GraphicalObject;

import java.awt.*;
import java.rmi.RemoteException;
import java.util.Random;

public class GhostClient implements Runnable {

    private CanvasInterface canvas;

    private int activityPercentage;

    private long id;

    private Random random;

    public GhostClient(CanvasInterface canvas, int activityPercentage) {
        this.canvas = canvas;
        this.activityPercentage = activityPercentage;
        try {
            id = canvas.registerNewUser();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        random = new Random();
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            // sleep for random amount of time
            long time = random.nextInt(3);
            if (activityPercentage == 50) {
                time += 8;
            } else {
                time += 3;
            }
            try {
                Thread.sleep(time * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // do something
            int randomAction = random.nextInt(100);

            int doAction;
            if (randomAction < 75) {
                doAction = 1;
            } else if (randomAction < 90) {
                doAction = 2;
            } else {
                doAction = 3;
            }
            switch (doAction) {
                case 1:
                    try {
                        canvas.addShape(generateRandomShape());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        canvas.removeAllWithID(id);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    try {
                        canvas.removeAll(id);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private GraphicalObject generateRandomShape() {
        int shapeTypeRand = random.nextInt(3);
        int colorRand = random.nextInt(4);
        int widthRand = random.nextInt(91) + 10;
        int heightRand = random.nextInt(91) + 10;
        int xPosRand = random.nextInt(375);
        int yPosRand = random.nextInt(375);

        GraphicalObject.ShapeType shapeType = null;
        switch (shapeTypeRand) {
            case 0:
                shapeType = GraphicalObject.ShapeType.Circle;
                break;
            case 1:
                shapeType = GraphicalObject.ShapeType.Triangle;
                break;
            case 2:
                shapeType = GraphicalObject.ShapeType.Rectangle;
                break;
        }

        Color color = null;
        switch (colorRand) {
            case 0:
                color = Color.BLACK;
                break;
            case 1:
                color = Color.RED;
                break;
            case 2:
                color = Color.GREEN;
                break;
            case 3:
                color = Color.BLUE;
                break;
        }

        Point p = new Point(xPosRand, yPosRand);

        return new GraphicalObject(id, shapeType, color, widthRand, heightRand, p);
    }
}
