package com.videoconference.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;
import javafx.scene.image.ImageView;
import javax.sound.sampled.*;
import com.videoconference.core.Stream.VideoPanelSimulator;

public class VideoConferencingAppFX extends Application {
    private volatile boolean isMuted = false;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String userEmail = "";
    private Thread videoThread;
    private VideoPanelSimulator videoSimulator;
    private ListView<String> participantList;
    private TextArea chatArea;
    private TextField messageField;
    private Label joinStatusLabel;
    private Button joinBtn;
    private ToggleButton micToggle;

    private TargetDataLine microphone;

    private void startAudioCapture() {
        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, true);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();

                byte[] buffer = new byte[4096];
                while (true) {
                    if (!isMuted) {
                        int bytesRead = microphone.read(buffer, 0, buffer.length);
                        // 你可以在这里添加音频传输逻辑
                    }
                }
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void muteMicrophone() {
        isMuted = true;
    }

    private void unmuteMicrophone() {
        isMuted = false;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Video Conferencing App (Real-Time)");

        TabPane tabPane = new TabPane();
        Tab joinTab = new Tab("Join Meeting", createJoinMeetingTab());
        Tab chatTab = new Tab("Chat", createChatTab());
        Tab participantsTab = new Tab("Participants", createParticipantsTab());
        Tab videoTab = createVideoTab(); // 👈 使用新的方法

        tabPane.getTabs().addAll(videoTab, joinTab, chatTab, participantsTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabPane, 600, 500);
        scene.getRoot().setStyle(
                "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
                        "-fx-font-size: 15px;" +
                        "-fx-background-color: #f4f6fb;"
        );

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    @Override
    public void stop() throws Exception {
        if (videoSimulator != null) videoSimulator.stop();
        if (socket != null && !socket.isClosed()) socket.close();
        super.stop();
    }

    private Tab createVideoTab() {
        ImageView videoView = new ImageView();
        Label timeLabel = new Label("Streaming for 0 sec");
        StackPane videoPane = new StackPane(videoView, timeLabel);
        videoView.setFitWidth(320);
        videoView.setFitHeight(240);
        videoView.setPreserveRatio(true);
        videoView.setStyle("-fx-border-color: black; -fx-border-width: 2;");

        Label statusLabel = new Label("Waiting to join meeting...");

        Button startVideoBtn = new Button("Start Video");
        startVideoBtn.setStyle("-fx-background-color: #4f8cff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        startVideoBtn.setOnAction(e -> {
            if (!userEmail.isEmpty()) {
                if (out != null) {
                    out.println("VIDEO_STARTED " + userEmail);
                }

                statusLabel.setText("Streaming as: " + userEmail);
                videoSimulator = new VideoPanelSimulator(userEmail, videoView, timeLabel);
                videoThread = new Thread(videoSimulator);
                videoThread.start();
                startVideoBtn.setDisable(true);
            } else {
                statusLabel.setText("Please join meeting first.");
            }
        });

        Button stopVideoBtn = new Button("Stop Video");
        stopVideoBtn.setStyle("-fx-background-color: #ff4f4f; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        stopVideoBtn.setOnAction(e -> {
            if (videoSimulator != null) {
                videoSimulator.stop();
            }
            videoView.setImage(null);
            timeLabel.setText("Streaming for 0 sec");
            statusLabel.setText("Video streaming has stopped.");
            startVideoBtn.setDisable(false);

            if (out != null && userEmail != null) {
                out.println("VIDEO_STOPPED " + userEmail);
            } else {
                System.err.println("Cannot send stop video command — missing output stream or userEmail.");
            }
        });

        ToggleButton micToggleBtn = new ToggleButton("Mute Mic");
        micToggleBtn.setStyle("-fx-background-radius: 8; -fx-font-weight: bold;");

        micToggleBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Microphone Status");
            alert.setHeaderText(null);

            if (micToggleBtn.isSelected()) {
                micToggleBtn.setText("Unmute Mic");
                muteMicrophone();
                if (videoSimulator != null) {
                    videoSimulator.setMicEnabled(false);
                }

                // 添加：发送 MIC_MUTED 指令
                out.println("MIC_MUTED " + userEmail);

                alert.setContentText("Your mic is already muted.");
            } else {
                micToggleBtn.setText("Mute Mic");
                unmuteMicrophone();
                if (videoSimulator != null) {
                    videoSimulator.setMicEnabled(true);
                }

                // 添加：发送 MIC_UNMUTED 指令
                out.println("MIC_UNMUTED " + userEmail);

                alert.setContentText("Your mic is open.");
            }
        });

        VBox leftPane = new VBox(10, videoPane, new HBox(10, startVideoBtn, stopVideoBtn), micToggleBtn, statusLabel);
        HBox content = new HBox(20, leftPane);
        content.setPadding(new Insets(15));

        Tab videoTab = new Tab("Video");
        videoTab.setContent(content);
        return videoTab;
    }

    private VBox createJoinMeetingTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        micToggle = new ToggleButton("Mute Mic");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #b0b8c1;");

        joinBtn = new Button("Join Meeting");
        joinBtn.setStyle("-fx-background-color: #4f8cff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        joinStatusLabel = new Label();

        joinBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                joinStatusLabel.setText("Please enter your email.");
                return;
            }
            userEmail = email;
            connectToServerAndJoin();
            startAudioCapture();  // ✅ 开始音频录制
        });

        vbox.getChildren().addAll(emailField, joinBtn, joinStatusLabel);
        return vbox;
    }

    private VBox createChatTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #b0b8c1;");

        messageField = new TextField();
        messageField.setPromptText("Type your message here...");
        messageField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #b0b8c1;");

        Button sendBtn = new Button("Send Message");
        sendBtn.setStyle("-fx-background-color: #4f8cff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        sendBtn.setOnAction(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty() && out != null) {
                out.println("MESSAGE " + message);
                messageField.clear();
            }
        });

        vbox.getChildren().addAll(chatArea, messageField, sendBtn);
        return vbox;
    }

    private VBox createParticipantsTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        participantList = new ListView<>();
        participantList.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #b0b8c1;");
        Button refreshBtn = new Button("Refresh List");
        refreshBtn.setStyle("-fx-background-color: #4f8cff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        refreshBtn.setOnAction(e -> {
            if (out != null) out.println("PARTICIPANTS");
        });
        vbox.getChildren().addAll(participantList, refreshBtn);
        return vbox;
    }

    private void connectToServerAndJoin() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 12345);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out.println("JOIN " + userEmail);
                Platform.runLater(() -> {
                    joinStatusLabel.setText("Joined as " + userEmail);
                    joinBtn.setDisable(true);

                });
                listenForServerUpdates();
            } catch (IOException e) {
                Platform.runLater(() -> joinStatusLabel.setText("Failed to connect: " + e.getMessage()));
            }
        }).start();
    }

    private void listenForServerUpdates() {
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() -> {
                        if (finalLine.startsWith("NOTICE ")) {
                            showAlert("Notification", finalLine.substring(7));
                        } else if (finalLine.startsWith("CHAT ")) {
                            String msg = finalLine.substring(5);
                            chatArea.appendText(msg + "\n");

                            if (msg.contains("has stopped their video") || msg.contains("has started their video")) {
                                showAlert("Video Notification", msg);
                            }
                        } else if (finalLine.startsWith("PARTICIPANTS ")) {
                            participantList.getItems().setAll(finalLine.substring(13).split(","));
                        }
                    });
                }
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Error", "Disconnected from server."));
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        System.out.println("showAlert called with message: " + message); // 临时调试用
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

