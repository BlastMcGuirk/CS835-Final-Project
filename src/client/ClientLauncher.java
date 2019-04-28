package client;

import client.GUI.Window;
import client.behaviors.Behavior;
import client.behaviors.RMIBehavior;
import client.behaviors.SocketBehavior;
import server.state.Canvas;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import static server.ServerLauncher.RMI_PORT;
import static server.ServerLauncher.SOCKET_PORT;

public class ClientLauncher {

    public static void main(String[] args) {
        // args[0]:
        //		1 = RMI
        //		2 = Socket
        Behavior behavior;

        int code = Integer.parseInt(args[0]);

        // RMI Client
        if (code == 1) {
            // Get canvas from RMI
            Canvas c = null;
            try {
                c = (Canvas) java.rmi.Naming.lookup("rmi://" + "0.0.0.0" + ":" + RMI_PORT + "/Canvas");
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
            }
            // Assign client behavior
            assert c != null;
            behavior = new RMIBehavior(c);
            System.out.println("Connected to RMI service.");
        }

        // Socket Client
        else if (code == 2) {
            // Open socket connection
            Socket socket = null;
            try {
                socket = new Socket("0.0.0.0", SOCKET_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert socket != null;

            Scanner in = null;
            PrintWriter out = null;
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert in != null;
            assert out != null;

            behavior = new SocketBehavior(socket, in, out);
            System.out.println("Connected to Socket service.");
        } else {
            throw new IllegalArgumentException("Args[0] must be 1 for RMI or 2 for Socket");
        }

        JFrame clientWindow = new Window(behavior);
        clientWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        clientWindow.setResizable(false);
        clientWindow.setVisible(true);
    }

}
