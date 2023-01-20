package in.vs.time;

public class TotallyOrderedClock {
    private final long PID;

    private long time = 0L;
    private long logicalTime = 0L;

    public TotallyOrderedClock(long PID) {
        this.PID = PID;
    }

    public TotallyOrderedTimestamp createTimestamp() throws IllegalArgumentException {
        logicalTime++;
        return createTimestamp(System.currentTimeMillis());
    }

    public TotallyOrderedTimestamp createTimestamp(long time) throws IllegalArgumentException {
        if (time < this.time)
            throw new IllegalArgumentException("");
        logicalTime++;
        this.time = time;
        return new TotallyOrderedTimestamp(PID, this.time, logicalTime);
    }
}
