package uulm.in.vs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uulm.in.vs.consistency.CausalMap;
import uulm.in.vs.time.VectorClock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;


public class CausalMapTest {
    int numProcesses = 3;
    List<CausalMap> processes = new ArrayList<>(numProcesses);

    @BeforeEach
    void setUp() {
        processes.add(new CausalMap(new VectorClock(numProcesses,0))); // process 0
        processes.add(new CausalMap(new VectorClock(numProcesses,1))); // process 1
        processes.add(new CausalMap(new VectorClock(numProcesses,2))); // process 2
    }

    @AfterEach
    void clear() {
        for(int i = 0; i < numProcesses; i++) {
            Set<String> keys = processes.get(i).keySet();
            if(!keys.isEmpty())
                for(String key : keys)
                    processes.get(i).remove(key);
        }
    }

    @Test
    void testWriteAndRead() throws InterruptedException {
        Random generator = new Random();
        // Pick a random process for submitting write requests
        int processID = generator.nextInt(3);
        int firstValue = generator.nextInt(Integer.MAX_VALUE);
        int secondValue = generator.nextInt(Integer.MAX_VALUE);

        // Verify local integrity
        for(int i = 0; i < 10; i++) {
            processes.get(processID).put("key" + i, String.valueOf(firstValue));
            String localRead1 = processes.get(processID).get("key" + i);

            processes.get(processID).put("key" + i, String.valueOf(secondValue));
            String localRead2 = processes.get(processID).get("key" + i);

            if(localRead1.equals(String.valueOf(secondValue)))
                assertNotEquals(localRead2, String.valueOf(firstValue));
        }

        // Select another process to read from
        processID = ++processID % 3;
        // Verify remote integrity
        for(int i = 0; i < 10; i++) {
            String readResult = processes.get(processID).get("key" + i);
            // The read result could be null if the value has not been propagated yet
            if(readResult == null) {
                Thread.sleep(5);
                continue;
            }

            String remoteRead1 = processes.get(processID).get("key" + i);
            String remoteRead2 = processes.get(processID).get("key" + i);

            if(remoteRead1.equals(String.valueOf(secondValue)))
                assertNotEquals(remoteRead2, String.valueOf(firstValue));

        }
    }

    @Test
    void testDeleteAndReWrite() throws InterruptedException {
        Random generator = new Random();
        // Pick a random process to submit a write request
        int processID = generator.nextInt(3);
        int firstValue = generator.nextInt(Integer.MAX_VALUE);
        int secondValue = generator.nextInt(Integer.MAX_VALUE);

        // Verify local integrity
        for(int i = 0; i < 10; i++) {
            processes.get(processID).put("key" + i, String.valueOf(firstValue));
            String localRead1 = processes.get(processID).get("key" + i);

            processes.get(processID).remove("key" + i);
            processes.get(processID).put("key" + i, String.valueOf(secondValue));

            String localRead2 = processes.get(processID).get("key" + i);
            // The read result could be null if we try to read the deleted value
            if(localRead2 == null) {
                Thread.sleep(5);
                continue;
            }

            if(localRead1.equals(String.valueOf(secondValue)))
                assertNotEquals(localRead2, String.valueOf(firstValue));
        }

        // Select another process to read from
        processID = ++processID % 3;
        // Verify remote integrity
        for(int i = 0; i < 10; i++) {
            String readResult = processes.get(processID).get("key" + i);
            // The read result could be null if the value has not been propagated yet
            if(readResult == null) {
                Thread.sleep(5);
                continue;
            }

            String remoteRead1 = processes.get(processID).get("key" + i);
            String remoteRead2 = processes.get(processID).get("key" + i);

            if(remoteRead1.equals(String.valueOf(secondValue)))
                assertNotEquals(remoteRead2, String.valueOf(firstValue));

        }
    }
}
