package de.matthias.pubsub;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.math.BigInteger;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Matthias Klenz
 */
public class PrimeFactorization extends Thread {
    private final LinkedBlockingDeque<String> requests = new LinkedBlockingDeque<>();

    public static void main(String[] args) {
        try (ZContext context = new ZContext(2)) {
            ZMQ.Socket socket = context.createSocket(SocketType.SUB);
            new PrimeFactorization(socket);
        }
    }

    PrimeFactorization(ZMQ.Socket socket) {
        String host = "tcp://gvs.lxd-vs.uni-ulm.de:27378";

        socket.connect(host);
        boolean connected = socket.subscribe("");
        if(connected)
            System.out.println("connected");
        else {
            System.out.println("connection failed");
            return;
        }

        //take request from queue in own thread and use fermat to solve
        runAsync();

        //take new request from socket and add it to queue
        while (true) {
            String topic = socket.recvStr();
            if (topic == null)
                break;
            String data = socket.recvStr();
            requests.add(data);
        }
    }

    private void runAsync() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    if (requests.isEmpty()) {
                        //noinspection BusyWait
                        sleep(1000);
                    } else {
                        //calculation blocks thread
                        calculate(new BigInteger(requests.take()));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    /**
     * uses Fermat <a href="https://en.wikipedia.org/wiki/Fermat%27s_factorization_method">Fermat</a> basic method to calculate 2 primes, prints out the solution to the stdout
     * @param req the requested number to factorise
     */
    public void calculate(BigInteger req) {
        System.out.println("Calculating: " + req);

        //fermat
        BigInteger sqr = req.sqrt();
        BigInteger a = sqr;

        //ceil solution
        if(checkNotSquare(sqr)) {
            // a <- ceiling(sqrt(N))
            a = sqr.add(BigInteger.ONE);
        }

        //b2 <- a*a - N
        BigInteger b2 = a.multiply(a).subtract(req);

        //repeat until b2 is a square:
        while (checkNotSquare(b2)) {
            //a <- a + 1
            a = a.add(BigInteger.ONE);
            //b2 <- a*a - N
            b2 = a.multiply(a).subtract(req);
        }

        //a - sqrt(b2)
        a = a.subtract(b2.sqrt());
        //a is the first factor, b is the second
        BigInteger b = req.divide(a);

        System.out.println("Calculated: " + req + " = " + a + " * " + b);

        //just panic/crash if solution is wrong
        assert a.multiply(b).equals(req);
    }

    /**
     * checks if a given number is a square and negates that solution
     * @param val the value to check for a square
     * @return false if the value is a square
     */
    private boolean checkNotSquare (BigInteger val) {
        BigInteger squareRoot = val.sqrt();
        return !val.subtract(squareRoot.multiply(squareRoot)).equals(BigInteger.ZERO);
    }
}