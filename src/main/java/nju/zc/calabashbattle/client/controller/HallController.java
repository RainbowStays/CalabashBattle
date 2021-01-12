package nju.zc.calabashbattle.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import nju.zc.calabashbattle.client.ClientMain;
import nju.zc.calabashbattle.client.ClientMain.stateType;
import nju.zc.calabashbattle.client.model.Hall;

public class HallController{

    ClientMain mainApp;
    Stage stage;
    Hall hallModel;

    @FXML
    private Button hallButton;

    @FXML
    private TextArea displayArea;


    public void init(){
        hallButton.setText("Cancel");
        displayArea.setText("");
    }
    
    public void print(String text){
        displayArea.appendText(text + "\n");
    }

    public void setMainApp(ClientMain mainApp){
        this.mainApp = mainApp;
    }

    public void setStage(Stage s){
        this.stage = s;
    }
    public void setModel(Hall hallModel){
        this.hallModel = hallModel;
    }

    public void onButtonAction(){
        close();
        mainApp.changeState(stateType.waiting_for_logging);
    }

    public void setButtonDisable(boolean state){
        hallButton.setDisable(state);
    }

    public void close(){
        stage.close();
    }

    public void loginFailed(String s){
        hallButton.setText("Exit");
        print(s + ". Press \'Exit\' to exit...");
    }

}