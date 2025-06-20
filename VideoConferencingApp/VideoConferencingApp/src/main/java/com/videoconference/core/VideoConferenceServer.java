package com.videoconference.core;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class VideoConferenceServer {
    private static final int PORT = 12345;
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private static final List<String> participants = new CopyOnWriteArrayList<>();
    private static final List<String> chats = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Video Conference Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                new Thread(handler).start();
            }
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String userEmail = "";

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    handleCommand(line);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + userEmail);
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
                clients.remove(this);
                if (!userEmail.isEmpty()) {
                    participants.remove(userEmail);
                    broadcast("PARTICIPANTS " + String.join(",", participants));
                    String leaveMsg = userEmail + " left the meeting.";
                    chats.add(leaveMsg);
                    broadcast("CHAT " + leaveMsg);
                }
            }
        }

        private void handleCommand(String line) {
            if (line.startsWith("JOIN ")) {
                userEmail = line.substring(5);
                if (!participants.contains(userEmail)) {
                    participants.add(userEmail);
                    broadcast("PARTICIPANTS " + String.join(",", participants));
                    String joinMsg = userEmail + " joined the meeting.";
                    chats.add(joinMsg);
                    broadcast("CHAT " + joinMsg);
                }
            } else if (line.startsWith("MESSAGE ")) {
                String msg = userEmail + ": " + line.substring(8);
                chats.add(msg);
                broadcast("CHAT " + msg);
            } else if (line.equals("PARTICIPANTS")) {
                out.println("PARTICIPANTS " + String.join(",", participants));
            } else if (line.equals("CHATS")) {
                for (String chat : chats) {
                    out.println("CHAT " + chat);
                }
            } else if (line.startsWith("VIDEO_STARTED ")) {
                String email = line.substring("VIDEO_STARTED ".length());
                String notice = "NOTICE: " + email + " has started their video.";
                chats.add(notice);
                broadcast("CHAT " + notice);

            } else if (line.startsWith("VIDEO_STOPPED ")) {
                String email = line.substring("VIDEO_STOPPED ".length());
                String notice = "NOTICE: " + email + " has stopped their video.";
                chats.add(notice);
                broadcast("CHAT " + notice);
            }else if (line.startsWith("MIC_MUTED ")) {
                String email = line.substring("MIC_MUTED ".length());
                String notice = "NOTICE: " + email + " has muted their mic.";
                chats.add(notice);
                broadcast("CHAT " + notice);
            } else if (line.startsWith("MIC_UNMUTED ")) {
                String email = line.substring("MIC_UNMUTED ".length());
                String notice = "NOTICE: " + email + " has unmuted their mic.";
                chats.add(notice);
                broadcast("CHAT " + notice);
            }

        }

        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                try {
                    client.out.println(message);
                } catch (Exception e) {
                    System.err.println("Failed to send message to " + client.userEmail);
                }
            }
        }
    }
        }
