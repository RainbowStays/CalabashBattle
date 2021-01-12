package nju.zc.calabashbattle.client;

import java.net.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.*;
import nju.zc.calabashbattle.game.data.*;
import nju.zc.calabashbattle.game.message.AbstractMessage;
import nju.zc.calabashbattle.game.message.GameInfoMessage;
import nju.zc.calabashbattle.client.controller.*;
import nju.zc.calabashbattle.client.model.*;

public class ClientMain extends Application {
    public enum stateType {
        waiting_for_logging, logging, matching, game_processing, game_finished
    }

    private stateType state = stateType.waiting_for_logging;// 登录状态

    private Stage primaryStage;

    public ClientSocketController socketController = null;
    public HallController hallController = null;
    public LoginController loginController = null;
    public BattleFieldController fieldController = null;

    public void initLoginLayout() {
        try {
            FXMLLoader fx = new FXMLLoader();
            URL url = getClass().getResource("/resources/Fxml/LOGIN.fxml");
            fx.setLocation(url);
            Pane pane = (Pane) fx.load();
            loginController = (LoginController) fx.getController();
            primaryStage.setTitle("Login");
            primaryStage.setScene(new Scene(pane));
            primaryStage.show();
            primaryStage.setResizable(false);
            loginController.setMainApp(this);
            loginController.init();

            pane.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    switch (event.getCode()) {
                        case L:
                            if(!loginController.isTextFieldFocused())loadReplay();
                            break;
                        default:
                            break;
                    }
                }
            });
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        initLoginLayout();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try {
                    //System.out.println("turning off...");
                    if(socketController != null)socketController.close();
                    
                } catch (Exception e) {
                }
            }

        });
    }

    public void stop(){
        if(socketController != null)socketController.close();
        if(fieldController != null)fieldController.close();
    }

    public void performLogin(String name, String serverIP) {
        try {
            Stage stage = new Stage();

            FXMLLoader fx = new FXMLLoader();
            URL url = getClass().getResource("/resources/Fxml/Hall.fxml");
            fx.setLocation(url);
            Pane root = (Pane) fx.load();

            Hall hallModel = new Hall(name, serverIP, Define.PORT);
            hallController = (HallController) fx.getController();
            hallController.setMainApp(this);
            hallController.setModel(hallModel);
            hallController.init();
            hallController.setStage(stage);
            socketController = new ClientSocketController(this, name, serverIP, Define.PORT);

            changeState(stateType.logging);

            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setTitle("Hall");

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    try {
                        Platform.exit();
                        
                    } catch (Exception e) {
                    }
                }
    
            });

            hallController.print("Connecting to Server on " + serverIP + ":" + Define.PORT + "...");
            Thread connectThread = new Thread(socketController);
            connectThread.start();

        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public void performFieldCreate(GameInfoMessage msg) {
        try {
            if (fieldController == null) {
                Stage stage = new Stage();

                FXMLLoader fx = new FXMLLoader();
                URL url = getClass().getResource("/resources/Fxml/Battle.fxml");
                fx.setLocation(url);
                Pane root = (Pane) fx.load();

                fieldController = (BattleFieldController) fx.getController();
                fieldController.setMainApp(this);
                fieldController.setStage(stage);

                changeState(stateType.game_processing);

                hallController.close();

                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.show();
                stage.setTitle("Game Client");

                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        try {
                            Platform.exit();
                            
                        } catch (Exception e) {
                        }
                    }
        
                });

            }
            fieldController.init(msg.getTeam());
            fieldController.getMessage(msg, false);

        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void loadReplay() {
        try {
            if (fieldController == null) {
                Stage stage = new Stage();

                FXMLLoader fx = new FXMLLoader();
                URL url = getClass().getResource("/resources/Fxml/Battle.fxml");
                fx.setLocation(url);
                Pane root = (Pane) fx.load();

                fieldController = (BattleFieldController) fx.getController();
                fieldController.setMainApp(this);
                fieldController.setStage(stage);

                changeState(stateType.game_processing);

                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.show();
                stage.setTitle("Game Client");

            }
            fieldController.loadReplay();

        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    /*
     * @Override public void stop(){
     * 
     * }
     */

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void changeState(stateType s) {
        if (s == this.state)
            return;
        if (s == stateType.waiting_for_logging) {
            try {
                socketController.close();
            } catch (Exception e) {
            }
            primaryStage.show(); 
        }
        else primaryStage.hide();
        this.state = s;

    }

    public static void main(String[] args) {
        launch(args);
    }

	public void sendFieldControllerMessage(AbstractMessage msg){
		if(fieldController != null){
            fieldController.getMessage(msg, false);
        }
    }

	public void sendMessage(AbstractMessage msg) {
		if(socketController != null){
            socketController.sendMsg(msg);
        }
	}
  
}
