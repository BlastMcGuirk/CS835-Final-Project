package client;

import client.behaviors.Behavior;
import client.behaviors.RMIBehavior;
import client.behaviors.SocketBehavior;
import server.state.Canvas;

import javax.swing.*;

public class ClientLauncher {

    public static void main(String[] args) {
        // args[1]:
        //		1 = RMI
        //		2 = Socket
        Behavior behavior;

        int code = Integer.parseInt(args[1]);
        if (code == 1) {
            Canvas c = (Canvas) java.rmi.Naming.lookup("rmi://" + host + ":" + port + "/Canvas");
            behavior = new RMIBehavior(c);
        } else if (code == 2) {
            behavior = new SocketBehavior();
        } else {
            throw new IllegalArgumentException("Args[1] must be 1 for RMI or 2 for Socket");
        }

        JFrame clientWindow = new Window(behavior);
        clientWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        clientWindow.setResizable(false);
        clientWindow.setVisible(true);
    }

}
