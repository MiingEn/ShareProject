package com.videoconference.console;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class VideoConferencingApp {
    public static void main(String[] args) {
        final String SERVER_HOST = "localhost";
        final int SERVER_PORT = 12345;
        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            Scanner scanner = new Scanner(System.in)
        ) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("=== Video Conferencing Console App (Real-Time) ===");
            System.out.print("Enter your email to join: ");
            String email = scanner.nextLine().trim();
            out.println("JOIN " + email);

            // Start a thread to listen for server updates
            Thread listener = new Thread(() -> {
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("PARTICIPANTS ")) {
                            System.out.println("[Participants] " + line.substring(13));
                        } else if (line.startsWith("CHAT ")) {
                            System.out.println("[Chat] " + line.substring(5));
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            listener.setDaemon(true);
            listener.start();

            while (true) {
                System.out.println("\nOptions:");
                System.out.println("1. Send Message");
                System.out.println("2. View Participants");
                System.out.println("3. View Chats");
                System.out.println("0. Exit");
                System.out.print("Choose option: ");
                String option = scanner.nextLine();
                switch (option) {
                    case "1" -> {
                        System.out.print("Enter message: ");
                        String msg = scanner.nextLine();
                        out.println("MESSAGE " + msg);
                    }
                    case "2" -> out.println("PARTICIPANTS");
                    case "3" -> out.println("CHATS");
                    case "0" -> {
                        System.out.println("Exiting application...");
                        socket.close();
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            }
        } catch (IOException e) {
            System.out.println("Could not connect to server: " + e.getMessage());
        }
    }
}
