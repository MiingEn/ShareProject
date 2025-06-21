package com.videoconference.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.Comparator;

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
        this.participants = new CopyOnWriteArrayList<>();
        this.chats = new CopyOnWriteArrayList<>();
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
    }

    public void sendMessage(Chat chat) {
        chats.add(chat);
    }

    // g.1 Reduction
    public long getTotalMeetingDuration() {
        return participants.parallelStream()
                .mapToLong(Participant::getDuration)
                .reduce(0L, Long::sum);
    }

    // g.2 Pipeline
    public List<String> getSortedParticipantNames() {
        return participants.parallelStream()
                .map(p -> p.getUser().getName())
                .filter(name -> name != null && !name.isEmpty())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    // g.3 Performance comparison
    public void compareEmailExtractionPerformance() {
        long start = System.nanoTime();
        List<String> sequential = participants.stream()
                .map(p -> p.getUser().getEmail())
                .collect(Collectors.toList());
        long sequentialTime = System.nanoTime() - start;

        start = System.nanoTime();
        List<String> parallel = participants.parallelStream()
                .map(p -> p.getUser().getEmail())
                .collect(Collectors.toList());
        long parallelTime = System.nanoTime() - start;

        System.out.println("Sequential time (ns): " + sequentialTime);
        System.out.println("Parallel time (ns): " + parallelTime);
    }

    public void start() {
        recording.startRecording();
        System.out.println("Meeting " + meetingId + " started.");
    }

    public void end() {
        recording.stopRecording();
        System.out.println("Meeting " + meetingId + " ended.");
    }
}
