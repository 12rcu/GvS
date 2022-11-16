package de.matthias.pushpull;

import de.matthias.pushpull.controller.Controller;
import de.matthias.pushpull.controller.WorkerData;

import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        String controller = "tcp://127.0.0.1:27300";

        String worker1 = "tcp://127.0.0.1:27301";
        String worker2 = "tcp://127.0.0.1:27302";
        String worker3 = "tcp://127.0.0.1:27303";

        ArrayList<String> workers = new ArrayList<>();
        workers.add(worker1);
        workers.add(worker2);
        workers.add(worker3);

        //add 5 sec to each calc time to ensure that not only one Worker is working at a time
        Worker.calcTimeInflate = 5000L;

        //start worker each in a new thread
        workers.forEach(it -> new Thread(() -> new Worker(it, controller)).start());

        new Controller(workers, controller);
    }
}
