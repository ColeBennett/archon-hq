package net.thearchon.hq.app;

public abstract class AbstractCheck implements Check {

    private final int interval;

    public AbstractCheck(int interval) {
        this.interval = interval;
    }

    @Override
    public int getInterval() {
        return interval;
    }
}
