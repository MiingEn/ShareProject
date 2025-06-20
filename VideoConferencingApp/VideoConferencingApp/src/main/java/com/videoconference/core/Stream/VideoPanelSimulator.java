package com.videoconference.core.Stream;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;


public class VideoPanelSimulator implements Runnable {
    private volatile boolean running = true;
    private volatile boolean micEnabled = true;
    private final String userEmail;
    private final ImageView videoView;
    private final Label timeLabel;       // 显示时间

    public VideoPanelSimulator(String userEmail, ImageView videoView, Label timeLabel)
    {
        this.userEmail = userEmail;
        this.videoView = videoView;
        this.timeLabel = timeLabel;

    }

    public void setMicEnabled(boolean enabled) {
        if (this.micEnabled != enabled) {
            this.micEnabled = enabled;
        }
    }

    public void stop() {
        running = false;
    }


    @Override
    public void run() {
        int counter = 0;
        long startTime = System.currentTimeMillis();

        while (running) {
            WritableImage image = new WritableImage(320, 240);
            PixelWriter writer = image.getPixelWriter();
            for (int y = 0; y < 240; y++) {
                for (int x = 0; x < 320; x++) {
                    Color color = (x + y + counter) % 2 == 0 ? Color.LIGHTBLUE : Color.LIGHTGRAY;
                    writer.setColor(x, y, color);
                }
            }

            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            Platform.runLater(() -> {
                videoView.setImage(image);
                timeLabel.setText("Streaming for " + elapsedSeconds + " seconds");
            });

            counter++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}





