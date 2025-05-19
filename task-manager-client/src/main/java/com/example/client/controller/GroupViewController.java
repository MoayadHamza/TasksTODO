package com.example.client.controller;

import com.example.client.model.PGroup;
import com.example.client.model.Project;
import com.example.client.model.User;
import com.example.client.session.Session;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class GroupViewController {

    @FXML private ComboBox<Project> projectComboBox;
    @FXML private Label groupTitleLabel;
    @FXML private Label groupDescriptionLabel;
    @FXML private StackPane mainContentArea;
    @FXML private Button editGroupBtn;
    @FXML private Button deleteGroupBtn;
    @FXML private Button leaveGroupBtn;
    @FXML private Button viewMembersBtn;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private PGroup currentGroup;

    private MainController mainController;
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    public void initialize() {
        this.currentGroup = Session.getCurrentGroup();
        loadHomeGroupPage();

        groupTitleLabel.setText(currentGroup.getName());
        groupDescriptionLabel.setText(currentGroup.getDescription());

        boolean isAdmin = currentGroup.getAdmin().getId().equals(Session.getCurrentUser().getId());
        editGroupBtn.setVisible(isAdmin); // not safe at all
        deleteGroupBtn.setVisible(isAdmin); // note safe at all too

        fetchGroupProjects(currentGroup.getId());

        projectComboBox.setOnAction(e -> {
            Project selected = projectComboBox.getValue();
            if (selected != null) loadProjectView(selected);
        });
    }

    private void fetchGroupProjects(Long groupId) {
        String url = "http://localhost:8080/groups/" + groupId + "/projects";
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Failed to load group projects: " + e.getMessage());
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) return;
                assert response.body() != null;
                String body = response.body().string();
                System.out.println("Received JSON: " + body);
                Type listType = new TypeToken<List<Project>>() {}.getType();
                List<Project> projects = gson.fromJson(body, listType);
                response.close();
                Platform.runLater(() -> {
                    projectComboBox.getItems().clear();
                    projectComboBox.getItems().addAll(projects);
                });
            }
        });
    }

    private void loadHomeGroupPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/HomeGroupPage.fxml"));
            Node page = loader.load();
            mainContentArea.getChildren().setAll(page);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
    }

    private void loadProjectView(Project project) {
        try {
            System.out.println("loading the group project: " + project.getName());
            Session.setCurrentProject(project);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ProjectView.fxml"));
            Node view = loader.load();

            ProjectViewController controller = loader.getController();

            controller.setMainController(Session.getMainController()); // if needed, set GroupViewController
            //controller.initialize();

            mainContentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
    }

    @FXML
    private void handleLeaveGroup() {
        /*
        * it's possible to add more thing to this method for advanced security like adding the
        * user id to the path, where in the server side the endpoint first checks if the user
        * is already a member of the group before applying any change, like adding a task,
        * add/remove user, etc...
        * but I will stick to this basic structure because it's just a demo app, no real security
        * is needed.
        * */
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Leave this group?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                String url = "http://localhost:8080/groups/" + currentGroup.getId() + "/removeUser/" + Session.getCurrentUser().getId();
                Request request = new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build();

                client.newCall(request).enqueue(new Callback() {
                    @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        System.out.println("Failed to leave group.");
                    }
                    @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        response.close();
                        Platform.runLater(() -> {
                            mainController.refreshSidebar();
                            // TODO: Return to home or refresh sidebar
                            Session.clearSelections();
                            mainController.loadHomePage();
                        });
                    }
                });
            }
        });
    }

    @FXML
    private void handleDeleteGroup() {
        // same security comments as in the handleLeaveGroup method...
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this group?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                String url = "http://localhost:8080/groups/" + currentGroup.getId();
                Request request = new Request.Builder().url(url).delete().build();

                client.newCall(request).enqueue(new Callback() {
                    @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        System.out.println("Failed to delete group.");
                    }
                    @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        System.out.println(response.body().string());
                        response.close();
                        Platform.runLater(() -> {
                            mainController.refreshSidebar();
                            // TODO: Return to home or refresh sidebar
                            Session.clearSelections();
                            mainController.loadHomePage();
                        });
                    }
                });
            }
        });
    }

    @FXML
    private void handleEditGroup() {
        Region root = (Region) groupTitleLabel.getScene().getRoot();
        root.setEffect(new GaussianBlur(10));

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Group");

        TextField nameField = new TextField(currentGroup.getName());
        TextArea descriptionField = new TextArea(currentGroup.getDescription());

        Button saveDetails = new Button("Save Details");
        saveDetails.setOnAction(e -> {
            currentGroup.setName(nameField.getText().trim());
            currentGroup.setDescription(descriptionField.getText().trim());

            String json = gson.toJson(currentGroup);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            String url = "http://localhost:8080/groups/" + currentGroup.getId();
            Request request = new Request.Builder().url(url).put(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException ex) {
                    System.out.println("Update group failed.");
                }
                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    response.close();
                    Platform.runLater(() -> {
                        groupTitleLabel.setText(currentGroup.getName());
                        groupDescriptionLabel.setText(currentGroup.getDescription());
                        mainController.refreshSidebar();
                        // TODO : refresh or do something
                    });
                }
            });
        });

        // --- Member management section ---
        Label memberLabel = new Label("Manage Members:");
        Button addMember = new Button("Add Member");
        addMember.setOnAction(e -> showUsernameModal(true));

        Button removeMember = new Button("Remove Member");
        removeMember.setOnAction(e -> showUsernameModal(false));

        Button addProjectBtn = new Button("Add Project");
        addProjectBtn.setOnAction(e -> showAddProjectModal());

        VBox layout = new VBox(10,
                addProjectBtn,
                new Label("Group Name:"), nameField,
                new Label("Description:"), descriptionField,
                saveDetails,
                new Separator(),
                memberLabel,
                new HBox(10, addMember, removeMember)
        );
        layout.setStyle("-fx-padding: 20; -fx-background-color: white;");

        dialog.setScene(new Scene(layout, 450, 400));
        dialog.setOnCloseRequest(e -> root.setEffect(null));
        dialog.showAndWait();
    }

    public void handleViewMembers() {
        if (currentGroup == null) return;

        // === Blur background ===
        Region root = (Region) groupTitleLabel.getScene().getRoot();
        root.setEffect(new GaussianBlur(10));

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Group Members");

        VBox layout = new VBox(15);
        layout.setStyle("-fx-padding: 20; -fx-background-color: white;");

        Label header = new Label("Group Members");
        header.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        // Scrollable member list
        VBox memberListBox = new VBox(8);
        ScrollPane scrollPane = new ScrollPane(memberListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);

        // === Load members from group object ===
        Request request = new Request.Builder()
                .url("http://localhost:8080/groups/" + currentGroup.getId() + "/members")
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String body = response.body().string();
                Type listtype = new TypeToken<List<User>>() {}.getType();
                List<User> members = gson.fromJson(body, listtype);
                response.close();

                Platform.runLater(() -> {
                    for (User user : members) {
                        String labelText = user.getFullName();
                        if (user.getId().equals(currentGroup.getAdmin().getId())) {
                            labelText += " (admin)";
                        }
                        Label memberLabel = new Label(labelText);
                        memberLabel.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 6 10; -fx-border-color: #ccc; -fx-font-size: 14;");
                        memberListBox.getChildren().add(memberLabel);
                    }

                    // === Close Button ===
                    Button closeBtn = new Button("Close");
                    closeBtn.setOnAction(e -> {
                        modal.close();
                        root.setEffect(null);
                    });

                    HBox buttonBox = new HBox(closeBtn);
                    buttonBox.setAlignment(Pos.CENTER_RIGHT);

                    layout.getChildren().addAll(header, scrollPane, buttonBox);
                    Scene scene = new Scene(layout, 300, 350);
                    modal.setScene(scene);
                    modal.setOnCloseRequest(e -> root.setEffect(null)); // Ensure blur is removed
                    modal.showAndWait();
                });
            }
        });

    }


    private void showAddProjectModal() {
        TextField nameField = new TextField();
        nameField.setPromptText("Project Name");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Optional description");

        Button saveBtn = new Button("Save");
        Button cancelBtn = new Button("Cancel");

        VBox layout = new VBox(10,
                new Label("New Project Name:"), nameField,
                new Label("Description:"), descriptionField,
                new HBox(10, saveBtn, cancelBtn)
        );
        layout.setStyle("-fx-padding: 20; -fx-background-color: white;");
        ((HBox) layout.getChildren().get(layout.getChildren().size() - 1)).setAlignment(Pos.CENTER_RIGHT);

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(new Scene(layout, 400, 300));
        dialog.setTitle("Create Group Project");

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String desc = descriptionField.getText().trim();

            if (name.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Project name is required.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            Project newProject = new Project();
            newProject.setName(name);
            newProject.setDescription(desc);
            String json = gson.toJson(newProject);

            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            String url = "http://localhost:8080/projects/group/" + currentGroup.getId();

            Request request = new Request.Builder().url(url).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException ex) {
                    System.out.println("Failed to create group project: " + ex.getMessage());
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    response.close();
                    Platform.runLater(() -> {
                        dialog.close();
                        fetchGroupProjects(currentGroup.getId()); // refresh ComboBox
                    });
                }
            });
        });

        cancelBtn.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }


    private void showUsernameModal(boolean isAdd) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(isAdd ? "Add Member" : "Remove Member");
        dialog.setHeaderText(isAdd ? "Enter the username to add:" : "Enter the username to remove:");
        dialog.showAndWait().ifPresent(username -> {
            String action = isAdd ? "addUser" : "removeUser";

            String fetchIdUrl = "http://localhost:8080/users/username/" + username;
            Request fetchUserReq = new Request.Builder().url(fetchIdUrl).get().build();

            client.newCall(fetchUserReq).enqueue(new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    System.out.println("User not found.");
                }
                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        System.out.println("Failed to fetch user.");
                        return;
                    }
                    assert response.body() != null;
                    String body = response.body().string();
                    System.out.println(body);
                    User user = gson.fromJson(body, User.class);
                    if(user == null) {
                        System.out.println("User not found");
                        response.close();
                        return;
                    }
                    response.close();

                    String url = "http://localhost:8080/groups/" + currentGroup.getId() + "/" + action + "/" + user.getId();
                    Request updateRequest = new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build();

                    client.newCall(updateRequest).enqueue(new Callback() {
                        @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            System.out.println("Update failed.");
                        }
                        @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                            response.close();
                        }
                    });
                }
            });
        });
    }
}
