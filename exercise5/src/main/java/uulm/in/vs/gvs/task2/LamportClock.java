package uulm.in.vs.gvs.task2;

public class LamportClock implements Comparable<LamportClock>{
    long internalTime = 0;

    public LamportClock() {}

    public LamportClock(long init) {
        this.internalTime = init;
    }

    public long getTime() {
        return  internalTime;
    }

    /**
    * Also returns incremented time.
    */
    public long increment() {
        internalTime ++;
        return internalTime;
    }

    public long merge(LamportClock b) {
        if(b.internalTime > this.internalTime) {
            this.internalTime = b.internalTime;
        }
        return increment();
    }

    public static LamportClock merge(LamportClock a, LamportClock b) {
        LamportClock clock = new LamportClock(Math.max(a.internalTime, b.internalTime));
        clock.increment();
        return clock;
    }

    public static int compare(LamportClock a, LamportClock b) {
        return Long.compare(a.internalTime, b.internalTime);
    }

    public boolean equals(LamportClock b) {
        return LamportClock.compare(this, b) == 0;
    }

    @Override
    public int compareTo(LamportClock l) {
        return  LamportClock.compare(this, l);
    }
}
