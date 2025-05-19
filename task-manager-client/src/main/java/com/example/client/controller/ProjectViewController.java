package com.example.client.controller;

import com.example.client.model.ImportanceLevel;
import com.example.client.model.Project;
import com.example.client.model.Task;
import com.example.client.model.TaskStatus;
import com.example.client.session.Session;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ProjectViewController {

    @FXML private Label projectTitleLabel;
    @FXML private Label projectDescriptionLabel;
    @FXML private VBox todoBox;
    @FXML private VBox inProgressBox;
    @FXML private VBox doneBox;

    private MainController mainController;
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    private Project currentProject;
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final String exitStyle = "-fx-background-color: transparent; -fx-border-color: transparent;";
    private final String enterStyle = "-fx-background-color: #e0e0e0; -fx-border-color: #888; -fx-border-width: 2;";

    public void initialize() {
        currentProject = Session.getCurrentProject();
        projectTitleLabel.setText(currentProject.getName());
        projectDescriptionLabel.setFont(Font.font(14));
        projectDescriptionLabel.setText(currentProject.getDescription() != null ? currentProject.getDescription() : "");

        todoBox.setOnMouseEntered(e -> todoBox.setStyle(enterStyle));
        todoBox.setOnMouseExited(e -> todoBox.setStyle(exitStyle));
        inProgressBox.setOnMouseEntered(e -> inProgressBox.setStyle(enterStyle));
        inProgressBox.setOnMouseExited(e -> inProgressBox.setStyle(exitStyle));
        doneBox.setOnMouseEntered(e -> doneBox.setStyle(enterStyle));
        doneBox.setOnMouseExited(e -> doneBox.setStyle(exitStyle));

        loadTasks();
    }

    private void loadTasks() {
        todoBox.getChildren().clear();
        inProgressBox.getChildren().clear();
        doneBox.getChildren().clear();

        String url = "http://localhost:8080/projects/" + currentProject.getId() + "/tasks";
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.out.println("Failed to load tasks");
                    return;
                }

                String responseBody = response.body().string();
                Type listType = new TypeToken<List<Task>>(){}.getType();
                List<Task> tasks = gson.fromJson(responseBody, listType);
                response.close();

                Platform.runLater(() -> {
                    for (Task task : tasks) {
                        Label taskLabel = createStyledTaskLabel(task);
                        switch (task.getCompleted()) {
                            case TODO -> todoBox.getChildren().add(taskLabel);
                            case IN_PROGRESS -> inProgressBox.getChildren().add(taskLabel);
                            case DONE -> doneBox.getChildren().add(taskLabel);
                        }
                    }
                });
            }
        });
    }


    private Label createStyledTaskLabel(Task task) {
        String color;
        switch (task.getImportance()) {
            case LOW -> color = "#2ecc71";     // green
            case MED -> color = "#f1c40f";     // yellow
            case HIGH -> color = "#e74c3c";    // red
            default -> color = "#bdc3c7";
        }

        Label circle = new Label("●");
        circle.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px;");

        Label title = new Label(task.getTitle());
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(title, Priority.ALWAYS);
        title.setStyle("-fx-text-fill: black; -fx-font-size: 13px;");

        HBox wrapper = new HBox(5, circle, title);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 6 10; -fx-border-color: #ccc;");
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setOnMouseClicked(e -> {
            Session.setCurrentTask(task);
            showTaskDetailsModal(task);
        });
        return new Label("", wrapper); // wrap HBox in a dummy Label
    }

    private void showTaskDetailsModal(Task task) {
        Region root = (Region) projectTitleLabel.getScene().getRoot();
        root.setEffect(new GaussianBlur(10));

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Task Details");

        Label titleLabel = new Label();
        Label descLabel = new Label();
        Label impLabel = new Label();
        Label statusLabel = new Label();

        Runnable updateLabels = () -> {
            titleLabel.setText(task.getTitle());
            descLabel.setText(task.getDescription());
            impLabel.setText("Importance: " + task.getImportance());
            statusLabel.setText("Status: " + task.getCompleted());

            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
            descLabel.setFont(Font.font("System", 14));
            descLabel.setWrapText(true);
        };
        updateLabels.run(); // initialize

        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        Pane HExpandPane = new Pane();
        HBox.setHgrow(HExpandPane, Priority.ALWAYS);
        Button closeBtn = new Button("Close");

        editBtn.setOnAction(e -> {
            //popup.close();
            //root.setEffect(null);
            showEditTaskModal(task, (Region) titleLabel.getScene().getRoot(), updateLabels);
        });

        deleteBtn.setOnAction(e -> {
            popup.close();
            root.setEffect(null);
            confirmDeleteTask(task);
        });

        closeBtn.setOnAction(e -> {
            popup.close();
            Session.setCurrentTask(null); // remove from current selected task
            root.setEffect(null);
        });

        HBox buttonBox = new HBox(10, editBtn, deleteBtn, HExpandPane, closeBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Pane VExpandePane = new Pane();
        VBox.setVgrow(VExpandePane, Priority.ALWAYS);

        Separator separator = new Separator(Orientation.HORIZONTAL);

        VBox layout = new VBox(10, titleLabel, descLabel, separator, impLabel, statusLabel, VExpandePane, buttonBox);
        layout.setStyle("-fx-padding: 20; -fx-background-color: white;");

        popup.setScene(new Scene(layout, 550, 400));
        popup.setOnCloseRequest(e -> root.setEffect(null));
        popup.showAndWait();
    }

    private void showEditTaskModal(Task task, Region father, Runnable onSuccess) {

        father.setEffect(new GaussianBlur(10));

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Task");

        TextField titleField = new TextField(task.getTitle());
        TextArea descArea = new TextArea(task.getDescription());
        descArea.setWrapText(true);

        ComboBox<ImportanceLevel> importanceBox = new ComboBox<>();
        importanceBox.getItems().addAll(ImportanceLevel.values());
        importanceBox.setValue(task.getImportance());

        ComboBox<TaskStatus> statusBox = new ComboBox<>();
        statusBox.getItems().addAll(TaskStatus.values());
        statusBox.setValue(task.getCompleted());

        Button saveBtn = new Button("Save");
        Button cancelBtn = new Button("Cancel");

        saveBtn.setOnAction(e -> {
            task.setTitle(titleField.getText().trim());
            task.setDescription(descArea.getText().trim());
            task.setImportance(importanceBox.getValue());
            task.setCompleted(statusBox.getValue());

            String json = gson.toJson(task);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url("http://localhost:8080/tasks/" + task.getId())
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException ex) {
                    System.out.println(ex.getMessage());
                    //ex.printStackTrace();
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    response.close();
                    Platform.runLater(() -> {
                        dialog.close();
                        father.setEffect(null);
                        loadTasks();
                        if(onSuccess != null) onSuccess.run();
                    });
                }
            });
        });

        cancelBtn.setOnAction(e -> {
            dialog.close();
            father.setEffect(null);
        });

        VBox layout = new VBox(10,
                new Label("Title:"), titleField,
                new Label("Description:"), descArea,
                new Label("Importance:"), importanceBox,
                new Label("Status:"), statusBox,
                new HBox(10, saveBtn, cancelBtn)
        );
        layout.setStyle("-fx-padding: 20; -fx-background-color: white;");
        ((HBox) layout.getChildren().get(layout.getChildren().size() - 1)).setAlignment(Pos.CENTER_RIGHT);

        Scene scene = new Scene(layout, 530, 350);
        dialog.setScene(scene);
        dialog.setOnCloseRequest(e -> father.setEffect(null));
        dialog.showAndWait();
    }

    private void confirmDeleteTask(Task task) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this task?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                String url = "http://localhost:8080/tasks/" + task.getId();
                Request request = new Request.Builder().url(url).delete().build();
                client.newCall(request).enqueue(new Callback() {
                    @Override public void onFailure(@NotNull Call call, @NotNull IOException ex) {
                        System.out.println("ERROR: " + ex.getMessage());
                    }

                    @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        response.close();
                        Session.setCurrentTask(null);
                        Platform.runLater(ProjectViewController.this::loadTasks);
                    }
                });
            }
        });
    }

    public void handleEditProject() {
        // Get the scene's root to apply the blur
        Region root = (Region) projectTitleLabel.getScene().getRoot();
        GaussianBlur blur = new GaussianBlur(10);
        root.setEffect(blur);

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Edit Project");

        TextField nameField = new TextField(currentProject.getName());
        nameField.setPromptText("Project Name");

        TextArea descriptionArea = new TextArea(currentProject.getDescription());
        descriptionArea.setPromptText("Project Description");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button saveBtn = new Button("Save");
        Button cancelBtn = new Button("Cancel");

        saveBtn.setOnAction(e -> {
            String newName = nameField.getText().trim();
            String newDesc = descriptionArea.getText().trim();

            if (newName.isEmpty()) {
                errorLabel.setText("Project name is required.");
                return;
            }

            currentProject.setName(newName);
            currentProject.setDescription(newDesc);

            updateProjectOnServer(currentProject);
            popupStage.close();
            refreshView();
            mainController.refreshSidebar();
            root.setEffect(null); // Remove blur
        });

        cancelBtn.setOnAction(e -> {
            popupStage.close();
            root.setEffect(null); // Remove blur
        });

        VBox layout = new VBox(10,
                new Label("Name:"), nameField,
                new Label("Description:"), descriptionArea,
                errorLabel,
                new HBox(10, saveBtn, cancelBtn)
        );
        layout.setStyle("-fx-padding: 20; -fx-background-color: white;");
        ((HBox) layout.getChildren().get(layout.getChildren().size() - 1)).setAlignment(Pos.CENTER_RIGHT);

        Scene scene = new Scene(layout, 400, 300);
        popupStage.setScene(scene);
        popupStage.setOnCloseRequest(e -> root.setEffect(null)); // Safety: remove blur if user clicks X
        popupStage.showAndWait();
    }


    private void updateProjectOnServer(Project project) {
        String url = "http://localhost:8080/projects/" + project.getId();
        String json = gson.toJson(project);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("ERROR: " + e.getMessage());
                //e.printStackTrace();
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                response.close();
            }
        });
    }

    public void handleDeleteProject() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this project?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                System.out.println("deleting project (id:" + currentProject.getId() + ")...");
                // Optionally refresh sidebar or view
                String url = "http://localhost:8080/projects/" + currentProject.getId();
                Request request = new Request.Builder().url(url).delete().build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        System.out.println("ERROR:\n" + e.getMessage());
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        System.out.println("Project deleted!");
                        Platform.runLater(() -> {
                            mainController.refreshSidebar();
                        });
                        response.close();
                    }
                });

            }
        });
    }

    public void handleAddTask() {
        // === Apply blur to background ===
        Region root = (Region) projectTitleLabel.getScene().getRoot();
        GaussianBlur blur = new GaussianBlur(10);
        root.setEffect(blur);

        // === Create custom modal stage ===
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create New Task");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-background-color: white;");

        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setWrapText(true);

        ComboBox<String> importanceBox = new ComboBox<>();
        importanceBox.getItems().addAll("LOW", "MED", "HIGH");
        importanceBox.setPromptText("Importance");

        HBox buttons = new HBox(10);
        Button saveBtn = new Button("Save");
        Button cancelBtn = new Button("Cancel");
        buttons.getChildren().addAll(saveBtn, cancelBtn);

        layout.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Description:"), descriptionArea,
                new Label("Importance:"), importanceBox,
                buttons
        );

        // === Save logic ===
        saveBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            String desc = descriptionArea.getText().trim();
            String importance = importanceBox.getValue();

            if (title.isEmpty() || importance == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Title and importance are required.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            Task newTask = new Task();
            newTask.setTitle(title);
            newTask.setDescription(desc);
            newTask.setImportance(ImportanceLevel.valueOf(importance));
            newTask.setCompleted(TaskStatus.valueOf("TODO")); // default
            newTask.setUser(Session.getCurrentUser());
            //newTask.setProject(currentProject);

            String json = gson.toJson(newTask);

            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url("http://localhost:8080/tasks/project/" + currentProject.getId())
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException ex) {
                    System.out.println("ERROR: " + ex.getMessage());
                    //ex.printStackTrace();
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    response.close();
                    if (response.isSuccessful()) {
                        javafx.application.Platform.runLater(() -> {
                            dialog.close();
                            projectTitleLabel.getScene().getRoot().setEffect(null); // remove blur
                            loadTasks(); // refresh list
                        });
                    }
                }
            });
        });

        cancelBtn.setOnAction(e -> {
            dialog.close();
            projectTitleLabel.getScene().getRoot().setEffect(null); // remove blur
        });

        Scene scene = new Scene(layout, 500, 350);
        dialog.setScene(scene);

        dialog.setOnCloseRequest(e -> root.setEffect(null));
        dialog.show();
    }

    private void    showTaskPopup(Task task) {
        Alert popup = new Alert(Alert.AlertType.INFORMATION);
        popup.setTitle("Task Details");
        popup.setHeaderText(task.getTitle());
        popup.setContentText(
                "Title: " + task.getTitle() + "\n" +
                "Description: " + task.getDescription() + "\n" +
                        "Importance: " + task.getImportance() + "\n" +
                        "Status: " + task.getCompleted()
        );
        popup.showAndWait();
    }

    private void refreshView() {
        projectTitleLabel.setText(currentProject.getName());
        projectDescriptionLabel.setText(currentProject.getDescription());
        loadTasks();
    }


}