package com.example.client.controller;

import com.example.client.model.User;
import com.example.client.session.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomePageController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            String fullName = currentUser.getFullName();
            welcomeLabel.setText("Welcome, " + fullName + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
    }

}
