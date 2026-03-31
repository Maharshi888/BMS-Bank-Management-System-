package com.bank;

import com.bank.ui.LoginScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

// CO4: JavaFX GUI Entry Point
public class MainApp extends Application {

    public static final String APP_TITLE = "BMS — Bank Management System";
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle(APP_TITLE);
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.setResizable(true);

        // Start with the login screen
        LoginScreen loginScreen = new LoginScreen(stage);
        Scene scene = new Scene(loginScreen.getRoot(), 900, 620);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
