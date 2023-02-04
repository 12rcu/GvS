package uulm.in.vs.consistency;

import uulm.in.vs.time.VectorClock;

public class CausalEntry {
    String value;
    VectorClock vectorClock;
    boolean deleted;

    public void update(CausalEntry entry, boolean delete){
        if(vectorClock.geq(entry.vectorClock))
            return;

        value = entry.value;
        vectorClock.merge(entry.vectorClock);
        deleted = delete;
    }

    public CausalEntry(String value, VectorClock time){
        this.value = value;
        this.vectorClock = new VectorClock(time);
        this.deleted = false;
    }
}
