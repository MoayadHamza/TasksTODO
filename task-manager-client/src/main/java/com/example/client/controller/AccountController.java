package com.example.client.controller;

import com.example.client.model.User;
import com.example.client.session.Session;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AccountController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button updateButton;

    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "http://localhost:8080/users/";

    @FXML
    public void initialize() {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            fullNameField.setText(currentUser.getFullName());
            usernameField.setText(currentUser.getUsername());
            usernameField.setDisable(true); // Make username unchangeable
        }

        // Prevent spaces in password
        passwordField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (event.getCharacter().equals(" ")) {
                event.consume(); // block the space
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Spaces are not allowed in password.");
            }
        });
    }

    @FXML
    public void handleUpdate() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        String updatedFullName = fullNameField.getText().trim();
        String newPassword = passwordField.getText().replaceAll("\\s+", ""); // remove spaces

        if (updatedFullName.isEmpty()) {
            showMessage("Full name cannot be empty.", Color.RED);
            return;
        }

        currentUser.setFullName(updatedFullName);
        if (!newPassword.isEmpty()) {
            currentUser.setPassword(newPassword);
        }

        RequestBody body = RequestBody.create(
                gson.toJson(currentUser),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + currentUser.getId())
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                showMessage("Error: " + e.getMessage(), Color.RED);
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    showMessage("Account updated successfully!", Color.GREEN);
                    passwordField.clear();
                } else {
                    showMessage("Update failed.", Color.RED);
                }
                response.close();
            }
        });
    }

    @FXML
    public void handleDelete() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Are you sure you want to delete your account?");
        confirmAlert.setContentText("This action cannot be undone and all your personal tasks will be permanently deleted.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Request request = new Request.Builder()
                        .url(BASE_URL + currentUser.getId())
                        .delete()
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        showMessage("Error: " + e.getMessage(), Color.RED);
                    }

                    @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                        if (response.isSuccessful()) {
                            Session.clear();
                            showMessage("Account deleted.", Color.GREEN);
                            redirectToLogin();
                        } else {
                            showMessage("Deletion failed.", Color.RED);
                        }
                        response.close();
                    }
                });
            }
        });
    }


    private void showMessage(String msg, Color color) {
        javafx.application.Platform.runLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setTextFill(color);
        });
    }

    private void redirectToLogin() {
        javafx.application.Platform.runLater(() -> {
            try {
                // open login/register stage
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/MainAuthView.fxml"));
                Scene scene = new Scene(loader.load());
                Stage primaryStage = new Stage();
                primaryStage.setScene(scene);
                primaryStage.setTitle("Login or Register");
                primaryStage.show();

                // close main stage
                Stage currentStage = (Stage) fullNameField.getScene().getWindow();
                currentStage.close();
            } catch (IOException e) {
                showMessage("Failed to redirect to login.", Color.RED);
            }
        });
    }
}
