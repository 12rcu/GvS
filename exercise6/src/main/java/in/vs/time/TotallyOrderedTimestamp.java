package in.vs.time;

import java.math.BigInteger;

public class TotallyOrderedTimestamp implements Comparable<TotallyOrderedTimestamp> {
    private final long pid;
    private final long time;
    private final long logicalTime;

    public TotallyOrderedTimestamp(long pid, long time, long logicalTime) {
        this.pid = pid;
        this.time = time;
        this.logicalTime = logicalTime;
    }

    @Override
    public int compareTo(TotallyOrderedTimestamp arg) {
        if (arg.pid == this.pid) {
            //time should be the same so just compare the logical time
            return Long.compare(logicalTime, arg.logicalTime);
        }
        if (arg.time != time) {
            //if the time is different we can safely assume the bigger time is actually the bigger time
            return Long.compare(time, arg.time);
        }
        //if the time of 2 different PIDs is the same just order them by their PID
        return Long.compare(pid, arg.pid);
    }

    public BigInteger asBigInteger() {
        return BigInteger
                //time value
                .valueOf(time)
                //compare between pids (in case the same time but different PID)
                .multiply(BigInteger.valueOf(pid))
                //compare within pid (same PID)
                .multiply(BigInteger.valueOf(logicalTime))
                //add time again in case something was 0
                .add(BigInteger.valueOf(time));
    }

    public long getTimestamp() {
        return time;
    }
}
