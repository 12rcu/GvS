package uulm.in.vs.gvs.task2;

import com.google.common.primitives.Longs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class VectorClock {
    long[] time;
    int myId;

    public VectorClock(Collection<Long> C, int id) {
        //time spent on own method: 45 min
        //time spent implementing guava 3 min
        //source https://www.geeksforgeeks.org/java-guava-longs-toarray-method-with-examples/
        time = Longs.toArray(C);
        this.myId = id;
    }

    public VectorClock(int size, int id) {
        time = new long[size];
        this.myId = id;
    }

    /**
     * Returns all times in the vector
     */
    public long[] getTime() {
        return time;
    }

    /**
     * Also returns incremented time for own processID
     */
    public long increment() {
        time[myId]++;
        return time[myId];
    }

    /**
     * Returns time of given id
     */
    public long getTime(int id) {
        return time[id];
    }

    public long merge(VectorClock b) throws IllegalArgumentException {
        if(b.size() != size()) throw new IllegalArgumentException("different size");
        for (int i = 0; i < b.time.length; i++) {
            if(b.getTime(i) > getTime(i)) {
                time[i] = b.getTime(i);
            }
        }
        return increment();
    }

    public long size() {
        return time.length;
    }

    /**
     * Greater-or-Equals comparison
     * IllegalArgumentException is thrown when vectors are of different size.
     */
    public boolean geq(VectorClock b) throws IllegalArgumentException {
        if (b.size() == this.size()) {
            for (int i = 0; i < time.length; i++) {
                if(this.getTime(i) < b.getTime(i)) {
                    return false;
                }
            }
            return true;
        }
        throw new IllegalArgumentException("different size");
    }

    /**
     * @return Positive if a>b, Negative if a<b, 0 if a==b, empty Optional if not ordered
     * @throws IllegalArgumentException If Vectors are of different size
     */
    public static Optional<Integer> compare(VectorClock a, VectorClock b) throws IllegalArgumentException {
        if (a.size() != b.size()) throw new IllegalArgumentException("different size");

        if(a.equals(b))
            return Optional.of(0);
        if(a.bg(b))
            return Optional.of(1);
        if(a.sm(b))
            return Optional.of(-1);

        return Optional.empty();
    }

    private boolean bg(VectorClock b) {
        for (int i = 0; i < time.length; i++) {
            if(this.getTime(i) < b.getTime(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean sm(VectorClock b) {
        for (int i = 0; i < time.length; i++) {
            if(this.getTime(i) > b.getTime(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(VectorClock b) {
        for (int i = 0; i < time.length; i++) {
            if(this.getTime(i) != b.getTime(i)) {
                return false;
            }
        }
        return true;
    }
}
