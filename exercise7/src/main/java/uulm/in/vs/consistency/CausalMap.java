package uulm.in.vs.consistency;

import uulm.in.vs.time.VectorClock;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class CausalMap implements ConsistentMap<String, String> {
    private final ConcurrentHashMap<String, CausalEntry> hashMap;

    private final Communicator communicator;

    private final VectorClock timestamp;

    private final LinkedBlockingQueue<CausalUpdate> inputQueue;

    public CausalMap(VectorClock timestamp) {
        hashMap = new ConcurrentHashMap<>();
        communicator = Communicator.getInstance();
        this.timestamp = timestamp;
        inputQueue = new LinkedBlockingQueue<>();

        communicator.register(inputQueue);

        new Thread(() -> {
            while (true) {
                if(!inputQueue.isEmpty()) {
                    try {
                        //take the first element
                        CausalUpdate update = inputQueue.take();
                        //check if the update could take place
                        boolean success = makeUpdate(update);
                        if(!success) {
                            //if not, re-append the update
                            inputQueue.add(update);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    @Override
    public int size() {
        return hashMap.size();
    }

    @Override
    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    @Override
    public boolean containsKey(String s) {
        return hashMap.containsKey(s);
    }

    @Override
    public boolean containsValue(String s) {
        return hashMap.values().stream().anyMatch(it -> it.value.equals(s));
    }

    @Override
    public void remove(String key) {
        hashMap.get(key).deleted = true;
        timestamp.increment();
        communicator.submitUpdate(
                CausalUpdate.Delete(key, timestamp)
        );
    }

    @Override
    public void put(String key, String value) {
        timestamp.increment();
        hashMap.put(key, new CausalEntry(value, timestamp));
        communicator.submitUpdate(
                CausalUpdate.Update(key, value, timestamp)
        );
    }

    @Override
    public String get(String key) {
        if(!containsKey(key))
            return null;
        return hashMap.get(key).value;
    }

    @Override
    public Collection<String> Values() {
        return hashMap.values().stream().map(it -> it.value).toList();
    }

    @Override
    public Set<String> keySet() {
        return hashMap.keySet();
    }

    private boolean makeUpdate(CausalUpdate update) {
        if(update.timestamp.geq(timestamp)) {
            timestamp.merge(update.timestamp);
            hashMap.get(update.key).value = update.value;
            hashMap.get(update.key).deleted = update.delete;
        }
        return update.timestamp.geq(timestamp);
    }
}
