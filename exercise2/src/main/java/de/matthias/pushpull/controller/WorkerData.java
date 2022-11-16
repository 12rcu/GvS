package de.matthias.pushpull.controller;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * @author Matthias Klenz
 */
public class WorkerData {
    private final ZMQ.Socket pusher;

    public WorkerData(String pushHost, ZContext context) {
        this.pusher = context.createSocket(SocketType.PUSH);
        this.pusher.connect(pushHost);
    }

    public ZMQ.Socket getPusher() {
        return pusher;
    }
}
