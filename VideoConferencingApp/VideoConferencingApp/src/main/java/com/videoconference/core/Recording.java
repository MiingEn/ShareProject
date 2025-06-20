package com.videoconference.core;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Recording {
    private final String recordingId;
    private final String meetingId;
    private final File recordingFile;
    private boolean recording = false;
    private boolean recordingStopped = false;
    private final Lock lock = new ReentrantLock();

    public Recording(String recordingId, String meetingId, File recordingFile) {
        this.recordingId = recordingId;
        this.meetingId = meetingId;
        this.recordingFile = recordingFile;
    }

    public String getRecordingId() {
        return recordingId;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public File getRecordingFile() {
        return recordingFile;
    }

    // Use Lock for thread-safe state changes
    public void startRecording() {
        lock.lock();
        try {
            recording = true;
            recordingStopped = false;
            System.out.println("Recording started for meeting: " + meetingId);
        } finally {
            lock.unlock();
        }
    }

    public void stopRecording() {
        lock.lock();
        try {
            recording = false;
            recordingStopped = true;
            System.out.println("Recording stopped for meeting: " + meetingId);
        } finally {
            lock.unlock();
        }
    }

    public boolean isRecording() {
        lock.lock();
        try {
            return recording;
        } finally {
            lock.unlock();
        }
    }

    public boolean isRecordingStopped() {
        lock.lock();
        try {
            return recordingStopped;
        } finally {
            lock.unlock();
        }
    }
}
