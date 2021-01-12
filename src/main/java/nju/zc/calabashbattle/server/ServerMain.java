package nju.zc.calabashbattle.server;

import nju.zc.calabashbattle.server.controller.LogController;

import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import nju.zc.calabashbattle.server.model.Server;

public class ServerMain extends Application{

    //窗口程序初始化
    @Override
    public void start(Stage stage) throws Exception{
        try{
            FXMLLoader fx = new FXMLLoader();
            URL url = getClass().getResource("/resources/Fxml/ServerLog.fxml");
            fx.setLocation(url);
    
            AnchorPane root = (AnchorPane)fx.load();
            
            LogController log = null;
            Button endButton = null;
            
            log = (LogController)fx.getController();
    
            TextArea textArea = log.getTextArea();
            textArea.setEditable(false);
            stage.setScene(new Scene(root));
            stage.setTitle("Server");
            stage.setResizable(false);
            stage.show();

            Server server = new Server(log);
            

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    try {
                        server.print("Closing Server...");
                        server.close();
                        Platform.exit();
                        
                    } catch (Exception e) {
                    }
                }
    
            });
            

            endButton = log.getEndButton();
            endButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent event) {
                    server.print("Closing Server...");
                    server.close();
                    Platform.exit();
                }
                
            });

            server.start();

            }catch(Exception e){
                //e.printStackTrace();
            }
    }
    

    public static void main(String[] args) {
        launch(args);
    }

}