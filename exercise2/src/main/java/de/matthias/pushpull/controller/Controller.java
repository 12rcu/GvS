package de.matthias.pushpull.controller;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Matthias Klenz
 */
@SuppressWarnings("FieldCanBeLocal")
public class Controller {
    //challenge publisher
    private final String taskReceiverEndpoint = "tcp://gvs.lxd-vs.uni-ulm.de:27378";
    //socket
    private ZMQ.Socket taskSocket;
    //open tasks
    private final LinkedBlockingDeque<String> openTasks = new LinkedBlockingDeque<>();
    //worker
    private final List<WorkerData> worker;

    //endpoint for controller solution receive
    private final ZMQ.Socket puller;

    public Controller(List<String> workerHosts, String controllerHost) {
        try (ZContext context = new ZContext(2)) {
            this.puller = context.createSocket(SocketType.PULL);
            this.puller.bind(controllerHost);

            this.worker = workerHosts.stream().map(it -> new WorkerData(it, context)).toList();
            setupTaskReceiver(context);
            setupTaskHandler();
            setupReceiveHandler();
            while (true) {
                //keep alive
            }
        }
    }

    /**
     * Connect to the remote server to receive new tasks and add them into a queue
     */
    private void setupTaskReceiver(ZContext context) {
        new Thread(() -> {
            this.taskSocket = context.createSocket(SocketType.SUB);
            this.taskSocket.connect(taskReceiverEndpoint);
            boolean connected = taskSocket.subscribe("");
            if (connected)
                System.out.println("Controller: connected");
            else {
                System.out.println("Controller: connection failed");
                return;
            }
            //get tasks and add them to a queue

            while (true) {
                String topic = taskSocket.recvStr();
                if (topic == null)
                    break;
                String data = taskSocket.recvStr();
                System.out.println("Controller: Received Task: " + data);
                openTasks.add(data);
            }
        }).start();
    }

    /**
     * always runs through the open task list and tries to give a task to a free worker
     */
    private void setupTaskHandler() {
        new Thread(() -> {

            //generally considered bad practice but in this case I think it's okay
            //infinite loop with a running counter, that resets if the list of workers is iterated through
            for (int i = 0; true; i++) {
                i = i % worker.size();  //set to 0 if end of worker list was reached

                if (openTasks.isEmpty()) {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    String task = openTasks.pop();
                    worker.get(i).getPusher().send(task);
                }
            }
        }).start();
    }

    private void setupReceiveHandler() {
        new Thread(() -> {
            while (true) {
                System.out.println("Controller Received Solution: " + puller.recvStr());
            }
        }).start();
    }
}
