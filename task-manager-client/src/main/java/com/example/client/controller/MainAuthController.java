package com.example.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainAuthController {

    @FXML private StackPane authPane;
    @FXML private Label loginTab;
    @FXML private Label registerTab;


    @FXML
    public void initialize() {
        showLoginView(); // Show login by default
    }

    @FXML
    public void showLoginView() {
        loadView("views/LoginView.fxml");

        loginTab.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 8 0 0 8;");
        registerTab.setStyle("-fx-background-color: #85c1e9; -fx-text-fill: white; -fx-background-radius: 0 8 8 0;");
    }

    @FXML
    public void showRegisterView() {
        loadView("views/RegisterView.fxml");

        loginTab.setStyle("-fx-background-color: #85c1e9; -fx-text-fill: white; -fx-background-radius: 8 0 0 8;");
        registerTab.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 0 8 8 0;");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlPath));
            Node view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof LoginController loginController) {
                loginController.setOnLoginSuccess(this::launchMainApp);
            }

            authPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void launchMainApp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/MainView.fxml"));
            Scene mainScene = new Scene(loader.load());

            Stage mainStage = new Stage();
            mainStage.setScene(mainScene);
            mainStage.setTitle("Task Manager");

            Stage currentStage = (Stage) authPane.getScene().getWindow();
            currentStage.close();

            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
