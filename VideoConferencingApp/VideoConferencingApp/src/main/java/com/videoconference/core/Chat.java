package com.videoconference.core;

public class Chat {
    private final String chatId;
    private final String meetingId;
    private final User sender;
    private final String message;

    public Chat(String chatId, String meetingId, User sender, String message) {
        this.chatId = chatId;
        this.meetingId = meetingId;
        this.sender = sender;
        this.message = message;
    }

    public String getChatId() {
        return chatId;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public User getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    // As per class diagram: sendMessage stub
    public static void sendMessage(Chat chat) {
        // In a real system, this would send the message to the server or meeting
        System.out.println("[Chat] " + chat.getSender().getEmail() + ": " + chat.getMessage());
    }
}
