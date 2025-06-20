package com.videoconference.core.Stream;

public class VideoStreamSimulator implements Runnable {
    private final String userEmail;

    public VideoStreamSimulator(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public void run() {
        int frame = 1;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Simulate sending/receiving a video frame every second
                System.out.println("[VideoStream] " + userEmail + " - Sending frame " + frame++);
                Thread.sleep(1000); // 1 frame per second
            }
        } catch (InterruptedException e) {
            System.out.println("[VideoStream] " + userEmail + " - Video stream stopped.");
            Thread.currentThread().interrupt(); // Preserve interrupt status
        }
    }
}
