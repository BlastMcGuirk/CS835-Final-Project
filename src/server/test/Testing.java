package server.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import server.state.Canvas;
import server.state.GraphicalObject;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Testing {

    private static Canvas c;
    private static ExecutorService exec;
    private static GraphicalObject go1;
    private static GraphicalObject go2;
    private static GraphicalObject go3;

    @BeforeAll
    static void setUp() {
        exec = Executors.newFixedThreadPool(100);
        go1 = new GraphicalObject(1, GraphicalObject.ShapeType.Circle, Color.BLACK, 50, 50, new Point(10, 10));
        go2 = new GraphicalObject(2, GraphicalObject.ShapeType.Triangle, Color.BLUE, 75, 25, new Point(100, 100));
        go3 = new GraphicalObject(3, GraphicalObject.ShapeType.Rectangle, Color.RED, 40, 60, new Point(310, 210));
    }

    @BeforeEach
    void setUpBeforeEach() {
        c = new Canvas();
    }

    @Test
    void addShapesAtSameTime() throws InterruptedException {
        CountDownLatch readyLatch = new CountDownLatch(3);
        CountDownLatch gate = new CountDownLatch(1);
        CountDownLatch countDownLatch = new CountDownLatch(3);
        exec.submit(() -> {
            readyLatch.countDown();
            try {
                gate.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            c.addShape(go1);
            countDownLatch.countDown();
        });
        exec.submit(() -> {
            readyLatch.countDown();
            try {
                gate.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            c.addShape(go2);
            countDownLatch.countDown();
        });
        exec.submit(() -> {
            readyLatch.countDown();
            try {
                gate.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            c.addShape(go3);
            countDownLatch.countDown();
        });
        readyLatch.await();
        Thread.sleep(1000);
        gate.countDown();
        countDownLatch.await();
        Assertions.assertEquals(3, c.getShapeMap().size());
        Assertions.assertEquals(3, c.getVersionNumber());
    }

    @Test
    void removeShapesAtSameTime() throws InterruptedException {
        CountDownLatch addCountdown = new CountDownLatch(3);
        exec.submit(() -> {
            c.addShape(go1);
            addCountdown.countDown();
        });
        exec.submit(() -> {
            c.addShape(go2);
            addCountdown.countDown();
        });
        exec.submit(() -> {
            c.addShape(go3);
            addCountdown.countDown();
        });
        addCountdown.await();
        CountDownLatch removeCountdown = new CountDownLatch(3);
        exec.submit(() -> {
            c.removeAll(1);
            removeCountdown.countDown();
        });
        exec.submit(() -> {
            c.removeAll(2);
            removeCountdown.countDown();
        });
        exec.submit(() -> {
            c.removeAll(3);
            removeCountdown.countDown();
        });
        removeCountdown.await();
        Assertions.assertEquals(0, c.getShapeMap().size());
        Assertions.assertEquals(0, c.getVersionNumber());
    }

    @Test
    void removingAllWithIDSameIDs() throws InterruptedException {
        CountDownLatch addCountdown = new CountDownLatch(3);
        exec.submit(() -> {
            c.addShape(go1);
            addCountdown.countDown();
        });
        exec.submit(() -> {
            c.addShape(go2);
            addCountdown.countDown();
        });
        exec.submit(() -> {
            c.addShape(go3);
            addCountdown.countDown();
        });
        addCountdown.await();
        CountDownLatch removeCountdown = new CountDownLatch(3);
        exec.submit(() -> {
            c.removeAllWithID(1);
            removeCountdown.countDown();
        });
        exec.submit(() -> {
            c.removeAllWithID(1);
            removeCountdown.countDown();
        });
        exec.submit(() -> {
            c.removeAllWithID(1);
            removeCountdown.countDown();
        });
        removeCountdown.await();
        Assertions.assertEquals(3, c.getShapeMap().size());
        Assertions.assertEquals(6, c.getVersionNumber());
    }

    @Test
    void removingAllWithIDsDifferentIDs() throws InterruptedException {
        CountDownLatch addCountdown = new CountDownLatch(3);
        exec.submit(() -> {
            c.addShape(go1);
            addCountdown.countDown();
        });
        exec.submit(() -> {
            c.addShape(go2);
            addCountdown.countDown();
        });
        exec.submit(() -> {
            c.addShape(go3);
            addCountdown.countDown();
        });
        addCountdown.await();
        CountDownLatch removeCountdown = new CountDownLatch(3);
        exec.submit(() -> {
            c.removeAllWithID(1);
            removeCountdown.countDown();
        });
        exec.submit(() -> {
            c.removeAllWithID(2);
            removeCountdown.countDown();
        });
        exec.submit(() -> {
            c.removeAllWithID(3);
            removeCountdown.countDown();
        });
        removeCountdown.await();
        Assertions.assertEquals(0, c.getShapeMap().size());
        Assertions.assertEquals(6, c.getVersionNumber());
    }

    @Test
    void addingShapeWhileRemovingAll() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        exec.submit(() -> {
            c.addShape(go1);
            countDownLatch.countDown();
        });
        exec.submit(() -> {
            c.removeAll(1);
            countDownLatch.countDown();
        });
        countDownLatch.await();
    }

    @Test
    void editingSameShapeAtSameTime() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        c.addShape(go1);
        exec.submit(() -> {
            c.editShape(go1.getShapeID(), 2, GraphicalObject.ShapeType.Triangle, "Blue", 10, 10);
            countDownLatch.countDown();
        });
        exec.submit(() -> {
            c.editShape(go1.getShapeID(), 3, GraphicalObject.ShapeType.Rectangle, "Red", 11, 11);
            countDownLatch.countDown();
        });
        countDownLatch.await();
        GraphicalObject newGo1 = c.getShapeMap().get(go1.getShapeID());
        Assertions.assertNotEquals(1, newGo1.getClientID());
        if (newGo1.getClientID() == 2) {
            Assertions.assertEquals(GraphicalObject.ShapeType.Triangle, newGo1.getType());
            Assertions.assertEquals("Blue", newGo1.getColorName());
            Assertions.assertEquals(10, newGo1.getWidth());
            Assertions.assertEquals(10, newGo1.getHeight());
        } else {
            Assertions.assertEquals(GraphicalObject.ShapeType.Rectangle, newGo1.getType());
            Assertions.assertEquals("Red", newGo1.getColorName());
            Assertions.assertEquals(11, newGo1.getWidth());
            Assertions.assertEquals(11, newGo1.getHeight());
        }
    }

    @Test
    void tonsOfGhostClientsAdding() throws InterruptedException {
        CountDownLatch readyLatch = new CountDownLatch(100);
        CountDownLatch gate = new CountDownLatch(1);
        CountDownLatch countDownLatch = new CountDownLatch(10000);
        Runnable ghostClient = () -> {
            for (int i = 0; i < 100; i++) {
                readyLatch.countDown();
                try {
                    gate.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                c.addShape(go1);
                countDownLatch.countDown();
            }
        };
        for (int i = 0; i < 100; i++) {
            exec.submit(ghostClient);
        }
        readyLatch.await();
        Thread.sleep(1000);
        gate.countDown();
        countDownLatch.await();
        Assertions.assertEquals(10000, c.getVersionNumber());
    }

}
