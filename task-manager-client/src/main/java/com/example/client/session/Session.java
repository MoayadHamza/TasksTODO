package com.example.client.session;

import com.example.client.controller.MainController;
import com.example.client.model.PGroup;
import com.example.client.model.Project;
import com.example.client.model.Task;
import com.example.client.model.User;

public class Session {

    private static User currentUser;
    private static PGroup currentGroup;
    private static Project currentProject;
    private static Task currentTask;
    private static MainController currentMainController;

    // ================= HANDLE USER ==================
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearUser() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    // =================================================

    // ================= HANDLE GROUP ==================

    public static void setCurrentGroup(PGroup group) {
            currentGroup = group;
    }

    public static PGroup getCurrentGroup() {
        return currentGroup;
    }

    public static void clearGroup() {
        currentGroup = null;
    }

    // =================================================

    // ================ HANDLE PROJECT =================

    public static void setCurrentProject(Project project) {
        currentProject = project;
    }

    public static Project getCurrentProject() {
        return currentProject;
    }

    public static void clearProject() {
        currentProject = null;
    }

    // =================================================

    // ================= HANDLE TASK ===================

    public static void setCurrentTask(Task task) {
        currentTask = task;
    }

    public static Task getCurrentTask() {
        return currentTask;
    }

    public static void clearTask() {
        currentTask = null;
    }

    // =================================================

    // ============ HANDLE MAIN CONTROLLER =============

    public static void setMainController(MainController controller) {
        currentMainController = controller;
    }

    public static MainController getMainController() {
        return currentMainController;
    }

    public static void clearMainController() {
        currentMainController = null;
    }

    // =================================================

    // =================== GENERAL =====================

    public static void clearSelections() {
        clearTask();
        clearProject();
        clearProject();

    }

    public static void clear() {
        clearTask();
        clearProject();
        clearGroup();
        clearUser();
        clearMainController();

    }

    // =================================================

}
