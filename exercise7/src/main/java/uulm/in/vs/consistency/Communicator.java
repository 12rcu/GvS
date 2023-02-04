package uulm.in.vs.consistency;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This implements a communicator that may scramble messages.
 * All communication should run through this thread.
 */
public class Communicator {
    private class MessageDistributor implements Runnable {
        private final Random random;
        private final ArrayList<LinkedBlockingQueue<CausalUpdate>> recipients;

        public LinkedBlockingQueue<LinkedBlockingQueue<CausalUpdate>> newRecipients;

        public LinkedBlockingQueue<CausalUpdate> inputQueue;

        public AtomicBoolean keep_running;

        public MessageDistributor() {
            recipients = new ArrayList<>();
            newRecipients = new LinkedBlockingQueue<>();
            inputQueue = new LinkedBlockingQueue<>();
            keep_running = new AtomicBoolean(true);
            random = new Random(System.currentTimeMillis());
        }

        @Override
        public void run() {
            // Do magic to distribution stuff
            while(keep_running.get()) {
                try {
                    CausalUpdate update = inputQueue.take();
                    while(newRecipients.peek() != null) {
                        recipients.add(newRecipients.take());
                    }
                    // Put the update back to the queue in 25% of the cases
                    if (random.nextInt() % 4 == 0) {
                        inputQueue.put(update);
                    } else {
                        for (var recipient : recipients) {
                            // Forward the update in 75% of the cases
                            //if (random.nextInt() % 4 != 0) {
                            recipient.offer(update);
                            //}
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Communicator instance;
    private final MessageDistributor messageDistributor;

    private Communicator() {
        messageDistributor = new MessageDistributor();
        Thread runner = new Thread(messageDistributor);
        runner.start();
    }

    public static synchronized Communicator getInstance() {
        if(Communicator.instance == null) {
            Communicator.instance = new Communicator();
        }
        return Communicator.instance;
    }

    public void submitUpdate(CausalUpdate update) {
        try {
            messageDistributor.inputQueue.put(update);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void register(LinkedBlockingQueue<CausalUpdate> box) {
        try {
            messageDistributor.newRecipients.put(box);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
