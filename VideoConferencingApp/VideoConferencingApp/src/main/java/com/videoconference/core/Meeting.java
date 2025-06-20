package com.videoconference.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Meeting {
    private final String meetingId;
    private final String meetingPassword;
    private final User host;
    private final List<Participant> participants;
    private final List<Chat> chats;
    private final Recording recording;

    public Meeting(String meetingId, String meetingPassword, User host, Recording recording) {
        this.meetingId = meetingId;
        this.meetingPassword = meetingPassword;
        this.host = host;
        this.recording = recording;
        // Use thread-safe collections for high concurrency
        this.participants = new CopyOnWriteArrayList<>();
        this.chats = new CopyOnWriteArrayList<>();
    }

    public String getMeetingId() {
        return meetingId;
    }

    public String getMeetingPassword() {
        return meetingPassword;
    }

    public User getHost() {
        return host;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public List<Chat> getChats() {
        return chats;
    }

    public Recording getRecording() {
        return recording;
    }

    // Thread-safe add/remove
    public void addParticipant(Participant participant) {
        participants.add(participant);
    }

    public void removeParticipant(Participant participant) {
        participants.remove(participant);
    }

    public void sendMessage(Chat chat) {
        chats.add(chat);
    }

    // Parallel stream example: get all participant emails
    public List<String> getParticipantEmailsParallel() {
        return participants.parallelStream()
                .map(p -> p.getUser().getEmail())
                .collect(Collectors.toList());
    }

    public void start() {
        System.out.println("Meeting " + meetingId + " started.");
    }

    public void end() {
        System.out.println("Meeting " + meetingId + " ended.");
    }
}
