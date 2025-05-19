package com.example.client.controller;

import com.example.client.model.PGroup;
import com.example.client.model.Project;
import com.example.client.model.User;
import com.example.client.session.Session;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML private Button logoutButton;
    @FXML private StackPane mainContent;
    @FXML private javafx.scene.control.Label homeTab;
    @FXML private javafx.scene.control.Label accountTab;
    @FXML private Button addPersonalProjectBtn;
    @FXML private Button addGroupBtn;
    @FXML private VBox personalProjectsBox;
    @FXML private VBox sidebarVBox;
    @FXML private VBox groupsBox;





    private final String inactiveStyle = "-fx-background-color: #e0e0e0; -fx-text-fill: black; -fx-font-size: 14; -fx-pref-width: 100; -fx-pref-height: 50; -fx-cursor: hand;";
    private final String activeStyle = "-fx-background-color: #d0d0d0; -fx-text-fill: black; -fx-font-size: 14; -fx-pref-width: 100; -fx-pref-height: 50; -fx-cursor: hand;";
    private static final String BASE_URL = "http://localhost:8080";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    // ------------------ HANDLE INITIALIZE -----------------

    @FXML
    public void initialize() {
        loadHomePage();
        logoutButton.setOnAction(e -> handleLogout());
        Session.setMainController(this);
        initializeSidebar();

    }

    public void initializeSidebar() {
        User user = Session.getCurrentUser();
        if (user == null) return;

        initializePersonalProjectsBox(user);
        initializeGroupsBox(user);

    }

    public void initializePersonalProjectsBox(User user) {
        List<Project> personalProjects = fetchPersonalProjects(user.getId());
        displayPersonalProjects(personalProjects);
    }

    public void initializeGroupsBox(User user) {
        List<PGroup> userGroups = fetchUserGroups(user.getId());
        displayGroups(userGroups);
    }

    // ------------------------------------------------------------------

    // ---------------- HANDLE PERSONAL PROJECTS BOX --------------------

    public static List<Project> fetchPersonalProjects(Long userId) {
        String url = BASE_URL + "/projects/user/" + userId;
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Type listType = new TypeToken<List<Project>>(){}.getType();
            if (response.body() == null) throw new IOException("Empty response body");
            String json = response.body().string();
            System.out.println("Received JSON: " + json); // useful during debugging
            return gson.fromJson(json, listType);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void displayPersonalProjects(List<Project> projects) {
        personalProjectsBox.getChildren().clear(); // clear old items

        for (Project project : projects) {
            Label label = new Label(project.getName());
            label.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-padding: 5 10; -fx-cursor: hand;");
            label.setMaxWidth(Double.MAX_VALUE); // fill width
            label.setOnMouseClicked(e -> openProjectView(project)); // handle click

            personalProjectsBox.getChildren().add(label);
        }
    }

    private void openProjectView(Project project) {
        System.out.println("Opening project: " + project.getName());
        Session.clearSelections(); // clear all selections (task/project/group)
        Session.setCurrentProject(project); // set the selected project as current
        loadProjectView();
        // TODO: load and show project view using FXMLLoader
    }

    // ----------------------------------------------------------------

    // ----------------- HANDLE GROUPS BOX ----------------------------

    public static List<PGroup> fetchUserGroups(Long userId) {
        String url = BASE_URL + "/groups/user/" + userId;
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Type listType = new TypeToken<List<PGroup>>(){}.getType();
            if (response.body() == null) throw new IOException("Empty response body");
            String json = response.body().string();
            System.out.println("main controller.fetchusergroups");
            System.out.println("Received JSON: " + json); // useful during debugging
            return gson.fromJson(json, listType);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void displayGroups(List<PGroup> groups) {
        groupsBox.getChildren().clear(); // clear old items

        for (PGroup group : groups) {
            Label label = new Label(group.getId() + " " + group.getName());
            label.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-padding: 5 10; -fx-cursor: hand;");
            label.setMaxWidth(Double.MAX_VALUE); // fill width
            label.setOnMouseClicked(e -> openGroupView(group)); // handle click

            groupsBox.getChildren().add(label);
        }
    }

    public void openGroupView(PGroup group) {
        System.out.println("Opening Group: " + group.getName() + " | ID: " + group.getId());
        Session.clearSelections();
        Session.setCurrentGroup(group);
        loadGroupView();
        // TODO: load and show project view using FXMLLoader
    }

    // ----------------------------------------------------------

    // ------------------- HANDLE BUTTONS -----------------------

    public void onAddPersonalProject() {
        loadCreatePersonalProjectPage();
    }

    public void onAddGroupProject() {
        loadCreateGroupPage();
    }

    private void handleLogout() {
        System.out.println("@Logged out:\n" + Session.getCurrentUser());
        Session.clear();
        redirectToLogin();
    }

    // --------------------------------------------------------

    // ---------------- HANDLE LOADING PAGES ------------------

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlPath));
            Node page = loader.load();
            mainContent.getChildren().setAll(page);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
    }

    public void loadHomePage() {
        loadPage("views/HomePage.fxml");
        homeTab.setStyle(activeStyle);
        accountTab.setStyle(inactiveStyle);
    }

    public void loadAccountPage() {
        loadPage("views/AccountView.fxml");
        homeTab.setStyle(inactiveStyle);
        accountTab.setStyle(activeStyle);
    }

    public void loadCreatePersonalProjectPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/CreateProjectView.fxml"));
            Node view = loader.load();
            CreateProjectController controller = loader.getController();
            controller.setMainController(this);
            mainContent.getChildren().setAll(view); } catch (IOException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
        homeTab.setStyle(inactiveStyle);
        accountTab.setStyle(inactiveStyle);
    }

    public void loadCreateGroupPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/CreateGroupView.fxml"));
            Node view = loader.load();
            CreateGroupController controller = loader.getController();
            controller.setMainController(this);
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
        homeTab.setStyle(inactiveStyle);
        accountTab.setStyle(inactiveStyle);
    }

    public void loadProjectView() {
        try {
            //loadPage("views/ProjectView.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/ProjectView.fxml"));
            Node view = loader.load();
            ProjectViewController controller = loader.getController();
            controller.setMainController(this);
            mainContent.getChildren().setAll(view);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        homeTab.setStyle(inactiveStyle);
        accountTab.setStyle(inactiveStyle);
    }

    public void loadGroupView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/GroupView.fxml"));
            Node view = loader.load();
            GroupViewController controller = loader.getController();
            controller.setMainController(this);
            mainContent.getChildren().setAll(view);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        homeTab.setStyle(inactiveStyle);
        accountTab.setStyle(inactiveStyle);
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
                Stage currentStage = (Stage) logoutButton.getScene().getWindow();
                currentStage.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                //e.printStackTrace();
            }
        });
    }

    // ----------------------------------------------------------

    // ------------------------ OTHER ---------------------------



    public void refreshSidebar() {
        initializePersonalProjectsBox(Session.getCurrentUser());// Your sidebar population method
        initializeGroupsBox(Session.getCurrentUser());
    }

}
