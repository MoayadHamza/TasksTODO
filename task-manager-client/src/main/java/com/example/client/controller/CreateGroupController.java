package com.example.client.controller;

import com.example.client.model.PGroup;
import com.example.client.model.User;
import com.example.client.session.Session;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CreateGroupController {

    @FXML private TextField groupNameField;
    @FXML private TextArea descriptionField;
    @FXML private Label errorLabel;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private MainController mainController;
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    public void handleCreateGroup() {
        String name = groupNameField.getText().trim();
        String description = descriptionField.getText().trim();
        errorLabel.setText("");

        if (name.isEmpty()) {
            errorLabel.setText("Group name is required.");
            return;
        }

        User currentUser = Session.getCurrentUser();
        PGroup group = new PGroup(null, currentUser, name, description);  // No ID on creation

        String json = gson.toJson(group);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://localhost:8080/groups/create")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                javafx.application.Platform.runLater(() -> errorLabel.setText("Connection failed."));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                javafx.application.Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        errorLabel.setTextFill(Color.GREEN);
                        //errorLabel.setTextFill();
                        errorLabel.setText("Group created!");

                        Platform.runLater( () -> {
                            groupNameField.clear();
                            descriptionField.clear();
                            if (mainController != null ) {
                                mainController.refreshSidebar();
                            }
                        });
                    } else {
                        errorLabel.setText("Group creation failed.");
                    }
                });
                response.close();
            }
        });
    }
}
