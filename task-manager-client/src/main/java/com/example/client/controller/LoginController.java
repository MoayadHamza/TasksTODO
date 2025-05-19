package com.example.client.controller;

import com.example.client.model.User;
import com.example.client.session.Session;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;


    private Runnable onLoginSuccess;

    public void initialize() {
        loginButton.setDefaultButton(true);
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setText("");

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required.");
            return;
        }

        // Create User object with username and password
        User loginUser = new User();
        loginUser.setUsername(username);
        loginUser.setPassword(password);

        // Convert User to JSON
        Gson gson = new Gson();
        String json = gson.toJson(loginUser);

        // Prepare HTTP request
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                json,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("http://localhost:8080/users/login")
                .post(body)
                .build();

        // Execute HTTP request in a background thread
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                showError("Server not reachable.");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    showError("Login failed: " + response.message());
                    return;
                }

                // Expecting JSON response with user info (without password)
                String responseBody = response.body().string();
                User loggedUser = gson.fromJson(responseBody, User.class);

                // Store user in session
                Session.setCurrentUser(loggedUser);

                // Run UI updates on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    if (onLoginSuccess != null) {
                        onLoginSuccess.run();
                    }
                    // Close login window
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.close();
                });
                response.close();
            }

            private void showError(String message) {
                javafx.application.Platform.runLater(() -> errorLabel.setText(message));
            }
        });
    }


}
