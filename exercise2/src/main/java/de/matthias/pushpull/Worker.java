package de.matthias.pushpull;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.math.BigInteger;

public class Worker {
    public static Long calcTimeInflate = 0L;

    private final ZMQ.Socket puller;
    private final ZMQ.Socket pusher;

    public Worker(String pullHost, String pushHost) {
        try (ZContext context = new ZContext(2)) {
            this.puller = context.createSocket(SocketType.PULL);
            this.puller.bind(pullHost);

            this.pusher = context.createSocket(SocketType.PUSH);
            this.pusher.connect(pushHost);

            setupReceive();
        }
    }

    private void setupReceive() {
        //noinspection InfiniteLoopStatement
        while (true) {
            //just block the thread for solving
            String sol = solve(new BigInteger(puller.recvStr()), calcTimeInflate);
            sendSolution(sol);
        }
    }

    private void sendSolution(String solution) {
        pusher.send(solution);
    }

    /**
     * Calculates the prime factors of a given number
     *
     * @param req the prime to solve
     * @param addedTimeToCalc add this time to the calculation to inflate the calc time
     * @return the solution that can be printed
     */
    private String solve(BigInteger req, Long addedTimeToCalc) {
        try {
            Thread.sleep(addedTimeToCalc);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

        //just panic/crash if solution is wrong
        assert a.multiply(b).equals(req);
        return "Calculated: " + req + " = " + a + " * " + b;
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
