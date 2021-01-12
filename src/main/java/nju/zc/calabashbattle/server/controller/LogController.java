package nju.zc.calabashbattle.server.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class LogController {
    @FXML
    private Button EndButton;

    @FXML
    private TextArea textArea;

    public Button getEndButton(){
        return this.EndButton;
    }

    public TextArea getTextArea(){
        return this.textArea;
    }

    public void print(Object o){
        Platform.runLater(()->
            textArea.appendText(o.toString()+"\n")
        );
    }

    public void onEndAction(){
        
        Platform.exit();
    }
}