package uulm.in.vs.gvs.task1;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author David Mödinger
 *
 */
public class BaseClock implements Runnable, Clock {
    private ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    private volatile long counter;
    private volatile ScheduledFuture<?> timeFuture;
    private volatile long tickrate;

    public BaseClock() {
        this(0);
    }

    public BaseClock(long start) {
        this(start,100);
    }

    public BaseClock(long start, int tickrate) {
        this.counter = start;
        this.tickrate = tickrate;
        this.timeFuture = this.timer.scheduleAtFixedRate(this, tickrate, tickrate, TimeUnit.MILLISECONDS);
    }

    private synchronized void updateTickrate(long speed) {
        // Limit the range of the tickrate
        tickrate = speed < 10 ? 10 : speed;
        tickrate = speed > 1000 ? 1000 : speed;

        // Update Clock
        long delay = timeFuture.getDelay(TimeUnit.MILLISECONDS);
        timeFuture.cancel(true);
        this.timeFuture = this.timer.scheduleAtFixedRate(this, delay, tickrate, TimeUnit.MILLISECONDS);
    }

    /*
    * Methods to gradually decrease/increase the tick rate
    * */
    public void decreaseSpeed() {
        updateTickrate((long)(1.1*tickrate));
    }

    public void increaseSpeed() {
        updateTickrate((long)(0.9*tickrate));
    }

    /*
     * Methods to set new absolute tick rates
     * */
    public void setVeryFastSpeed() {
        updateTickrate(10);
    }

    public void setFastSpeed() {
        updateTickrate(50);
    }

    public void setNormalSpeed() {
        updateTickrate(100);
    }

    public void setSlowSpeed() {
        updateTickrate(200);
    }

    public void setVerySlowSpeed() {
        updateTickrate(1000);
    }

    public void setTimeToFuture(long future) {
        // Only allow future times
        if(future > counter)
            counter = future;
    }

    public long getTickrate() {
        return tickrate;
    }

    public long getTime() {
        return counter;
    }

    public void stop() {
        timeFuture.cancel(true);
    }

    public void restart() {
        updateTickrate(tickrate);
    }

    // Use shutdown once you finished using your Clock.
    public void shutdown() {
        timer.shutdown();
    }

    @Override
    public void run() {
        counter+=100;
    }
}
