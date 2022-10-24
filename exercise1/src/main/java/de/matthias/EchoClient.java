package de.matthias;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * GvS Exercise 1
 * Compiled with Java17!
 *
 * @author Matthias Klenz
 */
public class EchoClient {

    public static void main(String[] args) {

        String host = "gvs.lxd-vs.uni-ulm.de";
        int port = 3211;


        try(Socket socket = new Socket(host, port)) {
            //open reader & writer
            System.out.println("\u001B[34mConnecting to " + socket.getInetAddress() + " ...\u001B[0m");
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //get the input through stdin/terminal
            System.out.print("Enter your msg: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            //send msg
            System.out.println("\u001B[34mSending Msg...\u001B[0m");
            writer.println(input);
            String reply = reader.readLine();

            //check reply
            System.out.println("\u001B[34mReceived: " + reply + " \u001B[0m");
            if(reply.equals(input)) {
                System.out.println("\u001B[32mThose are the same msg's, success!\u001B[0m");
            } else {
                System.out.println("\u001B[33mThose aren't the same msg's :(\u001B[0m");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("\u001B[33mSomething went wrong.\u001B[0m");
        } finally {
            System.out.println("\u001B[34mTerminated\u001B[0m");
        }
    }

    public static void solutionTwo() {

        ZContext context = new ZContext(1);
        ZMQ.Socket socket = context.createSocket(ZMQ.PAIR);
    }
}