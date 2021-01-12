package nju.zc.calabashbattle.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import nju.zc.calabashbattle.client.ClientMain;

public class LoginController{
    @FXML
    private Button loginButton;

    @FXML
    private Button replayButton;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField serverIPField;

    private ClientMain mainApp;

    public void init(){
        serverIPField.setText("127.0.0.1");
    }

    @FXML
    private void handleLoginAction(){
        String username = usernameField.getText();
        String IP = serverIPField.getText();
        mainApp.performLogin(username, IP);
    }

    @FXML
    private void handleReplayAction(){
        mainApp.loadReplay();
    }

    public void setMainApp(ClientMain mainApp){
        this.mainApp = mainApp;
    }

	public boolean isTextFieldFocused() {
        return (serverIPField.isFocused() || usernameField.isFocused());
	}

    
}