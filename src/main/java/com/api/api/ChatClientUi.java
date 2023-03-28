package com.api.api;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ChatClientUi extends Application {
    private static final String Host = "localhost";
    private static final int Port = 9000;

    private TextArea messages;
    private TextField input;
    private Button sendButton;
    private TextField name;
    private Button connectButton;
    private Channel channel;

    @Override
    public void start(Stage primaryStage){
        //Создание каркасса
        BorderPane pane = new BorderPane();
        Font font = Font.font("Arial", FontWeight.BOLD, 14);
        messages = new TextArea();
        messages.setEditable(false);
        messages.setFont(font);
        pane.setCenter(messages);

        VBox inputBox = new VBox();
        inputBox.setSpacing(10);
        HBox nameBox = new HBox();
        nameBox.setSpacing(10);
        Label nameLabel = new Label("Имя:");
        nameLabel.setFont(font);
        name = new TextField();
        name.setFont(font);
        nameBox.getChildren().addAll(nameLabel, name);
        inputBox.getChildren().add(nameBox);

        HBox messageBox = new HBox();
        messageBox.setSpacing(10);
        input = new TextField();
        sendButton = new Button("Отправить");
        sendButton.setOnKeyPressed(event ->{
            if(event.getCode()== KeyCode.ENTER){
                sendMessage();
            }
        });
        sendButton.setOnAction(e -> sendMessage());
        messageBox.getChildren().addAll(input, sendButton);
        inputBox.getChildren().add(messageBox);
        connectButton = new Button("Подключиться");
        connectButton.setFont(font);
        connectButton.setOnAction(e -> connectToServer());
        inputBox.getChildren().add(connectButton);

        pane.setBottom(inputBox);

        Scene scene = new Scene(pane, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        Image icon = new Image("file:icon.png");
        primaryStage.getIcons().add(icon);
        primaryStage.setResizable(true);
        primaryStage.setTitle("Простой чат");
        primaryStage.show();
    }

    private void showAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка имени пользователя");
            alert.setHeaderText(null);
            alert.setContentText("Имя пользователя не может быть пустым");
            alert.showAndWait();
        });
    }

    private void connectToServer() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            String name = this.name.getText();
            if (name.isEmpty()) {
                showAlert(
                );
                System.out.println("Имя не может быть пустым.");
                return;
            }
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch){
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new ChatClientHandler(ChatClientUi.this));
                        }
                    });

            channel = bootstrap.connect(Host, Port).sync().channel();

            channel.writeAndFlush("[" + name + "]: подключился к чату.\n");

            connectButton.setDisable(true);
            input.setDisable(false);
            sendButton.setDisable(false);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private void sendMessage() {
        String message = input.getText();
        if (!message.isEmpty()) {
            channel.writeAndFlush("[" + name.getText() + "]: " + message );
            input.clear();
        }
    }
    void appendMessage(String message) {
        Platform.runLater(() -> messages.appendText("\n" + message));
    }
}