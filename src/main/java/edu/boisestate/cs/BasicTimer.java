package edu.boisestate.cs;

public class BasicTimer {

    static private long startTime = 0;
    static private long stopTime = 0;

    static public void start() {
        stopTime = 0;
        startTime = System.nanoTime();
    }

    static public void stop() {
        stopTime = System.nanoTime();
    }

    static public long getRunTime() {
        return (stopTime - startTime) / 1000;
    }
}
