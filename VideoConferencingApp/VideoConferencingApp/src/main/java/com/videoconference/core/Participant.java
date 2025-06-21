package com.videoconference.core;

public class Participant {
    private final String participantId;
    private final User user;
    private boolean audioMuted = false;
    private boolean videoMuted = false;
    private long duration; // 以分钟为单位，或者你可以使用秒等

    public Participant(String participantId, User user) {
        this.participantId = participantId;
        this.user = user;
        this.duration = 0; // 默认值为 0
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void joinMeeting() {
        System.out.println(user.getEmail() + " joined the meeting.");
    }

    public void leaveMeeting() {
        System.out.println(user.getEmail() + " left the meeting.");
    }

    // Thread-safe mute methods
    public synchronized void muteAudio() {
        audioMuted = true;
        System.out.println(user.getEmail() + " muted audio.");
    }

    public synchronized void muteVideo() {
        videoMuted = true;
        System.out.println(user.getEmail() + " muted video.");
    }

    public String getParticipantId() {
        return participantId;
    }

    public User getUser() {
        return user;
    }

    public synchronized boolean isAudioMuted() {
        return audioMuted;
    }

    public synchronized boolean isVideoMuted() {
        return videoMuted;
    }
}
