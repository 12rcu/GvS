package uulm.in.vs.consistency;

import uulm.in.vs.time.VectorClock;

public class CausalUpdate {
    public String key;
    public String value;
    public VectorClock timestamp;
    public boolean delete;

    public CausalUpdate(String key, String value, VectorClock timestamp, boolean delete) {
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
        this.delete = delete;
    }

    public static CausalUpdate Delete(String key, VectorClock timestamp) {
        return new CausalUpdate(key, "", timestamp, true);
    }

    public static CausalUpdate Update(String key, String value, VectorClock timestamp) {
        return new CausalUpdate(key, value, timestamp, false);
    }

}
