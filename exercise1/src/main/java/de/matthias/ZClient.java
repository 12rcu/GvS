package de.matthias;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Scanner;

public class ZClient {
    public static void main(String[] args) {

        String host = "tcp://gvs.lxd-vs.uni-ulm.de:27347";

        try (ZContext context = new ZContext(1)) {
            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect(host);

            //get the input through stdin/terminal
            System.out.print("Enter your msg: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            //send msg
            System.out.println("\u001B[34mSending Msg...\u001B[0m");
            socket.send(input);
            System.out.println("\u001B[34mWaiting for server to reply...\u001B[0m");
            String reply = socket.recvStr();

            //check reply
            System.out.println("\u001B[34mReceived: " + reply + " \u001B[0m");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
