package com.videoconference.core.Stream;

import java.util.function.Consumer;

public class AudioStreamSimulator implements Runnable {
    private final String username;
    private final Consumer<String> logConsumer;
    private volatile boolean running = true;
    private volatile boolean micEnabled = true;

    public AudioStreamSimulator(String username, Consumer<String> logConsumer) {
        this.username = username;
        this.logConsumer = logConsumer;
    }

    public void stop() {
        running = false;
    }

    public void setMicEnabled(boolean enabled) {
        this.micEnabled = enabled;
        logConsumer.accept("Microphone for " + username + " has been " + (enabled ? "enabled" : "muted") + ".");
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            if (micEnabled) {
                logConsumer.accept(username + " is streaming audio...");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}


