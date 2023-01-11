package uulm.in.vs.gvs.task1;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.math.BigInteger;

/**
 * @author David Mödinger
 */

public class SynchronizedClock implements Clock {
    private final ZMQ.Socket socket;
    private long currentTime;
    private int numRequests;
    private int synchronises = 1;
    private long lastRequest;

    public SynchronizedClock(ZContext context, String host, int numRequests) {
        this.socket = context.createSocket(SocketType.REQ);
        socket.connect("tcp://" + host);

        currentTime = request();
        this.numRequests = numRequests;
    }

    public SynchronizedClock(ZContext context, String host, int numRequests, long start) {
        this.currentTime = start;
        this.socket = context.createSocket(SocketType.REQ);
        socket.connect("tcp://" + host);
        this.numRequests = numRequests;
    }

    public long getTime() {
        synchronise();
        return currentTime;
    }

    /**
     * makes a request to the server and stops the time it takes until the response comes in
     *
     * @return the time + rtt/2
     */
    private long request() {
        long initial = System.currentTimeMillis();
        socket.send(" ");
        long time = Long.parseLong(socket.recvStr());
        long rtt = System.currentTimeMillis() - initial;
        return normalize(calcTimeCristian(time, rtt));
    }

    private void synchronise() {
        for (int i = 0; i < numRequests; i++) {
            long newTime = request();
            if (newTime > currentTime) {
                currentTime = newTime;
            }
        }
    }

    private long calcTimeCristian(long time, long rtt) {
        return time + (rtt / 2); //rtt in milis
    }

    private long normalize(long newTime) {
        long currentRequest = System.currentTimeMillis() / 1000;
        long expectedCurrentTime = currentTime + (currentRequest - lastRequest);

        //todo add weighted avg from (synchronises * expectedCurrentTime + newTime) / (synchronises + 1) without overflow

        synchronises ++;
        return (expectedCurrentTime + newTime) / 2;
    }
}
