package com.example.client.controller;

import com.example.client.model.Project;
import com.example.client.model.User;
import com.example.client.session.Session;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CreateProjectController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private Label messageLabel;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private MainController mainController;
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    public void handleCreate() {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        if (name.isEmpty()) {
            showMessage("Project name is required.", true);
            return;
        }

        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        Project project = new Project(null, name, description, currentUser, null);

        String json = gson.toJson(project);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:8080/projects")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                showMessage("Error creating project.", true);
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    showMessage("Project created!", false);
                    Platform.runLater(() -> {
                        nameField.clear();
                        descriptionField.clear();
                        if (mainController != null) {
                            mainController.refreshSidebar();
                        }
                    });
                } else {
                    showMessage("Server error: " + response.body().string(), true);
                }
                response.close();
            }
        });
    }

    private void showMessage(String message, boolean isError) {
        Platform.runLater(() -> {
            messageLabel.setText(message);
            messageLabel.setTextFill(isError ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);
        });
    }
}
