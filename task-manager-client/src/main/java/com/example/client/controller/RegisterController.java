package com.example.client.controller;

import com.example.client.model.User;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button regButton;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    @FXML
    private void initialize() {
        // Prevent spaces in username
        usernameField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (event.getCharacter().equals(" ")) {
                event.consume(); // block the space
                messageLabel.setTextFill(Color.RED);
                messageLabel.setText("Spaces are not allowed in username.");
            }
        });

        // Prevent spaces in password
        passwordField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (event.getCharacter().equals(" ")) {
                event.consume(); // block the space
                messageLabel.setTextFill(Color.RED);
                messageLabel.setText("Spaces are not allowed in password.");
            }
        });
        regButton.setDefaultButton(true);
    }


    @FXML
    public void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText();
        String password = passwordField.getText();

        messageLabel.setText("");

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("All fields are required.");
            return;
        }

        User user = new User(null, fullName, username, password);
        String json = gson.toJson(user);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        String REGISTER_URL = "http://localhost:8080/users/register";
        Request request = new Request.Builder()
                .url(REGISTER_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                showMessage("Failed to connect to server.", Color.RED);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    if (responseBody.contains("already taken")) {
                        showMessage("Username is already taken.", Color.RED);
                    } else {
                        showMessage("Registration successful!", Color.GREEN);
                        clearFields();
                    }
                } else {
                    showMessage("Registration failed: " + responseBody, Color.RED);
                }
                response.close();
            }
        });
    }


    private void showMessage(String text, Color color) {
        javafx.application.Platform.runLater(() -> {
            messageLabel.setTextFill(color);
            messageLabel.setText(text);
        });
    }

    private void clearFields() {
        javafx.application.Platform.runLater(() -> {
            fullNameField.clear();
            usernameField.clear();
            passwordField.clear();
        });
    }
}
