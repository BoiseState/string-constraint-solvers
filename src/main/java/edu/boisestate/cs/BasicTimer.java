package edu.boisestate.cs;

public class BasicTimer {

    static private long startTime = 0;
    static private long stopTime = 0;

    static public void start() {
        stopTime = 0;
        startTime = System.currentTimeMillis();
    }

    static public void stop() {
        stopTime = System.currentTimeMillis();
    }

    static public long getRunTime() {
        return stopTime - startTime;
    }
}
